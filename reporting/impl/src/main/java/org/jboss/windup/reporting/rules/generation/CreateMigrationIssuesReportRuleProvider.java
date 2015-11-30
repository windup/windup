package org.jboss.windup.reporting.rules.generation;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
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
                    .perform(new CreateMigrationIssueReportOperation());
    }

    private void createMigrationIssuesReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel report = applicationReportService.create();
        report.setReportPriority(110);
        report.setReportIconClass("glyphicon glyphicon-warning-sign");
        report.setReportName("Migration Issues");
        report.setTemplatePath(TEMPLATE_PATH);
        report.setTemplateType(TemplateType.FREEMARKER);
        report.setDisplayInApplicationReportIndex(true);

        ReportService reportService = new ReportService(context);

        if (projectModel == null)
        {
            reportService.setUniqueFilename(report, "main_migration_issues", "html");
        }
        else
        {
            report.setProjectModel(projectModel);
            reportService.setUniqueFilename(report, "migration_issues", "html");
        }
    }

    private class CreateMigrationIssueReportOperation extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            createMigrationIssuesReport(event.getGraphContext(), null);

            for (FileModel inputPath : WindupConfigurationService.getConfigurationModel(event.getGraphContext()).getInputPaths())
            {
                createMigrationIssuesReport(event.getGraphContext(), inputPath.getBoundProject());
            }
        }
    }
}
