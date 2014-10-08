package org.jboss.windup.graph;

import java.nio.file.Path;
import java.util.logging.Logger;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.Service;

import com.thinkaurelius.titan.core.Cardinality;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
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
    private static final Logger log = Logger.getLogger(GraphContextImpl.class.getName());

    private final GraphTypeRegistry graphTypeRegistry;
    private final EventGraph<TitanGraph> eventGraph;
    private final BatchGraph<TitanGraph> batchGraph;
    private final FramedGraph<EventGraph<TitanGraph>> framed;

    private final Path graphDir;

    public GraphContextImpl(Imported<Service<? extends VertexFrame>> graphServices,
                GraphTypeRegistry graphTypeRegistry, GraphApiCompositeClassLoaderProvider classLoaderProvider,
                Path graphDir)
    {
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

        conf.setProperty("index.search.backend", "lucene");
        conf.setProperty("index.search.directory", lucene.toAbsolutePath().toString());

        final TitanGraph titanGraph = TitanFactory.open(conf);

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
            // titanGraph.makeKey(key).dataType(String.class).indexed(Vertex.class).make();
        }

        for (String key : new String[] { "archiveEntry" })
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

        this.eventGraph = new EventGraph<TitanGraph>(titanGraph);
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
            addModules,                // See above
            new JavaHandlerModule(),   // @JavaHandler
            graphTypeRegistry.build(), // Model classes
            new GremlinGroovyModule()  // @Gremlin
        );

        framed = factory.create(eventGraph);

    }

    @Override
    public GraphTypeRegistry getGraphTypeRegistry()
    {
        return graphTypeRegistry;
    }

    @Override
    public void close()
    {
        this.eventGraph.getBaseGraph().shutdown();
    }

    @Override
    public void clear()
    {
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
    public String toString()
    {
        String graphHash = getGraph() == null ? "null" : "" + getGraph().hashCode();
        return "GraphContextImpl(" + hashCode() + "), Graph(" + graphHash + ") + DataDir(" + getGraphDirectory() + ")";
    }
}
