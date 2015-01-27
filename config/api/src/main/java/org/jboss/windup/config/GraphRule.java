package org.jboss.windup.config;

import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * Base class for {@link Rule} implementations that operate on {@link GraphRewrite} events.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class GraphRule implements Rule
{
    /**
     * Evaluate this rule against the given {@link GraphRewrite} event. If this condition does not apply to the given
     * event, it must return <code>false</code>. If the condition applies and is satisfied, return <code>true</code>.
     */
    public abstract boolean evaluate(GraphRewrite event, EvaluationContext context);

    /**
     * Perform the {@link Operation}.
     */
    public abstract void perform(GraphRewrite event, EvaluationContext context);

    @Override
    public final boolean evaluate(Rewrite event, EvaluationContext context)
    {
        if (event instanceof GraphRewrite)
            return evaluate((GraphRewrite) event, context);
        return false;
    }

    @Override
    public final void perform(Rewrite event, EvaluationContext context)
    {
        if (event instanceof GraphRewrite)
            perform((GraphRewrite) event, context);
    }
}
