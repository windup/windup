package org.jboss.windup.rules.general;

import com.google.common.collect.Iterables;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * A {@link GraphCondition} that returns true/false based on the iterable returned from the wrapped condition. It is useful to check if the result
 * contains the specified number of elements etc.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class IterableFilter extends GraphCondition {
    private GraphCondition wrappedCondition;
    private Integer size;

    public IterableFilter(int size) {
        this.size = size;
    }

    public static IterableFilter withSize(int size) {
        return new IterableFilter(size);
    }

    public IterableFilter withWrappedCondition(GraphCondition condition) {
        this.wrappedCondition = condition;
        return this;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context) {
        wrappedCondition.evaluate(event, context);
        Iterable<? extends WindupVertexFrame> vertices = Variables.instance(event).findVariable(wrappedCondition.getOutputVariablesName());
        if (Iterables.size(vertices) == size) {
            return true;
        } else {
            return false;
        }
    }
}
