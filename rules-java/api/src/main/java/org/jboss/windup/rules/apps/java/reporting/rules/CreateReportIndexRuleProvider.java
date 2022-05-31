package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report with a list of other reports, as well as summary information about the analysis findings.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateReportIndexRuleProvider extends AbstractRuleProvider {
    public static final String REPORT_INDEX = "Dashboard";
    public static final String TEMPLATE = "/reports/templates/report_index.ftl";

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                        for (FileModel inputPath : configuration.getInputPaths()) {
                            createReportIndex(event.getGraphContext(), inputPath.getProjectModel());
                        }
                    }
                });
    }
    // @formatter:on

    private void createReportIndex(GraphContext context, ProjectModel projectModel) {
        ApplicationReportService service = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = service.create();
        applicationReportModel.setReportPriority(100);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName(REPORT_INDEX);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-dashboard");
        applicationReportModel.setMainApplicationReport(true);
        applicationReportModel.setTemplatePath(TEMPLATE);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setDescription(
                "Dashboard report aggregating findings from the analysis.");

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "report_index_" + projectModel.getName(), "html");
    }

}

