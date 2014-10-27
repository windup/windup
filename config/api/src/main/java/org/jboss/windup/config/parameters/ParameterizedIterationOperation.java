package org.jboss.windup.config.parameters;

import java.util.Map;
import java.util.Map.Entry;

import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.context.RewriteState;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.util.ParameterUtils;

public abstract class ParameterizedIterationOperation<T extends WindupVertexFrame> extends AbstractIterationOperation<T>
            implements Operation, Parameterized
{
    private WindupVertexFrame originalPayload;

    public ParameterizedIterationOperation()
    {
    }

    public ParameterizedIterationOperation(String variableName)
    {
        super(variableName);
    }

    public abstract void performParameterized(GraphRewrite event, EvaluationContext context, T payload);

    @Override
    public final void perform(GraphRewrite event, EvaluationContext context)
    {
        checkVariableName(event, context);
        WindupVertexFrame payload = resolveVariable(event, getVariableName());
        this.originalPayload = payload;
        try
        {
            super.perform(event, context);
        }
        finally
        {
            this.originalPayload = null;
        }
    }

    @Override
    public final void perform(GraphRewrite event, EvaluationContext context, T payload)
    {
        Map<WindupVertexFrame, ParameterValueStore> stores = ParameterizedGraphCondition
                    .getResultValueStoreMap(context);

        ParameterValueStore originalValueStore = DefaultParameterValueStore.getInstance(context);
        ParameterStore parameterStore = DefaultParameterStore.getInstance(context);

        try
        {
            DefaultEvaluationContext tempEvaluationContext = new DefaultEvaluationContext(context);
            tempEvaluationContext.setState(RewriteState.PERFORMING);
            ParameterValueStore valueStore = stores.get(originalPayload);
            for (Entry<String, Parameter<?>> entry : parameterStore)
            {
                Parameter<?> parameter = entry.getValue();
                String value = valueStore.retrieve(parameter);
                ParameterUtils.enqueueSubmission(event, tempEvaluationContext, parameter, value);
            }
            context.put(ParameterValueStore.class, valueStore);
            performParameterized(event, tempEvaluationContext, payload);
        }
        finally
        {
            context.put(ParameterValueStore.class, originalValueStore);
        }
    }
}
