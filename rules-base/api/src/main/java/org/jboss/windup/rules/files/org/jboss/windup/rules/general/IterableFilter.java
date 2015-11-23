package org.jboss.windup.rules.files.org.jboss.windup.rules.general;

import com.google.common.collect.Iterables;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Created by mbriskar on 11/19/15.
 */
public class IterableFilter extends GraphCondition
{
    private GraphCondition wrappedCondition;
    private Integer size;

    public static IterableFilter withSize(int size) {
        return new IterableFilter(size);
    }

    public IterableFilter(int size) {
        this.size=size;
    }

    public void withWrappedCondition(GraphCondition condition)
    {
        this.wrappedCondition=condition;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        wrappedCondition.evaluate(event,context);
        Iterable<? extends WindupVertexFrame> vertices= Variables.instance(event).findVariable(wrappedCondition.getOutputVariablesName());
        if(Iterables.size(vertices) == size) {
            return true;
        } else {
            return false;
        }
    }
}
