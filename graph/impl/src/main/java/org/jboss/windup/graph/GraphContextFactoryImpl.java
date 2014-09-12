package org.jboss.windup.graph;

import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

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
        return new GraphContextImpl(
            this.graphServices,
            this.graphTypeRegistry,
            this.graphApiCompositeClassLoaderProvider);
    }

    @Override
    public GraphContext create(Path graphDataDir)
    {
        GraphContext context = this.create();
        GraphContextConfig cfg = new GraphContextConfig().setGraphDataDir(graphDataDir);
        context.init(cfg);
        return context;
    }

    @Produces
    @ApplicationScoped
    public GraphContext produceGraphContext()
    {
        if (this.graphContext == null)
        {
            this.graphContext = this.create();
        }
        return graphContext;
    }
}
