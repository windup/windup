package org.jboss.windup.graph;

import java.io.File;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;

@Singleton
public class GraphContextProvider
{

    @Inject
    private WindupContext windupContext;
    
    @Inject
    private GraphTypeRegistry graphTypeRegistry;

    private GraphContext graphContext;

    @Produces
    public GraphContext produceGraphContext()
    {
        if (graphContext == null)
        {
            graphContext = new GraphContextImpl(new File(windupContext.getRunDirectory(), "windup-graph"), graphTypeRegistry);
        }
        return graphContext;
    }

}