package org.jboss.windup.addon.config.operation.ruleelement;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

public abstract class AbstractIterationRuleElement<T extends WindupVertexFrame> extends GraphOperation
{
    Class<T> clazz;
    private String variableName;

    public AbstractIterationRuleElement(Class<T> clazz, String variableName)
    {
        this.clazz = clazz;
        this.variableName = variableName;
    }

    protected String getVariableName()
    {
        return variableName;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        SelectionFactory selectionFactory = SelectionFactory.instance(event);
        T payload = selectionFactory
                    .getCurrentPayload(clazz, getVariableName());
        perform(event, context, payload);
    }

    public abstract void perform(GraphRewrite event, EvaluationContext context, T payload);
}
