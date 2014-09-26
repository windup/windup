package org.jboss.windup.config.operation.ruleelement;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

public abstract class AbstractIterationFilter<T extends WindupVertexFrame> extends GraphCondition
{
    Class<T> clazz;

    public AbstractIterationFilter(String variableName)
    {
        this.setInputVariablesName(variableName);
    }

    /**
     * If the variable name is not specified, the iteration will set it.
     * 
     * @param clazz
     */
    public AbstractIterationFilter()
    {
    }

    public boolean hasVariableNameSet()
    {
        return getInputVariablesName() != null;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        checkVariableName(event, context);
        Variables varStack = Variables.instance(event);
        T payload = Iteration.getCurrentPayload(varStack, clazz, getInputVariablesName());
        return evaluate(event, context, payload);
    }

    /**
     * Check the variable name and if not set, set it with the singleton variable being on the top of the stack.
     */
    protected void checkVariableName(GraphRewrite event, EvaluationContext context)
    {
        if (getInputVariablesName() == null)
        {
            setInputVariablesName(Iteration.getPayloadVariableName(event, context));
        }
    }

    public abstract boolean evaluate(GraphRewrite event, EvaluationContext context, T payload);

}
