package org.jboss.windup.graph;

import javax.inject.Inject;

import com.tinkerpop.blueprints.Vertex;

public class GraphUtilImpl implements GraphUtil
{
    @Inject
    private GraphContext graphContext;
    
    @Override
    public <T> T castToType(Vertex vertex, Class<T> type)
    {
        return graphContext.getFramed().frame(vertex, type);
    }

}
