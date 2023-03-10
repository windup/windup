package org.jboss.windup.rules.apps.java.reporting.rules;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.OnlyOnceTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummary;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummaryService;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.rules.AttachApplicationReportsToIndexRuleProvider;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Generates a JSON summary in the reports directory with issues and tags summary information.
 */
@RuleMetadata(phase = DependentPhase.class,
        after = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true)
public class CreateJsonSummaryRuleProvider extends AbstractRuleProvider {
    private static final Set<String> DISCARDED_TAGS = new HashSet<>(Arrays.asList("Java EE", "Embedded", "View", "Connect", "Store", "Sustain", "Execute"));

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(WindupConfigurationModel.class).withProperty(WindupConfigurationModel.SUMMARY_MODE, true))
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        generateDataSummary(event);
                    }
                });
    }

    private void generateDataSummary(GraphRewrite event) {
        try {
            final WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            final List<Object> analysisSummaryList = new ArrayList<>();
            for (FileModel inputApplicationFile : windupConfiguration.getInputPaths()) {
                final ProjectModel inputApplication = inputApplicationFile.getProjectModel();
                final ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(inputApplication, new OnlyOnceTraversalStrategy());
                final Map<String, List<ProblemSummary>> summariesBySeverity =
                        ProblemSummaryService.getProblemSummaries(
                                        event.getGraphContext(), projectModelTraversal.getAllProjects(true), Collections.emptySet(), Collections.emptySet())
                                .entrySet().stream().collect(Collectors.toMap((e) -> e.getKey().getCategoryID(), Map.Entry::getValue));

                analysisSummaryList.add(writeApplicationExportSummary(summariesBySeverity, inputApplicationFile, event.getGraphContext()));
            }
            writeJsonOutputFile(analysisSummaryList, windupConfiguration);
        } catch (Exception e) {
            throw new WindupException("Error serializing problem details due to: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> writeApplicationExportSummary(Map<String, List<ProblemSummary>> summariesBySeverity, FileModel application, GraphContext context) {

        final Map<String, Map<String, Integer>> translatedResults = new HashMap<>();

        summariesBySeverity.forEach((k, v) -> {
            int incidents = 0;
            int effortPoints = 0;
            for (ProblemSummary summary : v) {
                incidents += summary.getNumberFound();
                effortPoints += summary.getNumberFound() * summary.getEffortPerIncident();
            }
            HashMap<String, Integer> results = new HashMap<>();
            results.put("incidents", incidents);
            results.put("totalStoryPoints", effortPoints);
            translatedResults.put(k, results);
        });


        final Map<String, Object> resultsWithTitle = new LinkedHashMap<>();
        resultsWithTitle.put("application", application.getFileName());
        resultsWithTitle.put("incidentsByCategory", translatedResults);

        final List<ProblemSummary> mandatorySummaries = summariesBySeverity.get("mandatory");
        final Map<Integer, Integer> incidentsByEffort = new HashMap<>();
        if (mandatorySummaries != null) {
            mandatorySummaries.forEach(ps -> {
                if (!incidentsByEffort.containsKey(ps.getEffortPerIncident())) {
                    incidentsByEffort.put(ps.getEffortPerIncident(), ps.getNumberFound());
                } else {
                    incidentsByEffort.replace(ps.getEffortPerIncident(), incidentsByEffort.get(ps.getEffortPerIncident()) + ps.getNumberFound());
                }
            });
        }
        final Map<String, Map<String, Integer>> translatedEffortResults = new HashMap<>();

        incidentsByEffort.forEach((k, v) -> {
            HashMap<String, Integer> results = new HashMap<>();

            results.put("incidents", v);
            results.put("totalStoryPoints", k * v);
            translatedEffortResults.put(getEffortDescription(k), results);
        });

        resultsWithTitle.put("mandatoryIncidentsByType", translatedEffortResults);
        resultsWithTitle.put("technologyTags", getTechnologyTagsForApplication(application, context));
        return resultsWithTitle;
    }

    private void writeJsonOutputFile(List<Object> analysisSummary, WindupConfigurationModel windupConfig){
        final String outputFolderPath = windupConfig.getOutputPath().getFilePath() + File.separator + "analysisSummary.json";
        final ObjectMapper mapper = new ObjectMapper();
        try (FileWriter writer = new FileWriter(outputFolderPath)) {
            String json = mapper.writeValueAsString(analysisSummary);
            writer.write(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't convert given map to JSON: " + e.getMessage());
        } catch (IOException ioe) {
            throw new RuntimeException("Couldn't write summary to file: " + ioe.getMessage());
        }
    }

    private Set<Map<String, String>> getTechnologyTagsForApplication(FileModel application, GraphContext context) {
        final GraphService<TechnologyUsageStatisticsModel> service = new GraphService<>(context, TechnologyUsageStatisticsModel.class);
        return service.findAll()
                .stream()
                .filter(technologyUsageStatisticsModel -> application.getProjectModel().equals(technologyUsageStatisticsModel.getProjectModel().getRootProjectModel()))
                .map(technologyUsageStatisticsModel -> {
                    final Map<String, String> techTag = new HashMap<>(2);
                    techTag.put("name", technologyUsageStatisticsModel.getName());
                    techTag.put("category", technologyUsageStatisticsModel.getTags()
                                                .stream()
                                                .filter(technologyName -> !DISCARDED_TAGS.contains(technologyName))
                                                .findFirst()
                                                .orElse("Other"));
                    return techTag;
                })
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(stringStringMap -> stringStringMap.get("name")))));
    }

    private String getEffortDescription(Integer effort){
        switch (effort) {
            case 0:
                return "Info";
            case 1:
                return "Trivial";
            case 3:
                return "Complex";
            case 5:
                return "Redesign";
            case 7:
                return "Architectural";
            default:
                return "Unknown";
        }
    }
}
