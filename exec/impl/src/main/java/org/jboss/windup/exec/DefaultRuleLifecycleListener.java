package org.jboss.windup.exec;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.engine.WindupProgressMonitor;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
class DefaultRuleLifecycleListener implements RuleLifecycleListener {
    
    private final WindupProgressMonitor progressMonitor;
    private final Configuration configuration;

    public DefaultRuleLifecycleListener(WindupProgressMonitor progressMonitor, Configuration configuration)
    {
        this.progressMonitor = progressMonitor;
        this.configuration = configuration;
    }


    @Override
    public void beforeExecution()
    {
        progressMonitor.beginTask("Executing Rules: ", configuration.getRules().size());
    }


    @Override
    public void beforeRuleEvaluation(Rule rule, EvaluationContext context)
    {
        progressMonitor.subTask(RuleUtils.prettyPrintRule(rule));
    }


    @Override
    public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule, boolean result)
    {
        if( result == false )
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
    public void afterExecution()
    {
        progressMonitor.done();
    }

}
