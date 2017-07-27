package org.jboss.windup.exec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.KeepWorkDirsOption;
import org.jboss.windup.config.PreRulesetEvaluation;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.SkipReportsRenderingOption;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.metadata.TechnologyReferenceTransformer;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.config.phase.PostReportRenderingPhase;
import org.jboss.windup.config.phase.PreReportGenerationPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.ExcludeTagsOption;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;
import org.jboss.windup.exec.configuration.options.SourceOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.exec.rulefilters.AndPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.exec.rulefilters.SourceAndTargetPredicate;
import org.jboss.windup.exec.rulefilters.TaggedRuleProviderPredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.TechnologyReferenceModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.Checks;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.RuleVisit;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * Loads and executes the Rules from RuleProviders according to given WindupConfiguration.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class WindupProcessorImpl implements WindupProcessor
{
    private static final Logger LOG = Logging.get(WindupProcessorImpl.class);

    @Inject
    private RuleLoader ruleLoader;

    @Inject
    private GraphContextFactory graphContextFactory;

    @Inject
    private Imported<RuleLifecycleListener> listeners;

    @Override
    public void execute()
    {
        execute(new WindupConfiguration());
    }

    @Override
    public void execute(WindupConfiguration configuration)
    {
        long startTime = System.currentTimeMillis();

        validateConfig(configuration);

        boolean autoCloseGraph = false;
        if (configuration.getGraphContext() == null)
        {
            // Since we created it, we should clean it up
            autoCloseGraph = true;
            Path graphPath = configuration.getOutputDirectory().resolve(GraphContextFactory.DEFAULT_GRAPH_SUBDIRECTORY);
            GraphContext graphContext = this.graphContextFactory.create(graphPath);
            configuration.setGraphContext(graphContext);
        }

        try
        {
            printConfigInfo(configuration);

            GraphContext context = configuration.getGraphContext();
            context.setOptions(configuration.getOptionMap());

            WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);

            Set<FileModel> inputPathModels = new LinkedHashSet<>();
            for (Path inputPath : configuration.getInputPaths()) {
                inputPathModels.add(getFileModel(context, inputPath));
            }
            configurationModel.setInputPaths(inputPathModels);

            configurationModel.setOutputPath(getFileModel(context, configuration.getOutputDirectory()));
            configurationModel.setOnlineMode(configuration.isOnline());
            configurationModel.setExportingCSV(configuration.isExportingCSV());
            configurationModel.setKeepWorkDirectories(configuration.getOptionValue(KeepWorkDirsOption.NAME));
            for (Path path : configuration.getAllUserRulesDirectories()) {
                System.out.println("Using user rules dir: " + path);
                if (path == null) {
                    throw new WindupException("Null path found (all paths are: "
                            + configuration.getAllUserRulesDirectories() + ")");
                }
                configurationModel.addUserRulesPath(getFileModel(context, path));
            }

            for (Path path : configuration.getAllIgnoreDirectories()) {
                configurationModel.addUserIgnorePath(getFileModel(context, path));
            }

            List<RuleLifecycleListener> listeners = new ArrayList<>();
            for (RuleLifecycleListener listener : this.listeners) {
                listeners.add(listener);
            }

            final GraphRewrite event = new GraphRewrite(listeners, context);
            RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(event.getRewriteContext(), configuration.getAllUserRulesDirectories(), configuration.getRuleProviderFilter());
            ruleLoaderContext = configureRuleProviderAndTagFilters(ruleLoaderContext, configuration);
            addSourceAndTargetInformation(event, configuration, configurationModel);

            RuleProviderRegistry providerRegistry = ruleLoader.loadConfiguration(ruleLoaderContext);
            Configuration rules = providerRegistry.getConfiguration();

            if (configuration.getProgressMonitor() != null)
                listeners.add(new DefaultRuleLifecycleListener(configuration.getProgressMonitor(), rules));

            event.getRewriteContext().put(RuleProviderRegistry.class, providerRegistry);

            RuleSubset ruleSubset = RuleSubset.create(rules);
            ruleSubset.setAlwaysHaltOnFailure(configuration.isAlwaysHaltOnException());

            for (RuleLifecycleListener listener : listeners) {
                ruleSubset.addLifecycleListener(listener);
            }

            new RuleVisit(ruleSubset).accept((rule) ->
            {
                if (rule instanceof PreRulesetEvaluation)
                    ((PreRulesetEvaluation) rule).preRulesetEvaluation(event);
            });

            ruleSubset.perform(event, createEvaluationContext());

            if (event.getWindupStopException() != null)
            {
                String message = Util.WINDUP_BRAND_NAME_ACRONYM+" was cancelled on request before finishing";
                if (event.getWindupStopException().getMessage() != null)
                    message += ", cause: " + event.getWindupStopException().getMessage();
                LOG.log(Level.INFO, message);
            }
        }
        finally
        {
            if (autoCloseGraph)
            {
                try
                {
                    configuration.getGraphContext().close();
                } catch (Throwable t)
                {
                    LOG.log(Level.WARNING, "Failed to close graph due to: " + t.getMessage(), t);
                }
            }

            long endTime = System.currentTimeMillis();
            long seconds = (endTime - startTime) / 1000L;
            LOG.info(Util.WINDUP_BRAND_NAME_ACRONYM+" execution took " + seconds + " seconds to execute on input: " + configuration.getInputPaths() + "!");

            ExecutionStatistics.get().reset();
        }
    }

    private void addSourceAndTargetInformation(GraphRewrite event, WindupConfiguration configuration, WindupConfigurationModel configurationModel)
    {
        @SuppressWarnings("unchecked")
        Collection<String> sources = (Collection<String>) configuration.getOptionMap().get(SourceOption.NAME);
        @SuppressWarnings("unchecked")
        Collection<String> targets = (Collection<String>) configuration.getOptionMap().get(TargetOption.NAME);

        GraphService<TechnologyReferenceModel> technologyReferenceService = new GraphService<>(event.getGraphContext(), TechnologyReferenceModel.class);
        Function<String, TechnologyReferenceModel> createReferenceModel = (techID) -> {
            TechnologyReference reference = TechnologyReference.parseFromIDAndVersion(techID);
            TechnologyReferenceModel technologyReferenceModel = technologyReferenceService.create();
            technologyReferenceModel.setTechnologyID(reference.getId());
            technologyReferenceModel.setVersionRange(reference.getVersionRangeAsString());
            return technologyReferenceModel;
        };

        if (sources != null)
        {
            sources.forEach((sourceID) -> {
                configurationModel.addSourceTechnology(createReferenceModel.apply(sourceID));
            });
        }

        if (targets != null)
        {
            targets.forEach((targetID) -> {
                configurationModel.addTargetTechnology(createReferenceModel.apply(targetID));
            });
        }
    }

    @SuppressWarnings("unchecked")
    private RuleLoaderContext configureRuleProviderAndTagFilters(RuleLoaderContext ruleLoaderContext, WindupConfiguration config)
    {
        Collection<String> includeTags = (Collection<String>) config.getOptionMap().get(IncludeTagsOption.NAME);
        Collection<String> excludeTags = (Collection<String>) config.getOptionMap().get(ExcludeTagsOption.NAME);
        Collection<String> sources = (Collection<String>) config.getOptionMap().get(SourceOption.NAME);
        Collection<String> targets = (Collection<String>) config.getOptionMap().get(TargetOption.NAME);

        if(includeTags != null && includeTags.isEmpty()) includeTags = null;
        if(excludeTags != null && excludeTags.isEmpty()) excludeTags = null;
        if(sources != null && sources.isEmpty()) sources = null;
        if(targets != null && targets.isEmpty()) targets = null;

        if (includeTags != null || excludeTags != null || sources != null || targets != null)
        {
            Predicate<RuleProvider> configuredPredicate = config.getRuleProviderFilter();

            final TaggedRuleProviderPredicate tagPredicate = new TaggedRuleProviderPredicate(includeTags, excludeTags);

            List<TechnologyReferenceTransformer> transformers = TechnologyReferenceTransformer.getTransformers(ruleLoaderContext);

            /*
             * Create a Map based upon the ID of the original to be transformed.
             */
            Map<String, List<TechnologyReferenceTransformer>> transformerMap = transformers.stream()
                .collect(Collectors.toMap(
                    // This is the Map key
                    transformer -> transformer.getOriginal().getId(),

                    // Create a List for each entry
                    (transformer) -> {
                        List<TechnologyReferenceTransformer> list = new ArrayList<>();
                        list.add(transformer);
                        return list;
                    },

                    // Merge the list if there are multiple entries for the same id
                    (old, latest) -> {
                        old.addAll(latest);
                        return old;
                    })
                );

            // Use the tech transformers to transform the IDs
            Function<String, String> transformTechFunction = (technologyID) -> {
                if (transformerMap.containsKey(technologyID)) {
                    for (TechnologyReferenceTransformer transformer : transformerMap.get(TechnologyReference.parseFromIDAndVersion(technologyID).getId()))
                        technologyID = transformer.transform(technologyID).toString();
                }
                return technologyID;
            };

            sources = (sources == null) ? null : sources.stream().map(transformTechFunction).collect(Collectors.toSet());
            config.setOptionValue(SourceOption.NAME, sources);
            targets = (targets== null) ? null : targets.stream().map(transformTechFunction).collect(Collectors.toSet());
            config.setOptionValue(TargetOption.NAME, targets);

            final SourceAndTargetPredicate sourceAndTargetPredicate = new SourceAndTargetPredicate(sources, targets);

            Predicate<RuleProvider> providerFilter = new AndPredicate(tagPredicate, sourceAndTargetPredicate);
            if (configuredPredicate != null)
                providerFilter = new AndPredicate(configuredPredicate, tagPredicate, sourceAndTargetPredicate);

            LOG.info("RuleProvider filter: " + providerFilter);
            config.setRuleProviderFilter(providerFilter);
        }

        Boolean skipReports = false;
        // if skipReportsRendering option is set filter rules in related phases
        if (config.getOptionMap().containsKey(SkipReportsRenderingOption.NAME))
        {
            skipReports = (Boolean) config.getOptionMap().get(SkipReportsRenderingOption.NAME);
        }
        if (skipReports)
        {
            NotPredicate skipReportsProviderFilter = new NotPredicate(
                        new RuleProviderPhasePredicate(PreReportGenerationPhase.class, ReportGenerationPhase.class,
                                    ReportRenderingPhase.class, PostReportGenerationPhase.class, PostReportRenderingPhase.class));
            Predicate<RuleProvider> configuredProvider = config.getRuleProviderFilter();
            Predicate<RuleProvider> providerFilter = new AndPredicate(skipReportsProviderFilter);
            if (configuredProvider != null)
            {
                providerFilter = new AndPredicate(configuredProvider, skipReportsProviderFilter);
            }

            LOG.info("Adding RuleProvider filter for skipping reports: " + providerFilter);
            config.setRuleProviderFilter(providerFilter);
        }

        return new RuleLoaderContext(ruleLoaderContext.getContext(), ruleLoaderContext.getRulePaths(), config.getRuleProviderFilter());
    }

    private FileModel getFileModel(GraphContext context, Path file)
    {
        return new FileService(context).createByFilePath(file.toString());
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
                    Util.WINDUP_BRAND_NAME_ACRONYM+" configuration must not be null. (Call default execution if no configuration is required.)");

        Collection<Path> inputPaths = windupConfiguration.getInputPaths();
        Assert.notNull(inputPaths, "Path to the application must not be null!");
        for (Path inputPath : inputPaths)
        {
            Assert.notNull(inputPath, "Path to the application must not be null!");
            Checks.checkFileOrDirectoryToBeRead(inputPath.toFile(), "Application");
        }

        Path outputDirectory = windupConfiguration.getOutputDirectory();
        Assert.notNull(outputDirectory, "Output directory must not be null!");
        Checks.checkDirectoryToBeFilled(outputDirectory.toFile(), "Output directory");
    }

    private void printConfigInfo(WindupConfiguration windupConfiguration)
    {
        LOG.info("");
        if (windupConfiguration.getInputPaths().size() == 1)
        {
            LOG.info("Input Application:" + windupConfiguration.getInputPaths().iterator().next());
        }
        else
        {
            LOG.info("Input Applications:");
            for (Path inputPath : windupConfiguration.getInputPaths())
            {
                LOG.info("\t" + inputPath);
            }
            LOG.info("");
        }
        LOG.info("Output Path:" + windupConfiguration.getOutputDirectory());
        LOG.info("");

        for (Map.Entry<String, Object> entrySet : windupConfiguration.getOptionMap().entrySet())
        {
            LOG.info("\t" + entrySet.getKey() + ": " + entrySet.getValue());
        }
    }

}
