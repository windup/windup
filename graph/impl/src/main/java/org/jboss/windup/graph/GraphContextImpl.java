package org.jboss.windup.graph;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.util.exception.WindupException;

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
    private final GraphTypeRegistry graphTypeRegistry;
    private EventGraph<TitanGraph> eventGraph;
    private BatchGraph<TitanGraph> batch;
    private FramedGraph<EventGraph<TitanGraph>> framed;
    private Path graphDirectory;

    @Override
    public GraphTypeRegistry getGraphTypeRegistry()
    {
        return graphTypeRegistry;
    }

    public EventGraph<TitanGraph> getGraph()
    {
        initGraphIfNeeded();
        return eventGraph;
    }

    /**
     * Returns a graph suitable for batch processing.
     * 
     * Note: This bypasses the event graph (thus no events will be fired for modifications to this graph)
     */
    public BatchGraph<TitanGraph> getBatch()
    {
        initGraphIfNeeded();
        return batch;
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

    @Override
    public void setGraphDirectory(Path graphDirectory)
    {
        if (this.eventGraph != null)
        {
            throw new WindupException("Error, attempting to set graph directory to: \"" + graphDirectory.toString()
                        + "\", but the graph has already been initialized (with graph folder: \""
                        + this.graphDirectory.toString() + "\"!");
        }
        this.graphDirectory = graphDirectory;
    }

    @Override
    public Path getGraphDirectory()
    {
        return graphDirectory;
    }

    private void initGraphIfNeeded()
    {
        if (eventGraph != null)
        {
            // graph is already initialized, just return
            return;
        }

        if (graphDirectory == null)
        {
            // just give it a default value
            setGraphDirectory(getDefaultGraphDirectory());
        }

        FileUtils.deleteQuietly(graphDirectory.toFile());

        Path lucene = graphDirectory.resolve("graphsearch");
        Path berkeley = graphDirectory.resolve("titangraph");

        // TODO: Externalize this.
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.directory", berkeley.toAbsolutePath().toString());
        conf.setProperty("storage.backend", "berkeleyje");

        conf.setProperty("storage.index.search.backend", "lucene");
        conf.setProperty("storage.index.search.directory", lucene.toAbsolutePath().toString());
        conf.setProperty("storage.index.search.client-only", "false");
        conf.setProperty("storage.index.search.local-mode", "true");

        TitanGraph titanGraph = TitanFactory.open(conf);

        // TODO: This has to load dynamically.
        // E.g. get all Model classes and look for @Indexed - org.jboss.windup.graph.api.model.anno.
        String[] keys = new String[] { "namespaceURI", "schemaLocation", "publicId", "rootTagName",
                    "systemId", "qualifiedName", "filePath", "mavenIdentifier", "packageName" };
        for (String key : keys)
        {
            titanGraph.makeKey(key).dataType(String.class).indexed(Vertex.class).make();
        }

        for (String key : new String[] { "archiveEntry" })
        {
            titanGraph.makeKey(key).dataType(String.class).indexed("search", Vertex.class).make();
        }

        for (String key : new String[] { WindupVertexFrame.TYPE_PROP })
        {
            titanGraph.makeKey(key).list().dataType(String.class).indexed(Vertex.class).make();
        }

        this.eventGraph = new EventGraph<TitanGraph>(titanGraph);
        batch = new BatchGraph<TitanGraph>(titanGraph, 1000L);

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
    public String toString()
    {
        return "GraphContext: " + getGraphDirectory().toString();
    }

    @Override
    public TypeAwareFramedGraphQuery getQuery()
    {
        return new TypeAwareFramedGraphQuery(getFramed());
    }

    /**
     * This is called if the user does not explicitly specify a graph directory
     */
    private Path getDefaultGraphDirectory()
    {
        return new File(FileUtils.getTempDirectory(), "windupgraph_" + UUID.randomUUID().toString()).toPath();
    }
}
