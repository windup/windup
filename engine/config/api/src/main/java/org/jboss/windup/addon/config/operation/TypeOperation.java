package org.jboss.windup.addon.config.operation;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;

public class TypeOperation extends GraphOperation
{
    private Class<? extends WindupVertexFrame> currentType;
    private Class<? extends WindupVertexFrame> newType;

    private TypeOperation(Class<? extends WindupVertexFrame> currentType, Class<? extends WindupVertexFrame> newType)
    {
        this.currentType = currentType;
        this.newType = newType;
    }

    public static GraphOperation addType(Class<? extends WindupVertexFrame> currentType,
                Class<? extends WindupVertexFrame> newType)
    {
        return new TypeOperation(currentType, newType);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        SelectionFactory selectionFactory = SelectionFactory.instance(event);
        WindupVertexFrame currentFrame = selectionFactory
                    .getCurrentPayload(currentType);
        Vertex vertex = currentFrame.asVertex();
        event.getGraphContext().getGraphTypeRegistry().addTypeToElement(newType, vertex);
    }
}
