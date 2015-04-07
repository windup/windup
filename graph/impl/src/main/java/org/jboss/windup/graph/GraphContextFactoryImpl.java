package org.jboss.windup.graph;

import java.io.File;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.forge.furnace.Furnace;

@Singleton
public class GraphContextFactoryImpl implements GraphContextFactory
{
    @Inject
    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;

    @Inject
    private Furnace furnace;

    @Inject
    private GraphTypeRegistry graphTypeRegistry;

    @Inject
    private GraphTypeManager graphTypeManager;

    private GraphContext graphContext;

    @Override
    public GraphContext create()
    {
        return new GraphContextImpl(
                    furnace,
                    graphTypeRegistry,
                    graphTypeManager,
                    graphApiCompositeClassLoaderProvider,
                    getTempGraphDirectory()).create();
    }

    @Override
    public GraphContext create(Path graphDir)
    {
        return new GraphContextImpl(
                    furnace,
                    graphTypeRegistry,
                    graphTypeManager,
                    graphApiCompositeClassLoaderProvider,
                    graphDir).create();
    }

    @Override
    public GraphContext load(Path graphDir)
    {
        return new GraphContextImpl(
                    furnace,
                    graphTypeRegistry,
                    graphTypeManager,
                    graphApiCompositeClassLoaderProvider,
                    graphDir).load();
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

    private Path getTempGraphDirectory()
    {
        return new File(FileUtils.getTempDirectory(), "windupgraph_" + RandomStringUtils.randomAlphanumeric(6))
                    .toPath();
    }

}
