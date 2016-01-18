package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates the main report HTML page for a Java application.
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateUnparsableFilesReportRuleProvider extends AbstractRuleProvider
{
    public static final String REPORT_NAME = "Unparsable";
    public static final String TEMPLATE_UNPARSABLE = "/reports/templates/unparsable_files.ftl";

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        // Create the ReportModel.
        AbstractIterationOperation<WindupConfigurationModel> createReportModel =
                new AbstractIterationOperation<WindupConfigurationModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
            {
                for(FileModel fileModel : payload.getInputPaths()){
                    ProjectModel rootProjectModel = fileModel.getProjectModel();
                    if (rootProjectModel == null)
                        throw new WindupException("Error, no project found in: " + fileModel.getFilePath());

                    createReportModel(event.getGraphContext(), rootProjectModel);
                }
            }

            public String toString() { return "addReport"; }
        };

        // For each FileModel...
        return ConfigurationBuilder.begin()
        .addRule()
        .when(
            Query.fromType(WindupConfigurationModel.class).as("wc"),
            Query.fromType(ProjectModel.class).as("projects")
        )
        .perform(
            Iteration.over("wc").perform(createReportModel).endIteration()
        );

    }
    // @formatter:on

    private void createReportModel(GraphContext context, ProjectModel rootProjectModel)
    {
        GraphService<UnparsablesAppReportModel> service = new GraphService<>(context, UnparsablesAppReportModel.class);
        UnparsablesAppReportModel reportModel = service.create();
        reportModel.setReportPriority(120);
        reportModel.setDisplayInApplicationReportIndex(true);
        reportModel.setReportName(REPORT_NAME);
        reportModel.setReportIconClass("glyphicon glyphicon-warning-sign");
        reportModel.setMainApplicationReport(false);
        reportModel.setProjectModel(rootProjectModel);
        reportModel.setTemplatePath(TEMPLATE_UNPARSABLE);
        reportModel.setTemplateType(TemplateType.FREEMARKER);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(reportModel, REPORT_NAME + "_" + rootProjectModel.getName(), "html");
    }
}
