package org.jboss.windup.rules.apps.java.reporting.rules;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummary;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummaryService;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.reporting.service.EffortReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Generates a .js (javascript) file in the reports directory with detailed issue summary information.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class CreateIssueSummaryDataRuleProvider extends AbstractRuleProvider
{

    public static final String DATA_DIR = "data";
    public static final String ISSUE_SUMMARIES_JS = "issue_summaries.js";

    public CreateIssueSummaryDataRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateIssueSummaryDataRuleProvider.class).setPhase(ReportRenderingPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new GraphOperation()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context)
                        {
                            generateDataSummary(event);
                        }
                    });
    }

    private void generateDataSummary(GraphRewrite event)
    {
        ReportService reportService = new ReportService(event.getGraphContext());

        try
        {
            Path dataDirectory = Paths.get(reportService.getReportDirectory(), DATA_DIR);
            if (!Files.exists(dataDirectory))
                Files.createDirectories(dataDirectory);

            Path issueSummaryJSPath = dataDirectory.resolve(ISSUE_SUMMARIES_JS);
            try (FileWriter issueSummaryWriter = new FileWriter(issueSummaryJSPath.toFile()))
            {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                issueSummaryWriter.write("var WINDUP_ISSUE_SUMMARIES = [];" + OperatingSystemUtils.getLineSeparator());

                for (FileModel inputApplicationFile : windupConfiguration.getInputPaths())
                {
                    ProjectModel inputApplication = inputApplicationFile.getProjectModel();

                    MappingJsonFactory jsonFactory = new MappingJsonFactory();
                    jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
                    ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
                    Map<Severity, List<ProblemSummary>> summariesBySeverity = ProblemSummaryService.getProblemSummaries(event.getGraphContext(),
                                inputApplication, Collections.<String> emptySet(), Collections.<String> emptySet());

                    issueSummaryWriter.write("WINDUP_ISSUE_SUMMARIES['" + inputApplication.asVertex().getId() + "'] = ");
                    objectMapper.writeValue(issueSummaryWriter, summariesBySeverity);
                    issueSummaryWriter.write(";" + OperatingSystemUtils.getLineSeparator());
                }

                issueSummaryWriter.write("var effortToDescription = [];" + OperatingSystemUtils.getLineSeparator());
                Map<Integer, String> effortToDescriptionMap = EffortReportService.getEffortLevelDescriptionMappings();
                for (Map.Entry<Integer, String> effortToDescription : effortToDescriptionMap.entrySet())
                {
                    issueSummaryWriter
                                .write("effortToDescription[" + effortToDescription.getKey() + "] = \"" + effortToDescription.getValue() + "\";");
                    issueSummaryWriter.write(OperatingSystemUtils.getLineSeparator());
                }

                issueSummaryWriter.write("var effortOrder = [");
                boolean first = true;
                for (Integer effort : effortToDescriptionMap.keySet())
                {
                    if (first)
                        first = false;
                    else
                        issueSummaryWriter.write(", ");

                    issueSummaryWriter.write(String.valueOf(effort));
                }
                issueSummaryWriter.write("];" + OperatingSystemUtils.getLineSeparator());

                issueSummaryWriter.write("var severityOrder = [");
                issueSummaryWriter.write("\"" + Severity.MANDATORY + "\", ");
                issueSummaryWriter.write("\"" + Severity.OPTIONAL + "\", ");
                issueSummaryWriter.write("\"" + Severity.POTENTIAL_ISSUES.toString() + "\"");
                issueSummaryWriter.write("];" + OperatingSystemUtils.getLineSeparator());
            }
        }
        catch (Exception e)
        {
            throw new WindupException("Error serializing problem details due to: " + e.getMessage(), e);
        }
    }
}
