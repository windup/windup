package org.jboss.windup.exec;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.loader.GraphConfigurationLoader;
import org.jboss.windup.engine.WindupConfiguration;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
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
    private GraphContextFactory factory;

    @Inject
    private GraphConfigurationLoader graphConfigurationLoader;

    @Override
    public void execute()
    {
        execute(new WindupConfiguration());
    }

    @Override
    public void execute(WindupConfiguration windupConfiguration)
    {
        Assert.notNull(windupConfiguration,
                    "Windup configuration must not be null. (Call default execution if no configuration is required.)");

        GraphContext context = windupConfiguration.getGraphContext();
        if (context == null)
        {
            Path outputDirectory = windupConfiguration.getOutputDirectory();
            if (outputDirectory != null)
            {
                Path graphDir = outputDirectory.resolve("graph");
                context = factory.create(graphDir);
            }
            else
            {
                context = factory.create();
            }
        }

        if (null != windupConfiguration.getGraphListener())
            windupConfiguration.getGraphListener().postOpen(context);

        Configuration rules = graphConfigurationLoader.loadConfiguration(context,
                    windupConfiguration.getRuleProviderFilter());

        GraphRewrite event = new GraphRewrite(context);

        RuleSubset ruleSubset = RuleSubset.create(rules);
        if (windupConfiguration.getProgressMonitor() != null)
            ruleSubset.addLifecycleListener(new DefaultRuleLifecycleListener(windupConfiguration.getProgressMonitor(),
                        rules));
        ruleSubset.perform(event, createEvaluationContext());

        if (null != windupConfiguration.getGraphListener())
            windupConfiguration.getGraphListener().preShutdown(context);
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
