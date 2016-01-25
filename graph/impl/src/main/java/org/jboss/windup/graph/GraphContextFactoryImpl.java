package org.jboss.windup.graph;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;

public class GraphContextFactoryImpl implements GraphContextFactory
{
    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;

    private Furnace furnace;

    private GraphTypeManager graphTypeManager;

    public GraphContextFactoryImpl() throws Exception
    {
    }

    //@PostConstruct doesn't work with SimpleContainer.
    public void init(){
        if (this.furnace != null)
            return;
        this.furnace = SimpleContainer.getFurnace(GraphContextFactory.class.getClassLoader());
        this.graphApiCompositeClassLoaderProvider = furnace.getAddonRegistry().getServices(GraphApiCompositeClassLoaderProvider.class).get();
        this.graphTypeManager = furnace.getAddonRegistry().getServices(GraphTypeManager.class).get();
    }

    @Override
    public GraphContext create()
    {
        init();
        return new GraphContextImpl(
                    furnace,
                    graphTypeManager,
                    graphApiCompositeClassLoaderProvider,
                    getTempGraphDirectory()).create();
    }

    @Override
    public GraphContext create(Path graphDir)
    {
        init();
        return new GraphContextImpl(
                    furnace,
                    graphTypeManager,
                    graphApiCompositeClassLoaderProvider,
                    graphDir).create();
    }

    @Override
    public GraphContext load(Path graphDir)
    {
        init();
        return new GraphContextImpl(
                    furnace,
                    graphTypeManager,
                    graphApiCompositeClassLoaderProvider,
                    graphDir).load();
    }

    private Path getTempGraphDirectory()
    {
        return new File(FileUtils.getTempDirectory(), "windupgraph_" + RandomStringUtils.randomAlphanumeric(6))
                    .toPath();
    }

}
