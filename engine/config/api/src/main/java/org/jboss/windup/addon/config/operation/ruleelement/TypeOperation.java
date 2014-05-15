package org.jboss.windup.addon.config.operation.ruleelement;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class TypeOperation extends GraphOperation
{
    private String variableName;
    private Class<? extends WindupVertexFrame> newType;

    private TypeOperation(String variableName, Class<? extends WindupVertexFrame> newType)
    {
        this.variableName = variableName;
        this.newType = newType;
    }

    public static GraphOperation addType(String variableName,
                Class<? extends WindupVertexFrame> newType)
    {
        return new TypeOperation(variableName, newType);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        SelectionFactory selectionFactory = SelectionFactory.instance(event);
        WindupVertexFrame currentFrame = selectionFactory
                    .getCurrentPayload(WindupVertexFrame.class, variableName);
        WindupVertexFrame newFrame = GraphUtil.addTypeToModel(event.getGraphContext(), currentFrame, newType);
        selectionFactory.setCurrentPayload(variableName, newFrame);
    }
}
