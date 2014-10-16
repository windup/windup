package org.jboss.windup.config;

import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Receives events from {@link RuleSubset} during execution.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RuleLifecycleListener
{
    /**
     * Called immediately before any {@link Rule} instances are executed.
     */
    public void beforeExecution(GraphRewrite event);

    /**
     * Called immediately before the given {@link Rule} is executed.
     */
    public void beforeRuleEvaluation(GraphRewrite event, Rule rule, EvaluationContext context);

    /**
     * Called immediately after execution of the each {@link Rule}.
     */
    public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule, boolean result);

    /**
     * Called immediately before {@link Rule} operations are performed (Only called if
     * {@link Rule#evaluate(org.ocpsoft.rewrite.event.Rewrite, EvaluationContext)} returned <code>true</code>).
     */
    public void beforeRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule);

    /**
     * Called immediately after {@link Rule} operations are performed (Only called if
     * {@link Rule#evaluate(org.ocpsoft.rewrite.event.Rewrite, EvaluationContext)} returned <code>true</code>).
     */
    public void afterRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule);

    /**
     * Called immediately after a a {@link Rule} has thrown an exception, to indicate a failure of some kind
     */
    public void afterRuleExecutionFailed(GraphRewrite event, EvaluationContext context, Rule rule, Throwable failureCause);

    /**
     * Called immediately after any {@link Rule} instances are executed.
     */
    public void afterExecution(GraphRewrite event);
}
