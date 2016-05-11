package org.jboss.windup.exec;

import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.enterprise.inject.Vetoed;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jboss.windup.config.AbstractRuleProvider;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@Vetoed
class DefaultRuleLifecycleListener implements RuleLifecycleListener
{

    private final WindupProgressMonitor progressMonitor;
    private final Configuration configuration;
    private String lastRuleProgressMessage = null;

    public DefaultRuleLifecycleListener(WindupProgressMonitor progressMonitor, Configuration configuration)
    {
        this.progressMonitor = progressMonitor;
        this.configuration = configuration;
    }

    @Override
    public void beforeExecution(GraphRewrite event)
    {
        final org.apache.commons.lang3.mutable.MutableInt count = new MutableInt(0);
        // Only count rulesets that are not disabled.
        for (Rule rule : configuration.getRules())
        {
            count.increment();
            Context ruleContext = rule instanceof Context ? (Context) rule : null;
            if (rule == null)
                return;
            AbstractRuleProvider ruleProvider = (AbstractRuleProvider) ruleContext.get(RuleMetadataType.RULE_PROVIDER);
            if (ruleProvider == null)
                return;
            if (ruleProvider.getMetadata().isDisabled())
            count.decrement();
        }
        progressMonitor.beginTask("Executing Windup", count.intValue());
    }

    @Override
    public void beforeRuleEvaluation(GraphRewrite event, Rule rule, EvaluationContext context)
    {
        progressMonitor.subTask(RuleUtils.prettyPrintRule(rule));
    }

    @Override
    public void ruleEvaluationProgress(GraphRewrite event, String name, int currentPosition, int total, int timeRemainingInSeconds)
    {
        String timeRemaining = formatTimeRemaining(timeRemainingInSeconds);
        int percentage = (int) (100 * ((double) currentPosition / (double) total));
        String newProgressMessage = "(" + percentage + "%) " + name + ", Estimated time until next Rule: " + timeRemaining;
        // don't redisplay it if nothing has changed
        if (!newProgressMessage.equals(lastRuleProgressMessage))
        {
            lastRuleProgressMessage = newProgressMessage;
            progressMonitor.subTask(newProgressMessage);
        }
    }

    private String formatTimeRemaining(int timeRemainingInSeconds)
    {
        int hours = timeRemainingInSeconds / 60 / 60;
        int minutes = (timeRemainingInSeconds - (hours * 60 * 60)) / 60;
        int seconds = timeRemainingInSeconds % 60;

        StringBuilder result = new StringBuilder();
        if (hours > 0)
            result.append(hours).append(" hours, ");
        if (hours > 0 || minutes > 0)
            result.append(minutes).append(" minutes, ");
        result.append(seconds).append(" seconds");
        return result.toString();
    }

    @Override
    public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule, boolean result)
    {
        if (result == false)
        {
            progressMonitor.worked(1);
        }
    }

    @Override
    public void beforeRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule)
    {
    }

    @Override
    public void afterRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule)
    {
        progressMonitor.worked(1);
    }

    @Override
    public void afterRuleExecutionFailed(GraphRewrite event, EvaluationContext context, Rule rule, Throwable failureCause)
    {
    }

    @Override
    public void afterExecution(GraphRewrite event)
    {
        progressMonitor.done();
    }

}
