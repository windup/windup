package org.jboss.windup.config;

import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This provides a set of default (empty) methods that make it easy to implement {@link RuleLifecycleListener}s that only need to override a subset of
 * the available methods.
 * 
 * @author jsightler
 *
 */
public abstract class AbstractRuleLifecycleListener implements RuleLifecycleListener
{

    @Override
    public void beforeExecution(GraphRewrite event)
    {
    }

    @Override
    public void beforeRuleEvaluation(GraphRewrite event, Rule rule, EvaluationContext context)
    {
    }

    @Override
    public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule, boolean result)
    {
    }

    @Override
    public void beforeRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule)
    {
    }

    @Override
    public void afterRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule)
    {
    }

    @Override
    public void afterRuleExecutionFailed(GraphRewrite event, EvaluationContext context, Rule rule, Throwable failureCause)
    {
    }

    @Override
    public void afterExecution(GraphRewrite event)
    {
    }
}
