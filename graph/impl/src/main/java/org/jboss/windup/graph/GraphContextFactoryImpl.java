package org.jboss.windup.graph;

import java.io.File;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
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
                    this.graphApiCompositeClassLoaderProvider, getDefaultGraphDirectory());
    }

    @Override
    public GraphContext create(Path graphDir)
    {
        return new GraphContextImpl(
                    this.graphServices,
                    this.graphTypeRegistry,
                    this.graphApiCompositeClassLoaderProvider, graphDir);
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

    private Path getDefaultGraphDirectory()
    {
        return new File(FileUtils.getTempDirectory(), "windupgraph_" + RandomStringUtils.randomAlphanumeric(6))
                    .toPath();
    }
}
