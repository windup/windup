package org.jboss.windup.config.operation.ruleelement;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class TypeOperation extends AbstractIterationOperator<WindupVertexFrame>
{
    private Class<? extends WindupVertexFrame> newType;

    private TypeOperation(String variableName, Class<? extends WindupVertexFrame> newType)
    {
        super(WindupVertexFrame.class, variableName);
        this.newType = newType;
    }

    public static GraphOperation addType(String variableName,
                Class<? extends WindupVertexFrame> newType)
    {
        return new TypeOperation(variableName, newType);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload)
    {
        VarStack varStack = VarStack.instance(event);
        WindupVertexFrame newFrame = GraphUtil.addTypeToModel(event.getGraphContext(), payload, newType);
        varStack.setCurrentPayload(getVariableName(), newFrame);
    }
}
