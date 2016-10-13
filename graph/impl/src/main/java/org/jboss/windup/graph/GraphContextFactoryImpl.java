package org.jboss.windup.graph;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.windup.util.ExecutionStatistics;

public class GraphContextFactoryImpl implements GraphContextFactory
{
    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;
    private Furnace furnace;
    private GraphTypeManager graphTypeManager;

    private Furnace getFurnace()
    {
        if (furnace == null)
            this.furnace = SimpleContainer.getFurnace(GraphContextFactory.class.getClassLoader());
        return this.furnace;
    }

    private GraphApiCompositeClassLoaderProvider getGraphApiCompositeClassLoaderProvider()
    {
        if (this.graphApiCompositeClassLoaderProvider == null)
            this.graphApiCompositeClassLoaderProvider = getFurnace().getAddonRegistry().getServices(GraphApiCompositeClassLoaderProvider.class)
                        .get();
        return this.graphApiCompositeClassLoaderProvider;
    }

    private GraphTypeManager getGraphTypeManager()
    {
        if (this.graphTypeManager == null)
            this.graphTypeManager = getFurnace().getAddonRegistry().getServices(GraphTypeManager.class).get();

        return this.graphTypeManager;
    }

    @Override
    public GraphContext create()
    {
        return ExecutionStatistics.performBenchmarked(GraphContextFactory.class.getName() + ".create(Path)", () ->
            new GraphContextImpl(
                    getFurnace(),
                    getGraphTypeManager(),
                    getGraphApiCompositeClassLoaderProvider(),
                    getTempGraphDirectory()).create()
        );
    }

    @Override
    public GraphContext create(Path graphDir)
    {
        return ExecutionStatistics.performBenchmarked(GraphContextFactory.class.getName() + ".create(Path)", () ->
            new GraphContextImpl(
                  getFurnace(),
                  getGraphTypeManager(),
                  getGraphApiCompositeClassLoaderProvider(),
                  graphDir).create()
        );
    }

    @Override
    public GraphContext load(Path graphDir)
    {
        return new GraphContextImpl(
                    getFurnace(),
                    getGraphTypeManager(),
                    getGraphApiCompositeClassLoaderProvider(),
                    graphDir).load();
    }

    private Path getTempGraphDirectory()
    {
        return new File(FileUtils.getTempDirectory(), "windupgraph_" + RandomStringUtils.randomAlphanumeric(6))
                    .toPath();
    }

}
