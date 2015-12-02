package org.jboss.windup.graph;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Annotations;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.listeners.AfterGraphInitializationListener;
import org.jboss.windup.graph.listeners.BeforeGraphCloseListener;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.thinkaurelius.titan.core.Cardinality;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.Mapping;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.FrameClassLoaderResolver;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;

public class GraphContextImpl implements GraphContext
{
    private static final Logger log = Logger.getLogger(GraphContextImpl.class.getName());

    private final Furnace furnace;
    private Map<String, Object> configurationOptions;
    private final GraphTypeManager graphTypeManager;
    private EventGraph<TitanGraph> eventGraph;
    private BatchGraph<TitanGraph> batchGraph;
    private FramedGraph<EventGraph<TitanGraph>> framed;
    private Configuration conf;

    private final Path graphDir;

    private final GraphApiCompositeClassLoaderProvider classLoaderProvider;

    /**
     * Used to save all the {@link BeforeGraphCloseListener}s that are also {@link AfterGraphInitializationListener}. This is due a need to call
     * {@link BeforeGraphCloseListener#beforeGraphClose()} on the same instance on which
     * {@link AfterGraphInitializationListener#afterGraphStarted(Map, GraphContext)} } was called
     */
    private final Map<String, BeforeGraphCloseListener> beforeGraphCloseListenerBuffer = new HashMap<>();

    public GraphContextImpl(Furnace furnace, GraphTypeManager typeManager,
                GraphApiCompositeClassLoaderProvider classLoaderProvider, Path graphDir)
    {
        this.furnace = furnace;
        this.graphTypeManager = typeManager;
        this.classLoaderProvider = classLoaderProvider;
        this.graphDir = graphDir;
    }

    public GraphContextImpl create()
    {
        FileUtils.deleteQuietly(graphDir.toFile());
        TitanGraph titan = initializeTitanGraph();
        initializeTitanIndexes(titan);
        createFramed(titan);
        fireListeners();
        return this;
    }

    public GraphContextImpl load()
    {
        TitanGraph titan = initializeTitanGraph();
        createFramed(titan);
        fireListeners();
        return this;
    }

    private void fireListeners()
    {
        Imported<AfterGraphInitializationListener> afterInitializationListeners = furnace.getAddonRegistry().getServices(
                    AfterGraphInitializationListener.class);
        Map<String, Object> confProps = new HashMap<>();
        Iterator<?> keyIter = conf.getKeys();
        while (keyIter.hasNext())
        {
            String key = (String) keyIter.next();
            confProps.put(key, conf.getProperty(key));
        }

        if (!afterInitializationListeners.isUnsatisfied())
        {
            for (AfterGraphInitializationListener listener : afterInitializationListeners)
            {
                listener.afterGraphStarted(confProps, this);
                if (listener instanceof BeforeGraphCloseListener)
                {
                    beforeGraphCloseListenerBuffer.put(listener.getClass().toString(), (BeforeGraphCloseListener) listener);
                }
            }
        }
    }

    private void createFramed(TitanGraph titanGraph)
    {
        this.eventGraph = new EventGraph<>(titanGraph);
        this.batchGraph = new BatchGraph<>(titanGraph, 1000L);

        final ClassLoader compositeClassLoader = classLoaderProvider.getCompositeClassLoader();

        final FrameClassLoaderResolver classLoaderResolver = new FrameClassLoaderResolver()
        {
            public ClassLoader resolveClassLoader(Class<?> frameType)
            {
                return compositeClassLoader;
            }
        };

        final Module addModules = new Module()
        {
            @Override
            public Graph configure(Graph baseGraph, FramedGraphConfiguration config)
            {
                config.setFrameClassLoaderResolver(classLoaderResolver);
                config.addFrameInitializer(new DefaultValueInitializer());
                config.addMethodHandler(new MapInPropertiesHandler());
                config.addMethodHandler(new MapInAdjacentPropertiesHandler());
                config.addMethodHandler(new MapInAdjacentVerticesHandler());
                config.addMethodHandler(new SetInPropertiesHandler());

                return baseGraph;
            }
        };



        FramedGraphFactory factory = new FramedGraphFactory(
                    addModules,
                    new JavaHandlerModule(),   // Supports @JavaHandler
                    graphTypeManager.build(),     // Adds detected WindupVertexFrame/Model classes
                    new GremlinGroovyModule() // Supports @Gremlin
        );

        framed = factory.create(eventGraph);
    }

    private void initializeTitanIndexes(TitanGraph titanGraph)
    {
        Map<String, Class<?>> defaultIndexKeys = new HashMap<>();
        Map<String, Class<?>> searchIndexKeys = new HashMap<>();
        Map<String, Class<?>> listIndexKeys = new HashMap<>();

        Set<Class<? extends WindupVertexFrame>> modelTypes = graphTypeManager.getRegisteredTypes();
        for (Class<? extends WindupVertexFrame> type : modelTypes)
        {
            for (Method method : type.getDeclaredMethods())
            {
                Indexed index = method.getAnnotation(Indexed.class);
                if (index != null)
                {
                    Property property = Annotations.getAnnotation(method, Property.class);
                    if (property != null)
                    {
                        Class<?> dataType = index.dataType();
                        switch (index.value())
                        {
                        case DEFAULT:
                            defaultIndexKeys.put(property.value(), dataType);
                            break;

                        case SEARCH:
                            searchIndexKeys.put(property.value(), dataType);
                            break;

                        case LIST:
                            listIndexKeys.put(property.value(), dataType);
                            break;

                        default:
                            break;
                        }
                    }
                }
            }
        }

        /*
         * This is the root Model index that enables us to query on frame-type (by subclass type, etc.) Without this, every typed query would be slow.
         * Do not remove this unless something really paradigm-shifting has happened.
         */
        listIndexKeys.put(WindupVertexFrame.TYPE_PROP, String.class);

        log.info("Detected and initialized [" + defaultIndexKeys.size() + "] default indexes: " + defaultIndexKeys);
        log.info("Detected and initialized [" + searchIndexKeys.size() + "] search indexes: " + searchIndexKeys);
        log.info("Detected and initialized [" + listIndexKeys.size() + "] list indexes: " + listIndexKeys);

        TitanManagement titan = titanGraph.getManagementSystem();
        for (Map.Entry<String, Class<?>> entry : defaultIndexKeys.entrySet())
        {
            String key = entry.getKey();
            Class<?> dataType = entry.getValue();

            PropertyKey propKey = titan.makePropertyKey(key).dataType(dataType).cardinality(Cardinality.SINGLE).make();
            titan.buildIndex(key, Vertex.class).addKey(propKey).buildCompositeIndex();
        }

        for (Map.Entry<String, Class<?>> entry : searchIndexKeys.entrySet())
        {
            String key = entry.getKey();
            Class<?> dataType = entry.getValue();

            if (dataType == String.class)
            {
                PropertyKey propKey = titan.makePropertyKey(key).dataType(String.class).cardinality(Cardinality.SINGLE).make();
                titan.buildIndex(key, Vertex.class).addKey(propKey, Mapping.STRING.getParameter()).buildMixedIndex("search");
            }
            else
            {
                PropertyKey propKey = titan.makePropertyKey(key).dataType(dataType).cardinality(Cardinality.SINGLE).make();
                titan.buildIndex(key, Vertex.class).addKey(propKey).buildMixedIndex("search");
            }
        }

        for (Map.Entry<String, Class<?>> entry : listIndexKeys.entrySet())
        {
            String key = entry.getKey();
            Class<?> dataType = entry.getValue();

            PropertyKey propKey = titan.makePropertyKey(key).dataType(dataType).cardinality(Cardinality.LIST).make();
            titan.buildIndex(key, Vertex.class).addKey(propKey).buildCompositeIndex();
        }

        titan.commit();
    }

    private TitanGraph initializeTitanGraph()
    {
        log.fine("Initializing graph.");

        Path lucene = graphDir.resolve("graphsearch");
        Path berkeley = graphDir.resolve("titangraph");

        // TODO: Externalize this.
        conf = new BaseConfiguration();

        // Sets a unique id in order to fix WINDUP-697. This causes Titan to not attempt to generate and ID,
        // as the Titan id generation code fails on machines with broken network configurations.
        conf.setProperty("graph.unique-instance-id", "windup_" + System.nanoTime() + "_" + RandomStringUtils.randomAlphabetic(6));
        conf.setProperty("storage.directory", berkeley.toAbsolutePath().toString());
        conf.setProperty("storage.backend", "berkeleyje");

        // Sets the berkeley cache to a relatively small value to reduce the memory footprint.
        // This is actually more important than performance on some of the smaller machines out there, and
        // the performance decrease seems to be minimal.
        conf.setProperty("storage.berkeleydb.cache-percentage", 1);

        // Increase storage write buffer since we basically do a large bulk load during the first phases.
        // See http://s3.thinkaurelius.com/docs/titan/current/bulk-loading.html
        conf.setProperty("storage.buffer-size", "4096");

        //
        // turn on a db-cache that persists across txn boundaries, but make it relatively small
        conf.setProperty("cache.db-cache", true);
        conf.setProperty("cache.db-cache-clean-wait", 0);
        conf.setProperty("cache.db-cache-size", .09);
        conf.setProperty("cache.db-cache-time", 0);

        conf.setProperty("index.search.backend", "lucene");
        conf.setProperty("index.search.directory", lucene.toAbsolutePath().toString());

        writeToPropertiesFile(conf, graphDir.resolve("TitanConfiguration.properties").toFile());
        return TitanFactory.open(conf);
    }

    public Configuration getConfiguration()
    {
        return conf;
    }

    @Override
    public GraphTypeManager getGraphTypeManager()
    {
        return graphTypeManager;
    }

    @Override
    public void close()
    {
        Imported<BeforeGraphCloseListener> beforeCloseListeners = furnace.getAddonRegistry().getServices(BeforeGraphCloseListener.class);
        for (BeforeGraphCloseListener listener : beforeCloseListeners)
        {
            if (!beforeGraphCloseListenerBuffer.containsKey(listener.getClass().toString()))
            {
                beforeGraphCloseListenerBuffer.put(listener.getClass().toString(), listener);
            }
        }
        for (BeforeGraphCloseListener listener : beforeGraphCloseListenerBuffer.values())
        {
            listener.beforeGraphClose();
        }
        beforeGraphCloseListenerBuffer.clear();
        this.eventGraph.getBaseGraph().shutdown();
    }

    @Override
    public void clear()
    {
        if (this.eventGraph == null)
            return;
        if (this.eventGraph.getBaseGraph() == null)
            return;
        if (this.eventGraph.getBaseGraph().isOpen())
            close();

        TitanCleanup.clear(this.eventGraph.getBaseGraph());
    }

    @Override
    public EventGraph<TitanGraph> getGraph()
    {
        return eventGraph;
    }

    /**
     * Returns a graph suitable for batchGraph processing.
     * <p>
     * Note: This bypasses the event graph (thus no events will be fired for modifications to this graph)
     */
    public BatchGraph<TitanGraph> getBatch()
    {
        return batchGraph;
    }

    @Override
    public FramedGraph<EventGraph<TitanGraph>> getFramed()
    {
        return framed;
    }

    @Override
    public TypeAwareFramedGraphQuery getQuery()
    {
        return new TypeAwareFramedGraphQuery(getFramed());
    }

    @Override
    public Path getGraphDirectory()
    {
        return graphDir;
    }

    @Override
    public Map<String, Object> getOptionMap()
    {
        if (this.configurationOptions == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(this.configurationOptions);
    }

    @Override
    public void setOptions(Map<String, Object> options)
    {
        this.configurationOptions = options;
    }

    @Override
    public String toString()
    {
        String graphHash = getGraph() == null ? "null" : "" + getGraph().hashCode();
        return "GraphContextImpl(" + hashCode() + "), Graph(" + graphHash + ") + DataDir(" + getGraphDirectory() + ")";
    }

    private void writeToPropertiesFile(Configuration conf, File file)
    {
        System.out.println("Writing graph config to " + file);
        try
        {
            PropertiesConfiguration propConf = new PropertiesConfiguration(file);
            propConf.append(conf);
            propConf.save();
        }
        catch (ConfigurationException ex)
        {
            throw new RuntimeException("Failed writing Titan config to " + file.getAbsolutePath() + ": " + ex.getMessage(), ex);
        }
    }

}
