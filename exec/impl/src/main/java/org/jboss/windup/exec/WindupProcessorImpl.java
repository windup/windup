package org.jboss.windup.exec;

import org.jboss.windup.graph.GraphContextConfig;
import org.jboss.windup.engine.WindupProcessorConfig;
import java.nio.file.Path;
import javax.inject.Inject;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.loader.GraphConfigurationLoader;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
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
    public void execute(WindupProcessorConfig wpConfig)
    {
        // Graph Context config.
        GraphContextConfig gcConfig = new GraphContextConfig();
        if(wpConfig.getOutputDirectory() != null){
            Path graphDir = wpConfig.getOutputDirectory().resolve("graph");
            gcConfig.setGraphDataDir(graphDir);
        }

        // Initialize the graph explicitely.
        this.graphContext.init(gcConfig);
        
        // Call the listener.
        if(null != wpConfig.getGraphListener())
            wpConfig.getGraphListener().postOpen(this.graphContext);
        
        final Configuration ocpConfig;
        ocpConfig = graphConfigurationLoader.loadConfiguration(this.graphContext, wpConfig.getRuleProviderFilter());
        
        GraphRewrite event = new GraphRewrite(this.graphContext);
        
        final RuleSubset ruleSubset = RuleSubset.create(ocpConfig);

        if(wpConfig.getProgressMonitor() != null)
            ruleSubset.addLifecycleListener(new DefaultRuleLifecycleListener(wpConfig.getProgressMonitor(), ocpConfig));
        
        ruleSubset.perform(event, createEvaluationContext());
        
        // Call the listener
        if(null != wpConfig.getGraphListener())
            wpConfig.getGraphListener().preShutdown(this.graphContext);
    }

    
    
    // Convenience/deprecated methods.
    
    @Override
    public void execute()
    {
        execute(new WindupProcessorConfig());
    }
    
    
    /**
     * Creates EvaluationContext which serves as an environment and input for RuleSubset executor.
     */
    private EvaluationContext createEvaluationContext()
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

}
