package org.jboss.windup.exec;

import org.jboss.windup.engine.WindupProcessorConfig;
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
    public void execute( WindupProcessorConfig config )
    {
        if( config.getOutputDirectory() != null ){
            Path graphDir = config.getOutputDirectory().resolve("graph");
            this.graphContext.setGraphDirectory(graphDir);
        }
        
        final Configuration ocpConfig;
        if( config.getRuleProviderFilter() != null )
            ocpConfig = graphConfigurationLoader.loadConfiguration(this.graphContext, config.getRuleProviderFilter());
        else 
            ocpConfig = graphConfigurationLoader.loadConfiguration(this.graphContext);
        
        GraphRewrite event = new GraphRewrite(this.graphContext);
        
        final RuleSubset ruleSubset = RuleSubset.create(ocpConfig);

        if( config.getProgressMonitor() != null )
            ruleSubset.addLifecycleListener(new DefaultRuleLifecycleListener(config.getProgressMonitor(), ocpConfig));
        
        ruleSubset.perform(event, createEvaluationContext());
    }

    
    
    // Convenience/deprecated methods.
    
    @Override
    public void execute()
    {
        execute(new WindupProcessorConfig());
    }

    @Override
    public void execute(Predicate<WindupRuleProvider> ruleProviderFilter)
    {
        WindupProcessorConfig conf = new WindupProcessorConfig().setRuleProviderFilter(ruleProviderFilter);
        execute(conf);
    }

    @Override
    public void execute(final WindupProgressMonitor monitor)
    {
        WindupProcessorConfig conf = new WindupProcessorConfig().setProgressMonitor(monitor);
        this.execute(conf);
    }

    /**
     * Only runs certain rule providers - based on given filter.
     */
    @Override
    public void execute(Predicate<WindupRuleProvider> ruleProviderFilter, WindupProgressMonitor monitor)
    {
        WindupProcessorConfig conf = new WindupProcessorConfig()
                .setRuleProviderFilter(ruleProviderFilter)
                .setProgressMonitor(monitor);
        this.execute(conf);
    }

    private DefaultEvaluationContext createEvaluationContext()
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

}
