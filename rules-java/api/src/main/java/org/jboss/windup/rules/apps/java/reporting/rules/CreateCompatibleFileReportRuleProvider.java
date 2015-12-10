package org.jboss.windup.rules.apps.java.reporting.rules;

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
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Creates the report HTML page for compatible files - "lift and shift" files.
 */
public class CreateCompatibleFileReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/compatible_files.ftl";

    public CreateCompatibleFileReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateCompatibleFileReportRuleProvider.class)
                    .setPhase(ReportGenerationPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Boolean generateReport = (Boolean)context.getOptionMap().get(EnableCompatibleFilesReportOption.NAME);
        if (generateReport == null || !generateReport)
            return ConfigurationBuilder.begin();

        ConditionBuilder applicationProjectModelsFound = Query.fromType(WindupConfigurationModel.class);

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
                    createApplicationReport(event.getGraphContext(), projectModel);
                }
            }

            @Override
            public String toString()
            {
                return "CreateCompatibleFilesApplicationReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(applicationProjectModelsFound)
                    .perform(addApplicationReport);

    }
    // @formatter:on

    private ApplicationReportModel createApplicationReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(200);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Compatible Files");
        applicationReportModel.setReportIconClass("glyphicon glyphicon-check");
        applicationReportModel.setMainApplicationReport(false);
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "compatiblefiles_" + projectModel.getName(), "html");

        return applicationReportModel;
    }
}
