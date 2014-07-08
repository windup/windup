package org.jboss.windup.graph;

import java.io.File;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;

@Singleton
public class GraphContextFactoryImpl implements GraphContextFactory
{
    @Inject
    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;

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
        return new GraphContextImpl(runDirectory,
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
        return new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
    }

}
