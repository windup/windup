package org.jboss.windup.graph;

import java.io.File;
import java.nio.file.Path;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import com.thinkaurelius.titan.core.Cardinality;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.FrameClassLoaderResolver;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.RandomStringUtils;


public class GraphContextImpl implements GraphContext
{
    private final GraphApiCompositeClassLoaderProvider classLoaderProvider;

    /**
     * Used to get access to all implemented {@link Service} classes.
     */
    private final Imported<Service<? extends VertexFrame>> graphServices;
    private final GraphTypeRegistry graphTypeRegistry;
    private EventGraph<TitanGraph> eventGraph;
    private BatchGraph<TitanGraph> batchGraph;
    private FramedGraph<EventGraph<TitanGraph>> framed;
    
    //private Path graphDirectory;
    private GraphContextConfig config;
    private Exception initStack;
    
            

    @Override
    public GraphTypeRegistry getGraphTypeRegistry()
    {
        return graphTypeRegistry;
    }

    @Override
    public void disconnectFromGraph()
    {
        this.eventGraph.getBaseGraph().shutdown();
        this.eventGraph = null;
        this.batchGraph = null;
        this.framed = null;
    }

    @Override
    public EventGraph<TitanGraph> getGraph()
    {
        initGraphIfNeeded();
        return eventGraph;
    }

    /**
     * Returns a graph suitable for batchGraph processing.
     * 
     * Note: This bypasses the event graph (thus no events will be fired for modifications to this graph)
     */
    public BatchGraph<TitanGraph> getBatch()
    {
        initGraphIfNeeded();
        return batchGraph;
    }

    public FramedGraph<EventGraph<TitanGraph>> getFramed()
    {
        initGraphIfNeeded();
        return framed;
    }

    public GraphContextImpl(Imported<Service<? extends VertexFrame>> graphServices,
                GraphTypeRegistry graphTypeRegistry,
                GraphApiCompositeClassLoaderProvider classLoaderProvider)
    {
        this.graphServices = graphServices;
        this.graphTypeRegistry = graphTypeRegistry;
        this.classLoaderProvider = classLoaderProvider;
    }

    /*
    @Override
    public void setGraphDirectory(Path graphDirectory)
    {
        if (this.eventGraph != null)
        {
            throw new WindupException("Error, attempting to set graph directory to: \"" + graphDirectory.toString()
                        + "\", but the graph has already been initialized (with graph folder: \""
                        + this.graphDirectory.toString()
                        + "\"! To change this, you must first disconnect from the graph!");
        }
        this.graphDirectory = graphDirectory;
    }

    @Override
    public Path getGraphDirectory()
    {
        return graphDirectory;
    }/**/

    
    /**
     * Lazily initializes the graph.
     */
    private void initGraphIfNeeded()
    {
        log.log(Level.FINE, "initGraphIfNeeded() called.");
        if (eventGraph != null)
            // Graph is already initialized, just return.
            return;
        
        log.log(Level.WARNING, "Initializing graph lazily.", stripProxyCalls(new StackTrace()));
        this.reinitGraph(new GraphContextConfig());
    }
    
    
    /**
     * Initializes the context - creates the data directory, creates the graph,
     * applies the configuration, creates the indexes, creates the classloaders,
     * creates FramedGraph.
     * 
     * @throws IllegalStateException if the graph was already initialized.
     */
    @Override
    public void init(GraphContextConfig config){
        if(this.eventGraph != null){
            if(this.config != null && this.config.isWarnOnLazyInit())
                throw new IllegalStateException("Graph was already initialized, see cause's stacktrace for where.", stripProxyCalls(this.initStack));
        }
        this.reinitGraph(config);
    }
    
    
    /**
     * Initializes the context - creates the data directory, creates the graph,
     * applies the configuration, creates the indexes, creates the classloaders,
     * creates FramedGraph.
     * 
     * @param config If the directory.
     */
    private void reinitGraph(GraphContextConfig config)
    {
        log.fine("(Re)initializing graph.");
        this.initStack = new Exception();
        
        if (config == null)
            config = new GraphContextConfig();
        this.config = config;

        // Default graph data dir if null.
        if (config.getGraphDataDir() == null){
            log.fine("Setting default directory.");
            config.setGraphDataDir(this.getDefaultGraphDirectory());
        }

        // Directories
        Path graphDir = config.getGraphDataDir();
        FileUtils.deleteQuietly(graphDir.toFile());

        Path lucene = graphDir.resolve("graphsearch");
        Path berkeley = graphDir.resolve("titangraph");

        // TODO: Externalize this.
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.directory", berkeley.toAbsolutePath().toString());
        conf.setProperty("storage.backend", "berkeleyje");

        conf.setProperty("index.search.backend", "lucene");
        conf.setProperty("index.search.directory", lucene.toAbsolutePath().toString());

        // Graph creation.
        TitanGraph titanGraph = TitanFactory.open(conf);

        // TODO: This has to load dynamically.
        // E.g. get all Model classes and look for @Indexed - org.jboss.windup.graph.api.model.anno.
        String[] keys = new String[] { "namespaceURI", "schemaLocation", "publicId", "rootTagName",
                    "systemId", "qualifiedName", "filePath", "mavenIdentifier", "packageName", "classification" };

        TitanManagement mgmt = titanGraph.getManagementSystem();

        for (String key : keys)
        {
            PropertyKey propKey = mgmt.makePropertyKey(key).dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.buildIndex(key, Vertex.class).addKey(propKey).buildCompositeIndex();
            // titanGraph.makeKey(key).dataType(String.class).indexed(Vertex.class).make();
        }

        for (String key : new String[] { "archiveEntry" })
        {
            PropertyKey propKey = mgmt.makePropertyKey(key).dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.buildIndex(key, Vertex.class).addKey(propKey).buildMixedIndex("search");
        }

        for (String key : new String[] { WindupVertexFrame.TYPE_PROP })
        {
            PropertyKey propKey = mgmt.makePropertyKey(key).dataType(String.class).cardinality(Cardinality.LIST).make();
            mgmt.buildIndex(key, Vertex.class).addKey(propKey).buildCompositeIndex();
        }
        mgmt.commit();

        // Graph extensions.
        this.eventGraph = new EventGraph<TitanGraph>(titanGraph);
        this.batchGraph = new BatchGraph<TitanGraph>(titanGraph, 1000L);

        
        // Composite classloader
        final ClassLoader compositeClassLoader = classLoaderProvider.getCompositeClassLoader();

        final AdjacentMapHandler frameMapHandler = new AdjacentMapHandler();

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
                config.setFrameClassLoaderResolver(fclr);
                config.addMethodHandler(frameMapHandler);
                config.addMethodHandler(new WindupPropertyMethodHandler());
                return baseGraph;
            }
        };

        // Frames with all the features.
        FramedGraphFactory factory = new FramedGraphFactory(
                    addModules, // Composite classloader
                    new JavaHandlerModule(), // @JavaHandler
                    graphTypeRegistry.build(), // Model classes
                    new GremlinGroovyModule() // @Gremlin
        );

        framed = factory.create(eventGraph);

    }
    private static final Logger log = Logger.getLogger(GraphContextImpl.class.getName());

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends VertexFrame, S extends Service<T>> S getService(Class<T> type)
    {
        S closestMatch = null;
        for (Service<? extends VertexFrame> service : graphServices)
        {
            if (service.getType() == type)
            {
                closestMatch = (S) service;
            }
            else if (closestMatch == null && service.getType().isAssignableFrom(type))
            {
                closestMatch = (S) service;
            }
        }
        if (closestMatch == null)
        {
            closestMatch = (S) new GraphService(this, type);
        }
        return closestMatch;
    }

    @Override
    public String toString()
    {
        String graphHash = getGraph() == null ? "null" : "" + getGraph().hashCode();
        return "GraphContextImpl " + hashCode() + ", " +
            (getConfig() == null ? "uninitialized" : ("graph "+graphHash+" in " + getConfig().getGraphDataDir()));
    }

    @Override
    public TypeAwareFramedGraphQuery getQuery()
    {
        return new TypeAwareFramedGraphQuery(getFramed());
    }

    /**
     * This is called if the user does not explicitly specify a graph directory.
     */
    private Path getDefaultGraphDirectory()
    {
        return new File(FileUtils.getTempDirectory(), "windupgraph_" + RandomStringUtils.randomAlphanumeric(6)).toPath();
    }


    public GraphContextConfig getConfig()
    {
        return config;
    }

    
    private static class StackTrace extends RuntimeException
    {
    }

    
    private Throwable stripProxyCalls(Exception ex)
    {
        List<StackTraceElement> newStack = new LinkedList();
        
        for(StackTraceElement call : ex.getStackTrace())
        {
            if(call.getClassName().startsWith("org.jboss.forge.furnace.proxy.")) continue;
            if(call.getClassName().startsWith("org.jboss.forge.furnace.util.ClassLoaders")) continue;
            if(call.getClassName().startsWith("org.jboss.weld.bean.proxy.")) continue;
            if(call.getClassName().startsWith("org.jboss.weld.proxies."))
                // Weld prefixes proxies with its package, but we need to see the method name.
                if( ! call.getClassName().contains("$$") ) continue;
            if(call.getClassName().startsWith("sun.reflect.")) continue;
            if(call.getClassName().startsWith("java.lang.reflect.")) continue;
            
            newStack.add(call);

            // Skip Arquillian, Surefire and Maven stuff.
            if(call.getClassName().startsWith("org.jboss.arquillian.")) break;
        }
        
        Exception newEx = new Exception(ex.getMessage(), ex.getCause());
        newEx.setStackTrace(newStack.toArray(new StackTraceElement[newStack.size()]));
        return newEx;
    }

    @Override
    public Path getGraphDirectory()
    {
        return config.getGraphDataDir();
    }

}
