package org.jboss.windup.config.operation.ruleelement;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

public abstract class AbstractIterationOperation<T extends WindupVertexFrame> extends GraphOperation
{
    Class<T> clazz;
    private String variableName;

    public AbstractIterationOperation(Class<T> clazz, String variableName)
    {
        this(clazz);
        this.variableName = variableName;
    }
    
    /**
     * If the variable name is not specified, the default name is taken
     * @param clazz
     */
    public AbstractIterationOperation(Class<T> clazz)
    {
        this.clazz = clazz;
        this.variableName = Iteration.DEFAULT_SINGLE_VARIABLE_STRING;
    }

    protected String getVariableName()
    {
        return variableName;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        Variables varStack = Variables.instance(event);
        T payload = Iteration.getCurrentPayload(varStack, clazz, getVariableName());
        perform(event, context, payload);
    }

    public abstract void perform(GraphRewrite event, EvaluationContext context, T payload);
}
