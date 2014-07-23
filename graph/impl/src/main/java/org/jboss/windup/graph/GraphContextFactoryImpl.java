package org.jboss.windup.graph;

import java.io.File;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.graph.service.Service;

import com.tinkerpop.frames.VertexFrame;

@Singleton
public class GraphContextFactoryImpl implements GraphContextFactory
{
    @Inject
    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;

    @Inject
    private Imported<Service<? extends VertexFrame>> graphServices;

    @Inject
    private GraphTypeRegistry graphTypeRegistry;

    private GraphContext graphContext;

    @Override
    public GraphContext create()
    {
        return produceGraphContext();
    }

    @Override
    public GraphContext create(File runDirectory)
    {
        return new GraphContextImpl(graphServices, runDirectory,
                    this.graphTypeRegistry,
                    this.graphApiCompositeClassLoaderProvider);
    }

    @Produces
    @ApplicationScoped
    public GraphContext produceGraphContext()
    {
        if (this.graphContext == null)
        {
            this.graphContext = create(new File(this.getRunDirectory(), "windup-graph"));
        }
        return graphContext;
    }

    private File getRunDirectory()
    {
        return new File(FileUtils.getTempDirectory(), "windupgraph_" + UUID.randomUUID().toString());
    }

}
