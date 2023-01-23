package org.jboss.windup.rules.apps.java.reporting.rules;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.OnlyOnceTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummary;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummaryService;
import org.jboss.windup.reporting.service.EffortReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.category.IssueCategory;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.reporting.service.EffortReportService.EffortLevel;

/**
 * Generates a .js (javascript) file in the reports directory with detailed issue summary information.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = ReportRenderingPhase.class)
public class CreateIssueSummaryDataRuleProvider extends AbstractRuleProvider {
    public static final String ISSUE_SUMMARIES_JS = "issue_summaries.js";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        generateDataSummary(event);
                    }
                });
    }

    private void generateDataSummary(GraphRewrite event) {
        ReportService reportService = new ReportService(event.getGraphContext());

        try {
            Path dataDirectory = reportService.getReportDataDirectory();

            Path issueSummaryJSPath = dataDirectory.resolve(ISSUE_SUMMARIES_JS);
            try (FileWriter issueSummaryWriter = new FileWriter(issueSummaryJSPath.toFile())) {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                issueSummaryWriter.write("var WINDUP_ISSUE_SUMMARIES = [];" + NEWLINE);

                List analysisSummaryList = new ArrayList();

                for (FileModel inputApplicationFile : windupConfiguration.getInputPaths()) {
                    ProjectModel inputApplication = inputApplicationFile.getProjectModel();
                    ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(inputApplication, new OnlyOnceTraversalStrategy());

                    MappingJsonFactory jsonFactory = new MappingJsonFactory();
                    jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
                    ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
                    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                    objectMapper.addMixIn(Object.class, PropertyFilterMixin.class);

                    // Filter out some tinkerpop specific properties
                    SimpleBeanPropertyFilter simpleBeanPropertyFilter = SimpleBeanPropertyFilter.serializeAllExcept("graph", "steps", "traversal", "rawTraversal", "wrappedGraph");
                    FilterProvider filters = new SimpleFilterProvider().addFilter("graphFilter", simpleBeanPropertyFilter);

                    Map<String, List<ProblemSummary>> summariesBySeverity =
                            ProblemSummaryService.getProblemSummaries(
                                            event.getGraphContext(), projectModelTraversal.getAllProjects(true), Collections.emptySet(), Collections.emptySet())
                                    .entrySet().stream().collect(Collectors.toMap((e) -> e.getKey().getCategoryID(), Map.Entry::getValue));

                    issueSummaryWriter.write("WINDUP_ISSUE_SUMMARIES['" + inputApplication.getId() + "'] = ");
                    objectMapper.writer(filters).writeValue(issueSummaryWriter, summariesBySeverity);
                    issueSummaryWriter.write(";" + NEWLINE);
                    if (windupConfiguration.isExportingSummary())
                    {
                        analysisSummaryList.add(writeApplicationExportSummary(summariesBySeverity, inputApplicationFile.getFileName()));
                    }
                }

                if (windupConfiguration.isExportingSummary())
                {
                    writeJsonOutputFile(analysisSummaryList, windupConfiguration);
                }

                issueSummaryWriter.write("var effortToDescription = [];" + NEWLINE);

                for (EffortReportService.EffortLevel level : EffortReportService.EffortLevel.values()) {
                    issueSummaryWriter.write("effortToDescription[" + level.getPoints() + "] = \"" + level.getShortDescription() + "\";");
                    issueSummaryWriter.write(NEWLINE);
                }

                issueSummaryWriter.write("var effortOrder = [");
                String comma = "";
                for (EffortLevel level : EffortLevel.values()) {
                    issueSummaryWriter.write(comma);
                    comma = ", ";
                    issueSummaryWriter.write("\"");
                    issueSummaryWriter.write(level.getShortDescription());
                    issueSummaryWriter.write("\"");
                }
                issueSummaryWriter.write("];" + NEWLINE);

                issueSummaryWriter.write("var severityOrder = [");
                IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(event.getRewriteContext());
                for (IssueCategory issueCategory : issueCategoryRegistry.getIssueCategories()) {
                    issueSummaryWriter.write("'" + issueCategory.getCategoryID() + "', ");
                }
                issueSummaryWriter.write("];" + NEWLINE);
            }
        } catch (Exception e) {
            throw new WindupException("Error serializing problem details due to: " + e.getMessage(), e);
        }
    }

    private static final String NEWLINE = OperatingSystemUtils.getLineSeparator();

    @JsonFilter("graphFilter")
    public static class PropertyFilterMixin {

    }

    private Map writeApplicationExportSummary(Map<String, List<ProblemSummary>> summariesBySeverity, String application) {

        Map<String, Map<String, Integer>> translatedResults = new HashMap<>();

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


        Map<String, Object> resultsWithTitle = new LinkedHashMap<>();
        resultsWithTitle.put("application:", application);
        resultsWithTitle.put("incidentsByCategory", translatedResults);

        List<ProblemSummary> mandatorySummaries = summariesBySeverity.get("mandatory");
        Map<Integer, Integer> incidentsByEffort = new HashMap<>();
        if (mandatorySummaries != null) {
            mandatorySummaries.stream().forEach(ps -> {

                if (!incidentsByEffort.containsKey(ps.getEffortPerIncident())) {
                    incidentsByEffort.put(ps.getEffortPerIncident(), ps.getNumberFound());
                } else {
                    incidentsByEffort.replace(ps.getEffortPerIncident(), incidentsByEffort.get(ps.getEffortPerIncident()) + ps.getNumberFound());
                }
            });
        }
        Map<String, Map<String, Integer>> translatedEffortResults = new HashMap<>();

        incidentsByEffort.forEach((k, v) -> {
            HashMap<String, Integer> results = new HashMap<>();

            results.put("incidents", v);
            results.put("totalStoryPoints", k * v);
            translatedEffortResults.put(getEffortDescription(k), results);
        });

        resultsWithTitle.put("mandatoryIncidentsByType", translatedEffortResults);
        return resultsWithTitle;
    }

    private void writeJsonOutputFile(List analysisSummary, WindupConfigurationModel windupConfig){
        String outputFolderPath = windupConfig.getOutputPath().getFilePath() + File.separator;

        ObjectMapper mapper = new ObjectMapper();
        String json;

        SimpleDateFormat format = new SimpleDateFormat("YYYYMMDDHHmm");
        String analysisTime = format.format(new Date());

        FileWriter writer = null;
        try {
            json = mapper.writeValueAsString(analysisSummary);
            String filename = "analysisSummary_" + analysisTime + ".json";
            writer = new FileWriter(outputFolderPath + filename);
            writer.write(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't convert given map to JSON: " + e.getMessage());
        } catch (IOException ioe) {
            throw new RuntimeException("Couldn't write summary to file: " + ioe.getMessage());
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
