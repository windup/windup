package org.jboss.windup.reporting.rules.generation;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class CreateMigrationIssuesReportRuleProvider extends AbstractRuleProvider
{

    public static final String TEMPLATE_PATH = "/reports/templates/migration-issues.ftl";

    public CreateMigrationIssuesReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateMigrationIssuesReportRuleProvider.class)
                    .setPhase(ReportGenerationPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new CreateProblemCentricReportOperation());
    }

    private class CreateProblemCentricReportOperation extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            ApplicationReportService applicationReportService = new ApplicationReportService(event.getGraphContext());
            ApplicationReportModel report = applicationReportService.create();
            report.setReportPriority(110);
            report.setDisplayInApplicationReportIndex(true);
            report.setReportIconClass("glyphicon glyphicon-warning-sign");
            report.setReportName("Migration Issues");
            report.setTemplatePath(TEMPLATE_PATH);
            report.setTemplateType(TemplateType.FREEMARKER);

            ProjectModel projectModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext()).getInputPath().getProjectModel();
            report.setProjectModel(projectModel);

            ReportService reportService = new ReportService(event.getGraphContext());
            reportService.setUniqueFilename(report, "problem_centric_report", "html");
        }
    }
}
