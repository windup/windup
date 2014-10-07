package org.jboss.windup.exec;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.loader.WindupRuleLoader;
import org.jboss.windup.config.metadata.WindupRuleMetadata;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileModelService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupProcessorImpl implements WindupProcessor
{
    @Inject
    private WindupRuleLoader windupConfigurationLoader;

    @Inject
    private Imported<RuleLifecycleListener> listeners;

    @Override
    public void execute()
    {
        execute(new WindupConfiguration());
    }

    @Override
    public void execute(WindupConfiguration windupConfiguration)
    {
        Assert.notNull(windupConfiguration, "Windup configuration must not be null. (Call default execution if no configuration is required.)");

        GraphContext context = windupConfiguration.getGraphContext();
        Assert.notNull(context, "Windup GraphContext must not be null!");

        context.setOptions(windupConfiguration.getOptionMap());

        // initialize the basic configuration data in the graph
        WindupConfigurationModel configModel = WindupConfigurationService.getConfigurationModel(context);
        configModel.setInputPath(getFileModel(context, windupConfiguration.getInputPath()));
        configModel.setOutputPath(getFileModel(context, windupConfiguration.getOutputDirectory()));
        configModel.setOfflineMode(windupConfiguration.isOffline());
        if (windupConfiguration.getUserRulesDirectory() != null)
        {
            configModel.setUserRulesPath(getFileModel(context, windupConfiguration.getUserRulesDirectory()));
        }

        GraphRewrite event = new GraphRewrite(context);

        WindupRuleMetadata ruleMetadata = windupConfigurationLoader.loadConfiguration(context, windupConfiguration.getRuleProviderFilter());
        event.getRewriteContext().put(WindupRuleMetadata.class, ruleMetadata);

        Configuration rules = ruleMetadata.getConfiguration();

        RuleSubset ruleSubset = RuleSubset.create(rules);
        if (windupConfiguration.getProgressMonitor() != null)
            ruleSubset.addLifecycleListener(new DefaultRuleLifecycleListener(windupConfiguration.getProgressMonitor(), rules));

        for (RuleLifecycleListener listener : listeners)
        {
            ruleSubset.addLifecycleListener(listener);
        }

        ruleSubset.perform(event, createEvaluationContext());
    }

    private FileModel getFileModel(GraphContext context, Path path)
    {
        return new FileModelService(context).createByFilePath(path.toString());
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
