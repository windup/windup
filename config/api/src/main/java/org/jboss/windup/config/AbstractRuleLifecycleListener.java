package org.jboss.windup.config;

import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This provides a set of default (empty) methods that make it easy to implement {@link RuleLifecycleListener}s that only need to override a subset of
 * the available methods.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public abstract class AbstractRuleLifecycleListener implements RuleLifecycleListener {
    @Override
    public void beforeExecution(GraphRewrite event) {
    }

    @Override
    public boolean beforeRuleEvaluation(GraphRewrite event, Rule rule, EvaluationContext context) {
        // Execution was not cancelled.
        return false;
    }

    @Override
    public boolean ruleEvaluationProgress(GraphRewrite event, String name, int currentPosition, int total, int timeRemainingInSeconds) {
        // Execution was not cancelled.
        return false;
    }

    @Override
    public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule, boolean result) {
    }

    @Override
    public boolean beforeRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule) {
        // Execution was not cancelled.
        return false;
    }

    @Override
    public void afterRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule) {
    }

    @Override
    public void afterRuleExecutionFailed(GraphRewrite event, EvaluationContext context, Rule rule, Throwable failureCause) {
    }

    @Override
    public void afterExecution(GraphRewrite event) {
    }

    @Override
    public boolean shouldWindupStop() {
        return false;
    }
}
