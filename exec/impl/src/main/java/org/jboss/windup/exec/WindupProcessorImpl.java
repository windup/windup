package org.jboss.windup.exec;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.loader.GraphConfigurationLoader;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.engine.WindupProgressMonitor;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
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
        ruleSubset.addLifecycleListener(new DefaultRuleLifecycleListener(progressMonitor, configuration));
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

}
