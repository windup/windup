package org.jboss.windup.exec;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.Predicate;
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
import org.jboss.windup.config.metadata.TechnologyReferenceAliasTranslator;
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
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.exec.rulefilters.SourceAndTargetPredicate;
import org.jboss.windup.exec.rulefilters.TaggedRuleProviderPredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ApplicationModel;
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

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.jboss.windup.config.metadata.TechnologyReferenceAliasTranslator.*;

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
            GraphContext graphContext = this.graphContextFactory.create(graphPath, true);
            configuration.setGraphContext(graphContext);
        }

        try
        {
            printConfigInfo(configuration);

            GraphContext graphContext = configuration.getGraphContext();
            graphContext.setOptions(configuration.getOptionMap());

            WindupConfigurationModel configurationModel = setupWindupConfigurationModel(configuration, graphContext);

            List<RuleLifecycleListener> listeners = new ArrayList<>();
            this.listeners.forEach(listeners::add);

            final GraphRewrite windupContext = new GraphRewrite(listeners, graphContext);
            RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(windupContext.getRewriteContext(), configuration.getAllUserRulesDirectories(), configuration.getRuleProviderFilter());
            ruleLoaderContext = configureRuleProviderAndTagFilters(ruleLoaderContext, configuration);
            saveSourceAndTargetInformation(windupContext, configuration, configurationModel);

            RuleProviderRegistry ruleProviderRegistry = ruleLoader.loadConfiguration(ruleLoaderContext);
            Configuration rulesConfiguration = ruleProviderRegistry.getConfiguration();

            if (configuration.getProgressMonitor() != null)
                listeners.add(new DefaultRuleLifecycleListener(configuration.getProgressMonitor(), rulesConfiguration));

            windupContext.getRewriteContext().put(RuleProviderRegistry.class, ruleProviderRegistry);

            // Create rule subset for this execution
            RuleSubset ruleSubset = RuleSubset.create(rulesConfiguration);
            ruleSubset.setAlwaysHaltOnFailure(configuration.isAlwaysHaltOnException());
            listeners.forEach(ruleSubset::addLifecycleListener);

            // Do preruleset evaluation on the subset if applicable
            new RuleVisit(ruleSubset).accept((rule) -> {
                if (rule instanceof PreRulesetEvaluation)
                    ((PreRulesetEvaluation) rule).preRulesetEvaluation(windupContext);
            });

            ruleSubset.perform(windupContext, createEvaluationContext());

            if (windupContext.getWindupStopException() != null)
            {
                String message = Util.WINDUP_BRAND_NAME_ACRONYM + " was cancelled on request before finishing";
                if (windupContext.getWindupStopException().getMessage() != null)
                    message += ", cause: " + windupContext.getWindupStopException().getMessage();
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
                }
                catch (Throwable t)
                {
                    LOG.log(Level.WARNING, "Failed to close graph due to: " + t.getMessage(), t);
                }
            }

            long endTime = System.currentTimeMillis();
            long seconds = (endTime - startTime) / 1000L;
            LOG.info(Util.WINDUP_BRAND_NAME_ACRONYM + " execution took " + seconds + " seconds to execute on input: " + configuration.getInputPaths()
                        + "!");

            ExecutionStatistics.get().reset();
        }
    }

    private WindupConfigurationModel setupWindupConfigurationModel(WindupConfiguration configuration, GraphContext graphContext) {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(graphContext);

        Set<FileModel> inputPathModels = new LinkedHashSet<>();
        for (Path inputPath : configuration.getInputPaths())
        {
            inputPathModels.add(getFileModel(graphContext, inputPath));
        }
        configurationModel.setInputPaths(inputPathModels);
        List<String> applicationNames = configuration.getInputApplicationNames();
        GraphService<ApplicationModel> applicationModelService = new GraphService<>(graphContext, ApplicationModel.class);
        if (applicationNames == null)
        {
            inputPathModels.forEach(applicationModel -> {
                applicationModelService.addTypeToModel(applicationModel).setApplicationName(applicationModel.getFileName());
            });
        }
        else
        {
            // Convert to a list to allow indexing
            List<FileModel> inputModelsList = new ArrayList<>(inputPathModels);
            for (int i = 0; i < inputModelsList.size(); i++)
            {
                FileModel fileModel = inputModelsList.get(i);
                ApplicationModel applicationModel = applicationModelService.addTypeToModel(fileModel);
                if (i < applicationNames.size())
                    applicationModel.setApplicationName(applicationNames.get(i));
                else
                    applicationModel.setApplicationName(fileModel.getFileName());
            }
        }

        configurationModel.setOutputPath(getFileModel(graphContext, configuration.getOutputDirectory()));
        configurationModel.setOnlineMode(configuration.isOnline());
        configurationModel.setExportingCSV(configuration.isExportingCSV());
        configurationModel.setKeepWorkDirectories(configuration.getOptionValue(KeepWorkDirsOption.NAME));

        addUserRulesDirsToConfig(configuration, graphContext, configurationModel);
        addUserLabelsDirsToConfig(configuration, graphContext, configurationModel);

        configuration.getAllIgnoreDirectories().forEach(dir -> configurationModel.addUserIgnorePath(getFileModel(graphContext, dir)));
        
        return configurationModel;
    }

    private void addUserLabelsDirsToConfig(WindupConfiguration configuration, GraphContext graphContext, WindupConfigurationModel configurationModel) {
        for (Path path : configuration.getAllUserLabelsDirectories())
        {
            System.out.println("Using user labels dir: " + path);
            if (path == null) {
                throw new WindupException(format("Null path found (all paths are: %s)", configuration.getAllUserLabelsDirectories()));
            }
            configurationModel.addUserLabelsPath(getFileModel(graphContext, path));
        }
    }

    private void addUserRulesDirsToConfig(WindupConfiguration configuration, GraphContext graphContext, WindupConfigurationModel configurationModel) {
        for (Path path : configuration.getAllUserRulesDirectories())
        {
            System.out.println("Using user rules dir: " + path);
            if (path == null) {
                throw new WindupException(format("Null path found (all paths are: %s)", configuration.getAllUserRulesDirectories()));
            }
            configurationModel.addUserRulesPath(getFileModel(graphContext, path));
        }
    }

    /**
     * Saves sources and targets information into the graph
     */
    private void saveSourceAndTargetInformation(GraphRewrite event, WindupConfiguration configuration, WindupConfigurationModel configurationModel)
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

        Optional.ofNullable(sources).ifPresent(ss -> ss.forEach(sId -> configurationModel.addSourceTechnology(createReferenceModel.apply(sId))));
        Optional.ofNullable(targets).ifPresent(ts -> ts.forEach(tId -> configurationModel.addTargetTechnology(createReferenceModel.apply(tId))));
    }

    @SuppressWarnings("unchecked")
    private RuleLoaderContext configureRuleProviderAndTagFilters(RuleLoaderContext ruleLoaderContext, WindupConfiguration config)
    {
        Collection<String> includeTags = (Collection<String>) config.getOptionMap().get(IncludeTagsOption.NAME);
        Collection<String> excludeTags = (Collection<String>) config.getOptionMap().get(ExcludeTagsOption.NAME);
        Collection<String> sources = (Collection<String>) config.getOptionMap().get(SourceOption.NAME);
        Collection<String> targets = (Collection<String>) config.getOptionMap().get(TargetOption.NAME);

        if (includeTags != null && includeTags.isEmpty())
            includeTags = null;
        if (excludeTags != null && excludeTags.isEmpty())
            excludeTags = null;
        if (sources != null && sources.isEmpty())
            sources = null;
        if (targets != null && targets.isEmpty())
            targets = null;

        if (includeTags != null || excludeTags != null || sources != null || targets != null)
        {
            Predicate<RuleProvider> configuredPredicate = config.getRuleProviderFilter();

            final TaggedRuleProviderPredicate tagPredicate = new TaggedRuleProviderPredicate(includeTags, excludeTags);

            // Translate technology aliases if available (ie. "eap7" to "eap:7")
            Function<String, String> translateTechAliasFn = translateTechnologyAliasFn(ruleLoaderContext);
            sources = Optional.ofNullable(sources).map(ss -> ss.stream().map(translateTechAliasFn).collect(toSet())).orElse(null);
            targets = Optional.ofNullable(targets).map(ts -> ts.stream().map(translateTechAliasFn).collect(toSet())).orElse(null);
            
            config.setOptionValue(SourceOption.NAME, sources);
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

    private static Function<String, String> translateTechnologyAliasFn(RuleLoaderContext ruleLoaderContext) {
        List<TechnologyReferenceAliasTranslator> translators = getTranslators(ruleLoaderContext);

        /*
         * Create a Map of (key:sourceTechId, value:translatorsForThatTech)
         */
        final Map<String, List<TechnologyReferenceAliasTranslator>> translatorMap = translators.stream()
                    .collect(toMap(
                                // This is the Map key
                                transformer -> transformer.getOriginalTechnology().getId(),

                                // Create a List for each entry
                                (transformer) -> {
                                    List<TechnologyReferenceAliasTranslator> list = new ArrayList<>();
                                    list.add(transformer);
                                    return list;
                                },

                                // Merge the list if there are multiple entries for the same id
                                (old, latest) -> {
                                    old.addAll(latest);
                                    return old;
                                })
                    );

        // Use the tech translators to translate the IDs
        Function<String, String> translateTechFunc = (sourceTechIdAndVersion) -> {
            if (translatorMap.containsKey(sourceTechIdAndVersion)) 
            {
                String id = TechnologyReference.parseFromIDAndVersion(sourceTechIdAndVersion).getId();
                for (TechnologyReferenceAliasTranslator translator : translatorMap.get(id))
                    sourceTechIdAndVersion = translator.translate(sourceTechIdAndVersion).toString();
            }
            return sourceTechIdAndVersion;
        };
        
        return translateTechFunc;
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
                    Util.WINDUP_BRAND_NAME_ACRONYM + " configuration must not be null. (Call default execution if no configuration is required.)");

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
