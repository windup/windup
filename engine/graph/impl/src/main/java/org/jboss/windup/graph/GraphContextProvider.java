package org.jboss.windup.graph;

import java.io.File;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GraphContextProvider
{

    @Inject
    private WindupContext windupContext;

    private GraphContext graphContext;

    @Produces
    public GraphContext produceGraphContext()
    {
        if (graphContext == null)
        {
            graphContext = new GraphContextImpl(new File(windupContext.getRunDirectory(), "windup-graph"));
        }
        return graphContext;
    }

}