package org.jboss.windup.exec;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.loader.GraphConfigurationLoader;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.engine.WindupProgressMonitor;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * The entry point of the engine.
 */
public class WindupProcessorImpl implements WindupProcessor
{

    @Inject
    private GraphContext graphContext;

    @Inject
    private GraphConfigurationLoader graphConfigurationLoader;

    @Override
    public void setOutputDirectory(Path outputDirectory)
    {
        Path graphDirectory = outputDirectory.resolve("graph");
        graphContext.setGraphDirectory(graphDirectory);
    }

    @Override
    public void execute()
    {
        execute(new NullWindupProgressMonitor());
    }

    @Override
    public void execute(Predicate<WindupRuleProvider> ruleProviderFilter)
    {
        execute(ruleProviderFilter, new NullWindupProgressMonitor());
    }

    @Override
    public void execute(final WindupProgressMonitor progressMonitor)
    {
        final GraphContext context = graphContext;
        final Configuration configuration = graphConfigurationLoader.loadConfiguration(context);
        GraphRewrite event = new GraphRewrite(context);

        RuleSubset ruleSubset = RuleSubset.create(configuration);
        ruleSubset.addLifecycleListener(new RuleLifecycleListener()
        {
            @Override
            public void beforeExecution()
            {
                progressMonitor.beginTask("Executing Rules: ", configuration.getRules().size());
            }

            @Override
            public void beforeRuleEvaluation(Rule rule, EvaluationContext context)
            {
                progressMonitor.subTask(prettyPrintRule(rule));

            }

            @Override
            public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule,
                        boolean result)
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
            public void afterExecution()
            {
                progressMonitor.done();
            }
        });
        ruleSubset.perform(event, createEvaluationContext());
    }

    /**
     * Only runs certain rule providers - based on given filter.
     */
    @Override
    public void execute(Predicate<WindupRuleProvider> ruleProviderFilter, WindupProgressMonitor progressMonitor)
    {
        final GraphContext context = graphContext;
        final Predicate<WindupRuleProvider> ruleProviderFilter1 = ruleProviderFilter;
        final Configuration configuration = graphConfigurationLoader.loadConfiguration(context, ruleProviderFilter1);
        GraphRewrite event = new GraphRewrite(context);
        RuleSubset.create(configuration).perform(event, createEvaluationContext());
    }

    private DefaultEvaluationContext createEvaluationContext()
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

    private String prettyPrintRule(Rule rule)
    {
        StringBuilder builder = new StringBuilder();
        if (rule instanceof Context)
        {
            WindupRuleProvider ruleProvider = (WindupRuleProvider) ((Context) rule)
                        .get(RuleMetadata.RULE_PROVIDER);

            String category = (String) ((Context) rule).get(RuleMetadata.CATEGORY);

            if (ruleProvider != null)
            {
                builder.append(ruleProvider.getPhase() + " - ");
                builder.append(ruleProvider.getID() + " ");
            }

            if (category != null)
                builder.append("[" + category + "] ");

        }

        return builder.append(rule.getId()).toString();
    }

    /*
     * Null object pattern, presents a no-op implementation to avoid adding cyclomatic complexity to underlying
     * implementation.
     */
    private static class NullWindupProgressMonitor implements WindupProgressMonitor
    {
        @Override
        public void beginTask(String name, int totalWork)
        {
        }

        @Override
        public void done()
        {
        }

        @Override
        public boolean isCancelled()
        {
            return false;
        }

        @Override
        public void setCancelled(boolean value)
        {
        }

        @Override
        public void setTaskName(String name)
        {
        }

        @Override
        public void subTask(String name)
        {
        }

        @Override
        public void worked(int work)
        {
        }
    }
}
