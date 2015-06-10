package org.jboss.windup.config.condition;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters the results from the previous conditions, or from the specified vertices
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public abstract class GraphConditionFilter<T extends WindupVertexFrame> extends GraphCondition
{
    Class<T> clazz;

    public GraphConditionFilter(String variableName)
    {
        this.setInputVariablesName(variableName);
    }

    public GraphConditionFilter()
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
        Iterable<? extends T> payloads;
        // 1) get the input variables
        if(getInputVariablesName() == null) {
            //if there is no input variable specified, we need fill vertices before filtering
            payloads=fillIn(event,context);
        } else {
            Variables varStack = Variables.instance(event);
            payloads = Iteration.getCurrentIterationPayload(varStack, getInputVariablesName());
        }

        // 2) do the filtering
        List<WindupVertexFrame> result = new ArrayList<>();
        for(T payload : payloads) {
            if(accept(event, context, payload)) {
                result.add(payload);
            }
        }
        Variables.instance(event).setVariable(getOutputVariablesName(), result);
        return !result.isEmpty();
    }

    /**
     * Check the variable name and if not set and there is some default result from the previous condition, set it to it
     */
    protected void checkVariableName(GraphRewrite event, EvaluationContext context)
    {
        if (getInputVariablesName() == null && !Variables.instance(event).peek().isEmpty())
        {
            String topLayerName = Iteration.getPayloadVariableName(event, context);
            if (topLayerName.equals(Iteration.DEFAULT_VARIABLE_LIST_STRING))
            {
                setInputVariablesName(Iteration.DEFAULT_VARIABLE_LIST_STRING);
            }

        }
    }

    /**
     * In case the input variable is not specified, this method is called to return WindupVertexFrames that are going to be filtered and by accept() method and then returned
     */
    public abstract Iterable<? extends T> fillIn(GraphRewrite event, EvaluationContext context);
    public abstract boolean accept(GraphRewrite event, EvaluationContext context, T payload);

}