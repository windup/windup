package org.jboss.windup.rules.apps.java.reporting.rules;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.OverviewReportLineMessageModel;
import org.jboss.windup.reporting.model.TaggableModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.model.JavaApplicationOverviewReportModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates the main report HTML page for a Java application.
 */
public class CreateJavaApplicationOverviewReportRuleProvider extends AbstractRuleProvider
{
    public static final String DETAILS_REPORT = "Application Details";
    public static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/java_application.ftl";
    public static final String DESCRIPTION = "This provides a detailed overview of all resources found within the application that may need attention during the migration.";

    public CreateJavaApplicationOverviewReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateJavaApplicationOverviewReportRuleProvider.class)
                    .setPhase(ReportGenerationPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder applicationProjectModelsFound = Query
                .fromType(WindupConfigurationModel.class);

        AbstractIterationOperation<WindupConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupConfigurationModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
            {
                for (FileModel inputPath : payload.getInputPaths())
                {
                    ProjectModel projectModel = inputPath.getProjectModel();
                    if (projectModel == null)
                    {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createReport(event.getGraphContext(), projectModel);
                }
            }

            @Override
            public String toString()
            {
                return "CreateJavaApplicationOverviewReport";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .when(applicationProjectModelsFound)
                .perform(addApplicationReport);

    }
    // @formatter:on

    private void createReport(GraphContext context, ProjectModel projectModel)
    {
        GraphService<JavaApplicationOverviewReportModel> service = new GraphService<>(context, JavaApplicationOverviewReportModel.class);
        JavaApplicationOverviewReportModel applicationReportModel = service.create();
        applicationReportModel.setReportPriority(102);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName(DETAILS_REPORT);
        applicationReportModel.setDescription(DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-th-list");
        applicationReportModel.setMainApplicationReport(false);
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);
        applicationReportModel.setIncludeTags(Collections.<String> emptySet());
        applicationReportModel.setExcludeTags(Collections.singleton(TaggableModel.CATCHALL_TAG));
        GraphService<OverviewReportLineMessageModel> lineNotesService = new GraphService<>(context, OverviewReportLineMessageModel.class);
        Iterable<OverviewReportLineMessageModel> allLines = lineNotesService.findAll();
        Set<String> dupeCheck = new HashSet<>();

        for (OverviewReportLineMessageModel line : allLines)
        {
            if (dupeCheck.contains(line.getMessage()))
                continue;

            String projectPrettyPath = projectModel.getRootFileModel().getPrettyPath();
            if (projectPrettyPath == null)
            {
                throw new WindupException("Path for project: " + projectModel + " evaluated to null!");
            }

            ProjectModel project = line.getProject();
            boolean found = false;
            while (project != null && !found)
            {
                if (project.getRootFileModel() == null)
                {
                    throw new WindupException("Root file for project: " + project + " evaluated to null!");
                }
                if (projectPrettyPath.equals(project.getRootFileModel().getPrettyPath()))
                {
                    dupeCheck.add(line.getMessage());
                    applicationReportModel.addApplicationReportLine(line);
                    found = true;
                }
                else
                {
                    project = project.getParentProject();
                }
            }
        }
        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "ApplicationDetails_" + projectModel.getName(), "html");
    }
}
