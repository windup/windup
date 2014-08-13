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
        return produceGraphContext();
    }

    @Override
    public GraphContext create(Path runDirectory)
    {
        GraphContextImpl context = new GraphContextImpl(graphServices,
                    this.graphTypeRegistry,
                    this.graphApiCompositeClassLoaderProvider);
        context.setGraphDirectory(runDirectory);
        return context;
    }

    @Produces
    @ApplicationScoped
    public GraphContext produceGraphContext()
    {
        if (this.graphContext == null)
        {
            this.graphContext = new GraphContextImpl(graphServices,
                        this.graphTypeRegistry,
                        this.graphApiCompositeClassLoaderProvider);
        }
        return graphContext;
    }
}
