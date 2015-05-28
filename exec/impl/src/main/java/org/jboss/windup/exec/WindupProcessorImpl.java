package org.jboss.windup.exec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.PreRulesetEvaluation;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.ExcludeTagsOption;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;
import org.jboss.windup.exec.configuration.options.SourceOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.exec.rulefilters.AndPredicate;
import org.jboss.windup.exec.rulefilters.SourceAndTargetPredicate;
import org.jboss.windup.exec.rulefilters.TaggedRuleProviderPredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.Checks;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.config.RuleVisit;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.util.Visitor;

/**
 * Loads and executes the Rules from RuleProviders according to given WindupConfiguration.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>, ozizka@redhat.com
 */
public class WindupProcessorImpl implements WindupProcessor
{
    private static Logger LOG = Logging.get(WindupProcessorImpl.class);

    @Inject
    private RuleLoader ruleLoader;

    @Inject
    private Imported<RuleLifecycleListener> listeners;

    @Override
    public void execute()
    {
        execute(new WindupConfiguration());
    }

    @Override
    public void execute(WindupConfiguration config)
    {
        long startTime = System.currentTimeMillis();

        validateConfig(config);

        GraphContext context = config.getGraphContext();
        context.setOptions(config.getOptionMap());

        WindupConfigurationModel configModel = WindupConfigurationService.getConfigurationModel(context);
        configModel.setInputPath(getFileModel(context, config.getInputPath()));
        configModel.setOutputPath(getFileModel(context, config.getOutputDirectory()));
        configModel.setOfflineMode(config.isOffline());
        for (Path path : config.getAllUserRulesDirectories())
        {
            System.out.println("Using user rules dir: " + path);
            if (path == null)
            {
                throw new WindupException("Null path found (all paths are: "
                            + config.getAllUserRulesDirectories() + ")");
            }
            configModel.addUserRulesPath(getFileModel(context, path));
        }

        for (Path path : config.getAllIgnoreDirectories())
        {
            configModel.addUserIgnorePath(getFileModel(context, path));
        }

        configureRuleProviderAndTagFilters(config);

        RuleProviderRegistry providerRegistry =
                    ruleLoader.loadConfiguration(context, config.getRuleProviderFilter());
        Configuration rules = providerRegistry.getConfiguration();

        List<RuleLifecycleListener> listeners = new ArrayList<>();
        for (RuleLifecycleListener listener : this.listeners)
        {
            listeners.add(listener);
        }
        if (config.getProgressMonitor() != null)
            listeners.add(new DefaultRuleLifecycleListener(config.getProgressMonitor(), rules));

        final GraphRewrite event = new GraphRewrite(listeners, context);
        event.getRewriteContext().put(RuleProviderRegistry.class, providerRegistry);

        RuleSubset ruleSubset = RuleSubset.create(rules);

        for (RuleLifecycleListener listener : listeners)
        {
            ruleSubset.addLifecycleListener(listener);
        }

        new RuleVisit(ruleSubset).accept(new Visitor<Rule>()
        {
            @Override
            public void visit(Rule r)
            {
                if (r instanceof PreRulesetEvaluation)
                {
                    ((PreRulesetEvaluation) r).preRulesetEvaluation(event);
                }
            }
        });

        ruleSubset.perform(event, createEvaluationContext());

        long endTime = System.currentTimeMillis();
        long seconds = (endTime - startTime) / 1000L;
        LOG.info("Windup execution took " + seconds + " seconds to execute on input: "
                    + config.getInputPath() + "!");

        ExecutionStatistics.get().reset();
    }

    @SuppressWarnings("unchecked")
    private void configureRuleProviderAndTagFilters(WindupConfiguration config)
    {
        Collection<String> includeTags = (Collection<String>) config.getOptionMap().get(IncludeTagsOption.NAME);
        Collection<String> excludeTags = (Collection<String>) config.getOptionMap().get(ExcludeTagsOption.NAME);
        Collection<String> sources = (Collection<String>) config.getOptionMap().get(SourceOption.NAME);
        Collection<String> targets = (Collection<String>) config.getOptionMap().get(TargetOption.NAME);
        if (includeTags != null || excludeTags != null || sources != null || targets != null)
        {
            Predicate<RuleProvider> configuredPredicate = config.getRuleProviderFilter();

            final TaggedRuleProviderPredicate tagPredicate = new TaggedRuleProviderPredicate(includeTags, excludeTags);
            final SourceAndTargetPredicate sourceAndTargetPredicate = new SourceAndTargetPredicate(sources, targets);

            Predicate<RuleProvider> providerFilter = new AndPredicate(tagPredicate, sourceAndTargetPredicate);
            if (configuredPredicate != null)
            {
                providerFilter = new AndPredicate(configuredPredicate, tagPredicate, sourceAndTargetPredicate);
            }

            config.setRuleProviderFilter(providerFilter);
        }
    }

    private FileModel getFileModel(GraphContext context, Path path)
    {
        return new FileService(context).createByFilePath(path.toString());
    }

    private EvaluationContext createEvaluationContext()
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

    private void validateConfig(WindupConfiguration windupConfiguration)
    {
        Assert.notNull(windupConfiguration,
                    "Windup configuration must not be null. (Call default execution if no configuration is required.)");

        GraphContext context = windupConfiguration.getGraphContext();
        Assert.notNull(context, "Windup GraphContext must not be null!");

        Path inputPath = windupConfiguration.getInputPath();
        Assert.notNull(inputPath, "Path to the application must not be null!");
        Checks.checkFileOrDirectoryToBeRead(inputPath.toFile(), "Application");

        Path outputDirectory = windupConfiguration.getOutputDirectory();
        Assert.notNull(outputDirectory, "Output directory must not be null!");
        Checks.checkDirectoryToBeFilled(outputDirectory.toFile(), "Output directory");
    }

}
