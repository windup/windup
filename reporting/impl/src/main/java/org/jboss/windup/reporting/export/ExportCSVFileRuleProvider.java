package org.jboss.windup.reporting.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.opencsv.CSVWriter;

/**
 * RuleProvider generating optional CSV files for every application. This file will contain the main reporting information.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@RuleMetadata(phase = ReportGenerationPhase.class, haltOnException = true)
public class ExportCSVFileRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logger.getLogger(ExportCSVFileRuleProvider.class.getCanonicalName());
    private static final String MERGED_CSV_FILENAME = "AllIssues";

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(WindupConfigurationModel.class).withProperty(WindupConfigurationModel.CSV_MODE, true))
                .perform(
                        Iteration.over(Iteration.DEFAULT_VARIABLE_LIST_STRING).perform(
                                new ExportCSVReportOperation()).endIteration());
    }
    // @formatter:on

    private final class ExportCSVReportOperation extends AbstractIterationOperation<WindupConfigurationModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel config)
        {
            InlineHintService hintService = new InlineHintService(event.getGraphContext());
            String outputFolderPath = config.getOutputPath().getFilePath() + File.separator;
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            final Map<String, CSVWriter> projectToFile = new HashMap<>();
            final List<InlineHintModel> hints = hintService.findAll();
            final List<ClassificationModel> classifications = classificationService.findAll();
            List<EffortReportModel> reportableEvents = new ArrayList<>();
            reportableEvents.addAll(hints);
            reportableEvents.addAll(classifications);

            //try{} in case something bad happens, we need to close files
            try
            {
                reportableEvents.stream().sorted((o1,o2) ->

                        ((Comparator<EffortReportModel>) (o11, o21) -> {
                            IssueCategoryModel c1 = o11.getIssueCategory();
                            IssueCategoryModel c2 = o21.getIssueCategory();
                            Comparator comparator = new IssueCategoryModel.IssueSummaryPriorityComparator();
                            return comparator.compare(c1, c2);
                        }).thenComparing(((Comparator<EffortReportModel>) (o112, o212) -> {
                            int i1 = o112.getEffort();
                            int i2 = o212.getEffort();

                            return Integer.compare(i1, i2);
                        }).reversed()).compare(o1,o2)).forEachOrdered((Object reportableEvent) ->


                {
                    if (reportableEvent instanceof InlineHintModel)
                    {
                        InlineHintModel hint = (InlineHintModel)reportableEvent;
                        final ProjectModel parentRootProjectModel = hint.getFile().getProjectModel().getRootProjectModel();
                        String links = buildLinkString(hint.getLinks());
                        String ruleId = hint.getRuleID() != null ? hint.getRuleID() : "";
                        String title = hint.getTitle() != null ? hint.getTitle() : "";
                        String description = hint.getDescription() != null ? hint.getDescription() : "";
                        String projectNameString = "";
                        String fileName = "";
                        String filePath = "";
                        if (hint.getFile() != null) {
                            if (hint.getFile().getProjectModel() != null) {
                                projectNameString = hint.getFile().getProjectModel().getName();
                            }
                            fileName = hint.getFile().getFileName();
                            filePath = hint.getFile().getFilePath();
                        }
                        String[] strings = new String[]{
                                ruleId, hint.getIssueCategory().getCategoryID(), title, description, links,
                                projectNameString,
                                fileName, filePath, String.valueOf(
                                hint.getLineNumber()), String.valueOf(hint.getEffort())};
                        writeCsvRecordForProject(projectToFile, outputFolderPath, parentRootProjectModel, strings);

                    }
                    if (reportableEvent instanceof ClassificationModel)
                    {
                        ClassificationModel classification = (ClassificationModel)reportableEvent;
                        for (FileModel fileModel : classification.getFileModels())
                        {
                            final ProjectModel parentRootProjectModel = fileModel.getProjectModel().getRootProjectModel();
                            String links = buildLinkString(classification.getLinks());
                            String ruleId = classification.getRuleID() != null ? classification.getRuleID() : "";
                            String classificationText = classification.getClassification() != null ? classification.getClassification() : "";
                            String description = classification.getDescription() != null ? classification.getDescription() : "";
                            String projectNameString = "";
                            if (fileModel.getProjectModel() != null)
                            {
                                projectNameString = fileModel.getProjectModel().getName();
                            }
                            String fileName = fileModel.getFileName();
                            String filePath = fileModel.getFilePath();
                            String[] strings = new String[] {
                                    ruleId, classification.getIssueCategory().getCategoryID(), classificationText,
                                    description, links,
                                    projectNameString, fileName, filePath, "N/A",
                                    String.valueOf(
                                            classification.getEffort()) };
                            writeCsvRecordForProject(projectToFile, outputFolderPath, parentRootProjectModel, strings);

                        }
                    }
                }


                );

            }
            finally
            {
                for (CSVWriter csvWriter : projectToFile.values())
                {
                    try
                    {
                        csvWriter.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

            }

        }

        private String buildLinkString(Iterable<LinkModel> links)
        {
            StringBuilder linksString = new StringBuilder();
            for (LinkModel linkModel : links)
            {
                linksString.append("[");
                linksString.append(linkModel.getLink()).append(",");
                linksString.append(linkModel.getDescription());
                linksString.append("]");
            }
            return linksString.toString();
        }

        private void writeCsvRecordForProject(Map<String, CSVWriter> projectToFile, String outputFolderPath, ProjectModel projectModel, String[] line)
        {
            if (!projectToFile.containsKey(MERGED_CSV_FILENAME))
            {
                String mergedFilename = PathUtil.cleanFileName(MERGED_CSV_FILENAME) + ".csv";
                CSVWriter mergedFileWriter = initCSVWriter(outputFolderPath + mergedFilename, true);
                projectToFile.put(MERGED_CSV_FILENAME, mergedFileWriter);
            }
            if (!projectToFile.containsKey(projectModel.getName()))
            {
                String filename = PathUtil.cleanFileName(projectModel.getRootFileModel().getFileName()) + ".csv";
                CSVWriter writer = initCSVWriter(outputFolderPath + filename, false);
                projectToFile.put(projectModel.getName(), writer);
                LOG.info("Setting csv filename to: " + filename + " for id: " + projectModel.getId());
                projectModel.setCsvFilename(filename);
            }
            projectToFile.get(projectModel.getName()).writeNext(line);
            //Convert line array to ArrayList, add extra field for merged file on the end,
            // then convert back to array to send to CSVWriter
            ArrayList<String> mergedList = new ArrayList<String>(Arrays.stream(line).collect(Collectors.toList()));
            mergedList.add(projectModel.getRootFileModel().asFile().getName());
            String[] mergedLine = new String[ mergedList.size() ];
            projectToFile.get(MERGED_CSV_FILENAME).writeNext(mergedList.toArray(mergedLine));

        }

        private CSVWriter initCSVWriter(String path, boolean isMergedFile)
        {
            try
            {
                CSVWriter writer = new CSVWriter(
                            new FileWriter(path), ',');
                String[] headerLine;
                if (!isMergedFile)
                {
                    headerLine = new String[]{"Rule Id", "Issue Category", "Title", "Description", "Links", "Application", "File Name",
                            "File Path", "Line", "Story points"};
                }
                else
                {

                    headerLine = new String[]{"Rule Id", "Issue Category", "Title", "Description", "Links", "Application", "File Name",
                            "File Path", "Line", "Story points", "Parent Application"};
                }
                writer.writeNext(headerLine);
                return writer;
            }
            catch (IOException e)
            {
                System.err.println(Util.WINDUP_BRAND_NAME_ACRONYM+" was not able to create a CSV file " + path + ". CSV Export will not be generated.");
                throw new WindupException("Unable to create file " + path, e);
            }
        }
    }
}
