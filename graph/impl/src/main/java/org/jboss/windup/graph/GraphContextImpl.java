package org.jboss.windup.graph;

import java.io.File;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.FrameClassLoaderResolver;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;

public class GraphContextImpl implements GraphContext
{
    private GraphApiCompositeClassLoaderProvider classLoaderProvider;

    private TitanGraph graph;
    private BatchGraph<TitanGraph> batch;
    private FramedGraph<TitanGraph> framed;
    private GraphTypeRegistry graphTypeRegistry;
    private File diskCacheDir;

    public TitanGraph getGraph()
    {
        return graph;
    }

    @Override
    public GraphTypeRegistry getGraphTypeRegistry()
    {
        return graphTypeRegistry;
    }

    public BatchGraph<TitanGraph> getBatch()
    {
        return batch;
    }

    public FramedGraph<TitanGraph> getFramed()
    {
        return framed;
    }

    public GraphContextImpl(File diskCache, GraphTypeRegistry graphTypeRegistry,
                GraphApiCompositeClassLoaderProvider classLoaderProvider)
    {
        this.graphTypeRegistry = graphTypeRegistry;
        this.classLoaderProvider = classLoaderProvider;

        FileUtils.deleteQuietly(diskCache);
        this.diskCacheDir = diskCache;

        File lucene = new File(diskCache, "graphsearch");
        File berkley = new File(diskCache, "graph");

        // TODO: Externalize this.
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.directory", berkley.getAbsolutePath());
        conf.setProperty("storage.backend", "berkeleyje");

        conf.setProperty("storage.index.search.backend", "lucene");
        conf.setProperty("storage.index.search.directory", lucene.getAbsolutePath());
        conf.setProperty("storage.index.search.client-only", "false");
        conf.setProperty("storage.index.search.local-mode", "true");

        graph = TitanFactory.open(conf);

        // TODO: This has to load dynamically.
        // E.g. get all Model classes and look for @Indexed - org.jboss.windup.graph.api.model.anno.
        String[] keys = new String[] { "namespaceURI", "schemaLocation", "publicId", "rootTagName",
                    "systemId", "qualifiedName", "filePath", "mavenIdentifier" };
        for (String key : keys)
        {
            graph.makeKey(key).dataType(String.class).indexed(Vertex.class).make();
        }

        for (String key : new String[] { "archiveEntry", "type" })
        {
            graph.makeKey(key).dataType(String.class).indexed("search", Vertex.class).make();
        }

        batch = new BatchGraph<TitanGraph>(graph, 1000L);

        // Composite classloader
        final ClassLoader compositeClassLoader = classLoaderProvider.getCompositeClassLoader();

        final FrameClassLoaderResolver fclr = new FrameClassLoaderResolver()
        {
            public ClassLoader resolveClassLoader(Class<?> frameType)
            {
                return compositeClassLoader;
            }
        };

        final Module fclrModule = new Module()
        {
            @Override
            public Graph configure(Graph baseGraph, FramedGraphConfiguration config)
            {
                config.setFrameClassLoaderResolver(fclr);
                return baseGraph;
            }
        };

        // Frames with all the features.
        FramedGraphFactory factory = new FramedGraphFactory(
                    fclrModule, // Composite classloader
                    new JavaHandlerModule(), // @JavaHandler
                    graphTypeRegistry.build(), // Model classes
                    new GremlinGroovyModule() // @Gremlin
        );

        framed = factory.create(graph);
    }

    @Override
    public File getDiskCacheDirectory()
    {
        return diskCacheDir;
    }

}
