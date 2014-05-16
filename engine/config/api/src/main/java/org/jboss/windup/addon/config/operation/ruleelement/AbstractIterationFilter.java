package org.jboss.windup.addon.config.operation.ruleelement;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.condition.GraphCondition;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
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
        SelectionFactory selectionFactory = SelectionFactory.instance(event);
        T payload = selectionFactory
                    .getCurrentPayload(clazz, getVariableName());
        return evaluate(event, context, payload);
    }

    public abstract boolean evaluate(GraphRewrite event, EvaluationContext context, T payload);

}
