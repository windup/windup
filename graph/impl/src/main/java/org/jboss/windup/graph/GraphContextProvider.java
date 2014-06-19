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
public class GraphContextProvider
{
    @Inject
    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;

    @Inject
    private GraphTypeRegistry graphTypeRegistry;

    private GraphContext graphContext;

    @Produces
    @ApplicationScoped
    public GraphContext produceGraphContext()
    {
        if (this.graphContext == null)
        {
            this.graphContext = new GraphContextImpl(
                    new File(this.getRunDirectory(), "windup-graph"),
                    this.graphTypeRegistry,
                    this.graphApiCompositeClassLoaderProvider);
        }
        return graphContext;
    }

    private File getRunDirectory()
    {
        return new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
    }

}
