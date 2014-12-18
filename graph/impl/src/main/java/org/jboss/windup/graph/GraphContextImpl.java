package org.jboss.windup.graph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.listeners.AfterGraphInitializationListener;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.wrapper.EventGraphWithMultiThreadedTitanTransactions;

import com.thinkaurelius.titan.core.Cardinality;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.FrameClassLoaderResolver;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;

public class GraphContextImpl implements GraphContext
{
    private static final Logger log = Logger.getLogger(GraphContextImpl.class.getName());

    private Furnace furnace;
    private GraphApiCompositeClassLoaderProvider classLoaderProvider;
    private Map<String, Object> configurationOptions;
    private final GraphTypeRegistry graphTypeRegistry;
    private final TitanGraph titanGraph;
    private final EventGraphWithMultiThreadedTitanTransactions eventGraph;
    private final FramedGraph<? extends Graph> framed;
    private final List<GraphChangedListener> listeners = new ArrayList<>();

    private BatchGraph<TitanGraph> batchGraph;

    private final Path graphDir;

    public GraphContextImpl(Furnace furnace, GraphTypeRegistry graphTypeRegistry, GraphApiCompositeClassLoaderProvider classLoaderProvider,
                Path graphDir)
    {
        this.furnace = furnace;
        this.classLoaderProvider = classLoaderProvider;
        this.graphTypeRegistry = graphTypeRegistry;
        this.graphDir = graphDir;

        log.fine("Initializing graph.");

        FileUtils.deleteQuietly(graphDir.toFile());

        Path lucene = graphDir.resolve("graphsearch");
        Path berkeley = graphDir.resolve("titangraph");

        // TODO: Externalize this.
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.directory", berkeley.toAbsolutePath().toString());
        conf.setProperty("storage.backend", "berkeleyje");

        // Sets the berkeley cache to a relatively small value to reduce the memory footprint.
        // This is actually more important than performance on some of the smaller machines out there, and
        // the performance decrease seems to be minimal.
        conf.setProperty("storage.berkeleydb.cache-percentage", 1);

        //
        // turn on a db-cache that persists across txn boundaries, but make it relatively small
        conf.setProperty("cache.db-cache", true);
        conf.setProperty("cache.db-cache-clean-wait", 0);
        conf.setProperty("cache.db-cache-size", .05);
        conf.setProperty("cache.db-cache-time", 0);

        conf.setProperty("index.search.backend", "lucene");
        conf.setProperty("index.search.directory", lucene.toAbsolutePath().toString());

        this.titanGraph = TitanFactory.open(conf);

        // TODO: This has to load dynamically.
        // E.g. get all Model classes and look for @Indexed - org.jboss.windup.graph.api.model.anno.
        String[] keys = new String[] { "namespaceURI", "schemaLocation", "publicId", "rootTagName",
                    "systemId", "qualifiedName", "filePath", "mavenIdentifier", "packageName", "classification" };

        TitanManagement mgmt = titanGraph.getManagementSystem();

        for (String key : keys)
        {
            PropertyKey propKey = mgmt.makePropertyKey(key).dataType(String.class).cardinality(Cardinality.SINGLE)
                        .make();
            mgmt.buildIndex(key, Vertex.class).addKey(propKey).buildCompositeIndex();
        }

        for (String key : new String[] { "referenceSourceSnippit" })
        {
            PropertyKey propKey = mgmt.makePropertyKey(key).dataType(String.class).cardinality(Cardinality.SINGLE)
                        .make();
            mgmt.buildIndex(key, Vertex.class).addKey(propKey).buildMixedIndex("search");
        }

        for (String key : new String[] { WindupVertexFrame.TYPE_PROP })
        {
            PropertyKey propKey = mgmt.makePropertyKey(key).dataType(String.class).cardinality(Cardinality.LIST).make();
            mgmt.buildIndex(key, Vertex.class).addKey(propKey).buildCompositeIndex();
        }
        mgmt.commit();

        this.eventGraph = new EventGraphWithMultiThreadedTitanTransactions(titanGraph);
        for (GraphChangedListener listener : this.listeners)
        {
            this.eventGraph.addListener(listener);
        }
        this.batchGraph = new BatchGraph<TitanGraph>(titanGraph, 1000L);
        final ClassLoader compositeClassLoader = classLoaderProvider.getCompositeClassLoader();

        final FrameClassLoaderResolver fclr = new FrameClassLoaderResolver()
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
                // Composite classloader
                config.setFrameClassLoaderResolver(fclr);

                // Custom annotations handlers.
                config.addMethodHandler(new MapInPropertiesHandler());
                config.addMethodHandler(new MapInAdjacentPropertiesHandler());
                config.addMethodHandler(new MapInAdjacentVerticesHandler());

                // Returns "this" for setters, to allow things like frame.setFoo(123).setBar(456).
                config.addMethodHandler(new WindupPropertyMethodHandler());
                return baseGraph;
            }
        };

        FramedGraphFactory factory = new FramedGraphFactory(
                    addModules, // See above
                    new JavaHandlerModule(), // @JavaHandler
                    graphTypeRegistry.build(), // Model classes
                    new GremlinGroovyModule() // @Gremlin
        );

        this.framed = factory.create(eventGraph);

        Imported<AfterGraphInitializationListener> afterInitializationListeners = furnace.getAddonRegistry().getServices(
                    AfterGraphInitializationListener.class);
        Map<String, Object> confProps = new HashMap<>();
        Iterator<?> keyIter = conf.getKeys();
        while (keyIter.hasNext())
        {
            String key = (String) keyIter.next();
            confProps.put(key, conf.getProperty(key));
        }

        log.fine("Properties: " + confProps);
        if (!afterInitializationListeners.isUnsatisfied())
        {
            for (AfterGraphInitializationListener listener : afterInitializationListeners)
            {
                listener.afterGraphStarted(confProps, this);
            }
        }
    }

    @Override
    public void commit()
    {
        this.eventGraph.commit();
        this.framed.fireWrappedGraphReplacedEvent();
    }

    @Override
    public void addGraphChangedListener(GraphChangedListener listener)
    {
        this.listeners.add(listener);
    }

    @Override
    public GraphTypeRegistry getGraphTypeRegistry()
    {
        return graphTypeRegistry;
    }

    @Override
    public void close()
    {
        this.eventGraph.shutdown();
    }

    @Override
    public void clear()
    {
        TitanCleanup.clear(this.titanGraph);
    }

    @Override
    public EventGraph<TitanTransaction> getGraph()
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
    public FramedGraph<? extends Graph> getFramed()
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
}
