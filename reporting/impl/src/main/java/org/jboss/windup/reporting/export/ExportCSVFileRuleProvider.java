package org.jboss.windup.reporting.export;

import com.opencsv.CSVWriter;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * RuleProvider generating optional CSV files for every application. This file will contain the main reporting information.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class ExportCSVFileRuleProvider extends AbstractRuleProvider
{
    public static final int COMMIT_INTERVAL = 750;
    public static final int LOG_INTERVAL = 250;
    private static Logger LOG = Logging.get(ExportCSVFileRuleProvider.class);

    public ExportCSVFileRuleProvider()
    {
        super(MetadataBuilder.forProvider(ExportCSVFileRuleProvider.class)
                    .setPhase(ReportGenerationPhase.class)
                    .setHaltOnException(true));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when(Query.fromType(WindupConfigurationModel.class).withProperty(WindupConfigurationModel.CSV_MODE,true)
                        .and(Query.fromType(ClassificationModel.class).as("classifications")
                                    .and(Query.fromType(InlineHintModel.class).as("hints"))))
                    .perform(
                            Iteration.over(Iteration.DEFAULT_VARIABLE_LIST_STRING).perform(
                                        new ExportCSVReportOperation()).endIteration());
    }
    // @formatter:on

    private final class ExportCSVReportOperation extends AbstractIterationOperation<WindupConfigurationModel>
    {
        @Override public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel config)
        {
            final Variables variables = Variables.instance(event);
            final Iterable<InlineHintModel> hints = variables.findVariableOfType(InlineHintModel.class);
            final Iterable<ClassificationModel> classifications = variables.findVariableOfType(ClassificationModel.class);
            Map<String, List<String[]>> projectsToReportItems = new HashMap<>();
            for (InlineHintModel hint : hints)
            {
                final ProjectModel parentRootProjectModel = hint.getFile().getProjectModel().getRootProjectModel();
                String links = buildLinkString(hint.getLinks());
                String ruleId = hint.getRuleID() != null ? hint.getRuleID() : "";
                String title = hint.getTitle() != null ? hint.getTitle() : "";
                String description = hint.getDescription() != null ? hint.getDescription() : "";
                String projectNameString = "";
                String fileName = "";
                String filePath = "";
                if (hint.getFile() != null)
                {
                    if (hint.getFile().getProjectModel() != null)
                    {
                        projectNameString = hint.getFile().getProjectModel().getName();
                    }
                    fileName = hint.getFile().getFileName();
                    filePath = hint.getFile().getFilePath();
                }
                String[] strings = new String[] {
                            ruleId, "hint", title, description, links,
                            projectNameString,
                            fileName, filePath, String.valueOf(
                            hint.getLineNumber()), String.valueOf(hint.getEffort()) };
                if(projectsToReportItems.containsKey(parentRootProjectModel.getName())) {
                    projectsToReportItems.get(parentRootProjectModel.getName()).add(strings);
                } else {
                    List<String[]> reportLine = new ArrayList<>();
                    reportLine.add(strings);
                    projectsToReportItems.put(parentRootProjectModel.getName(),reportLine);
                }
            }
            for (ClassificationModel classification : classifications)
            {
                for (FileModel fileModel : classification.getFileModels())
                {
                    final ProjectModel parentRootProjectModel = fileModel.getProjectModel().getRootProjectModel();
                    String links = buildLinkString(classification.getLinks());
                    String ruleId = classification.getRuleID() != null ? classification.getRuleID() : "";
                    String classifText = classification.getClassification() != null ? classification.getClassification() : "";
                    String description = classification.getDescription() != null ? classification.getDescription() : "";
                    String projectNameString = "";
                    String fileName = "";
                    String filePath = "";
                    if (fileModel.getProjectModel() != null)
                    {
                        projectNameString = fileModel.getProjectModel().getName();
                    }
                    fileName = fileModel.getFileName();
                    filePath = fileModel.getFilePath();
                    String[] strings = new String[] {
                                ruleId, "classification", classifText,
                                description, links,
                                projectNameString, fileName, filePath, "N/A",
                                String.valueOf(
                                            classification.getEffort()) };
                    if(projectsToReportItems.containsKey(parentRootProjectModel.getName())) {
                        projectsToReportItems.get(parentRootProjectModel.getName()).add(strings);
                    } else {
                        List<String[]> reportLine = new ArrayList<>();
                        reportLine.add(strings);
                        projectsToReportItems.put(parentRootProjectModel.getName(),reportLine);
                    }

                }
            }
            for (String projectName : projectsToReportItems.keySet())
            {

                try (CSVWriter writer = new CSVWriter(
                            new FileWriter(config.getOutputPath().getFilePath() + File.separator + projectName + ".export"), ';'))
                {
                    String[] headerLine = new String[] {"Rule Id", "Problem type", "Title", "Description", "Links", "Application", "File Name", "File Path", "Line", "Story points"};
                    writer.writeNext(headerLine);
                    for (String[] strings : projectsToReportItems.get(projectName))
                    {
                        writer.writeNext(strings);
                    }

                }
                catch (IOException e)
                {
                    throw new WindupException(
                                "Unable to create a reporting csv file " + config.getOutputPath() + File.separator + projectName
                                            + ".export");
                }

            }
        }

        private void addProjectReportToProject(WindupVertexFrame frame, ProjectModel rootProjectModel, Map<String, LinkedList<WindupVertexFrame>> map)
        {
            String projectName = rootProjectModel.getName();
            if (map.containsKey(projectName))
            {
                map.get(projectName).add(frame);
            }
            else
            {
                LinkedList<WindupVertexFrame> linkedList = new LinkedList<>();
                linkedList.add(frame);
                map.put(projectName, linkedList);
            }
        }

        private String buildLinkString(Iterable<LinkModel> links)
        {
            StringBuilder linksString = new StringBuilder();
            for (LinkModel linkModel : links)
            {
                linksString.append("[");
                linksString.append(linkModel.getLink() + ",");
                linksString.append(linkModel.getDescription());
                linksString.append("]");
            }
            linksString.toString();
            return linksString.toString();
        }
    }
}
