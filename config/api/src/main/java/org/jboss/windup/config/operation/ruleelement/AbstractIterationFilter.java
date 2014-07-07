package org.jboss.windup.config.operation.ruleelement;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.runner.VarStack;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

public abstract class AbstractIterationFilter<T extends WindupVertexFrame> extends GraphCondition
{
    Class<T> clazz;
    private final String variableName;

    public AbstractIterationFilter(Class<T> clazz, String variableName)
    {
        this.clazz = clazz;
        this.variableName = variableName;
    }

    protected String getVariableName()
    {
        return variableName;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        VarStack varStack = VarStack.instance(event);
        T payload = Iteration.getCurrentPayload(varStack, clazz, getVariableName());
        return evaluate(event, context, payload);
    }

    public abstract boolean evaluate(GraphRewrite event, EvaluationContext context, T payload);

}
