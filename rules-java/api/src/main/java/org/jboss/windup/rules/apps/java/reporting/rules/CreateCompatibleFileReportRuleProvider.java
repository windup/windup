package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
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
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates the report HTML page for compatible files - "lift and shift" files.
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateCompatibleFileReportRuleProvider extends AbstractRuleProvider {
    public static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/compatible_files.ftl";
    public static final String REPORT_DESCRIPTION = "This provides a list of files that are believed to be compatible, potentially requiring no migration effort.";

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {

        GraphCondition graphCondition = new GraphCondition() {
            @Override
            public boolean evaluate(GraphRewrite event, EvaluationContext context) {
                Boolean generateReport = (Boolean) event.getGraphContext().getOptionMap().get(EnableCompatibleFilesReportOption.NAME);
                if (generateReport == null)
                    generateReport = false;

                return generateReport && Query.fromType(WindupConfigurationModel.class).evaluate(event, context);
            }
        };

        AbstractIterationOperation<WindupConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupConfigurationModel>() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload) {
                for (FileModel inputPath : payload.getInputPaths()) {
                    ProjectModel application = inputPath.getProjectModel();
                    if (application == null) {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createApplicationReport(event.getGraphContext(), application);
                }
            }

            @Override
            public String toString() {
                return "CreateCompatibleFilesApplicationReport";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .when(graphCondition)
                .perform(addApplicationReport);

    }
    // @formatter:on

    private ApplicationReportModel createApplicationReport(GraphContext context, ProjectModel application) {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(200);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Compatible Files");
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-check");
        applicationReportModel.setMainApplicationReport(false);
        applicationReportModel.setProjectModel(application);
        applicationReportModel.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "compatiblefiles_" + application.getName(), "html");

        return applicationReportModel;
    }
}
