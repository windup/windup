package org.jboss.windup.config;

import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This provides a simplified way to extend {@link WindupRuleProvider} for cases where the rule simply needs to provide
 * some query, and wants to execute a function over each resulting row.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public abstract class IteratingRuleProvider<PAYLOADTYPE extends WindupVertexFrame> extends WindupRuleProvider
{
    /**
     * Gets the condition for the {@link Configuration}'s "when" clause.
     */
    public abstract ConditionBuilder when();

    /**
     * Perform this function for each {@link WindupVertexFrame} returned by the "when" clause.
     */
    public abstract void perform(GraphRewrite event, EvaluationContext context, PAYLOADTYPE payload);

    private class IterationOperation extends AbstractIterationOperation<PAYLOADTYPE>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, PAYLOADTYPE payload)
        {
            IteratingRuleProvider.this.perform(event, context, payload);
        }

        @Override
        public String toString()
        {
            return IteratingRuleProvider.this.toStringPerform();
        }
    }

    /**
     * This should return a string describing the operation to be performed by the subclass.
     */
    public abstract String toStringPerform();

    @Override
    public final Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(when())
                    .perform(new IterationOperation());
    }
}
