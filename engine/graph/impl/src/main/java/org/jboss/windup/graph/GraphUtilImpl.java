package org.jboss.windup.graph;

import javax.inject.Inject;

import org.jboss.windup.graph.model.meta.WindupVertexFrame;

import com.tinkerpop.blueprints.Vertex;

public class GraphUtilImpl implements GraphUtil
{
    @Inject
    private GraphContext graphContext;

    @Override
    public <T extends WindupVertexFrame> T addTypeToModel(WindupVertexFrame frame, Class<T> type)
    {
        Vertex vertex = frame.asVertex();
        graphContext.getGraphTypeRegistry().addTypeToElement(type, vertex);
        graphContext.getGraph().commit();
        return graphContext.getFramed().frame(vertex, type);
    }
}
