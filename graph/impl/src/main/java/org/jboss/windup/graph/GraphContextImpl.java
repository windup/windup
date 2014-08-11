package org.jboss.windup.graph;

import java.io.File;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
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

public class GraphContextImpl implements GraphContext
{
    private final GraphApiCompositeClassLoaderProvider classLoaderProvider;

    /**
     * Used to get access to all implemented {@link Service} classes.
     */
    private final Imported<Service<? extends VertexFrame>> graphServices;
    private final EventGraph<TitanGraph> eventGraph;
    private final TitanGraph titanGraph;
    private final BatchGraph<TitanGraph> batch;
    private final FramedGraph<EventGraph<TitanGraph>> framed;
    private final GraphTypeRegistry graphTypeRegistry;
    private final File diskCacheDir;

    public EventGraph<TitanGraph> getGraph()
    {
        return eventGraph;
    }

    @Override
    public GraphTypeRegistry getGraphTypeRegistry()
    {
        return graphTypeRegistry;
    }

    /**
     * Returns a graph suitable for batch processing.
     * 
     * Note: This bypasses the event graph (thus no events will be fired for modifications to this graph)
     */
    public BatchGraph<TitanGraph> getBatch()
    {
        return batch;
    }

    public FramedGraph<EventGraph<TitanGraph>> getFramed()
    {
        return framed;
    }

    public GraphContextImpl(Imported<Service<? extends VertexFrame>> graphServices, File diskCache,
                GraphTypeRegistry graphTypeRegistry,
                GraphApiCompositeClassLoaderProvider classLoaderProvider)
    {
        this.graphServices = graphServices;
        this.graphTypeRegistry = graphTypeRegistry;
        this.classLoaderProvider = classLoaderProvider;

        FileUtils.deleteQuietly(diskCache);
        this.diskCacheDir = diskCache;

        File lucene = new File(diskCache, "graphsearch");
        File berkeley = new File(diskCache, "graph");

        // TODO: Externalize this.
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.directory", berkeley.getAbsolutePath());
        conf.setProperty("storage.backend", "berkeleyje");

        conf.setProperty("storage.index.search.backend", "lucene");
        conf.setProperty("storage.index.search.directory", lucene.getAbsolutePath());
        conf.setProperty("storage.index.search.client-only", "false");
        conf.setProperty("storage.index.search.local-mode", "true");

        this.titanGraph = TitanFactory.open(conf);
        this.eventGraph = new EventGraph<TitanGraph>(this.titanGraph);

        // TODO: This has to load dynamically.
        // E.g. get all Model classes and look for @Indexed - org.jboss.windup.graph.api.model.anno.
        String[] keys = new String[] { "namespaceURI", "schemaLocation", "publicId", "rootTagName",
                    "systemId", "qualifiedName", "filePath", "mavenIdentifier" };
        for (String key : keys)
        {
            this.titanGraph.makeKey(key).dataType(String.class).indexed(Vertex.class).make();
        }

        for (String key : new String[] { "archiveEntry", WindupVertexFrame.TYPE_PROP })
        {
            this.titanGraph.makeKey(key).dataType(String.class).indexed("search", Vertex.class).make();
        }

        batch = new BatchGraph<TitanGraph>(this.titanGraph, 1000L);

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
    public File getDiskCacheDirectory()
    {
        return diskCacheDir;
    }

    @Override
    public String toString()
    {
        return "GraphContext: " + getDiskCacheDirectory();
    }

}
