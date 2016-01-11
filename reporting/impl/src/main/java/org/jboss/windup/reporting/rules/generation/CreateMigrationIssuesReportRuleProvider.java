package org.jboss.windup.reporting.rules.generation;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.MigrationIssuesReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.google.common.collect.Iterables;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class CreateMigrationIssuesReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_PATH = "/reports/templates/migration-issues.ftl";
    public static final String REPORT_DESCRIPTION = "The Migration Issues report provides a concise summary of all issues that require attention.";

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

    private class CreateMigrationIssueReportOperation extends GraphOperation
    {
        private static final String ALL_MIGRATION_ISSUES_REPORT_NAME = "All Migration Issues";
        private static final String MIGRATION_ISSUES_REPORT_NAME = "Migration Issues";

        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            int inputApplicationCount = Iterables.size(WindupConfigurationService.getConfigurationModel(event.getGraphContext()).getInputPaths());
            if (inputApplicationCount > 1)
            {
                createGlobalMigrationIssuesReport(event.getGraphContext());
            }

            for (FileModel inputPath : WindupConfigurationService.getConfigurationModel(event.getGraphContext()).getInputPaths())
            {
                ApplicationReportModel report = createSingleApplicationMigrationIssuesReport(event.getGraphContext(), inputPath.getProjectModel());
                report.setMainApplicationReport(false);
            }
        }

        private ApplicationReportModel createMigrationIssuesReportBase(GraphContext context)
        {
            ApplicationReportService applicationReportService = new ApplicationReportService(context);
            ApplicationReportModel report = applicationReportService.create();
            report.setReportPriority(101);
            report.setReportIconClass("glyphicon glyphicon-warning-sign");
            report.setTemplatePath(TEMPLATE_PATH);
            report.setTemplateType(TemplateType.FREEMARKER);
            report.setDisplayInApplicationReportIndex(true);
            report.setDescription(REPORT_DESCRIPTION);

            new GraphService<>(context, MigrationIssuesReportModel.class).addTypeToModel(report);

            return report;
        }

        private ApplicationReportModel createSingleApplicationMigrationIssuesReport(GraphContext context, ProjectModel projectModel)
        {
            ReportService reportService = new ReportService(context);
            ApplicationReportModel report = createMigrationIssuesReportBase(context);
            report.setReportName(MIGRATION_ISSUES_REPORT_NAME);
            report.setProjectModel(projectModel);
            reportService.setUniqueFilename(report, "migration_issues", "html");
            return report;
        }

        private ApplicationReportModel createGlobalMigrationIssuesReport(GraphContext context)
        {
            ReportService reportService = new ReportService(context);
            ApplicationReportModel report = createMigrationIssuesReportBase(context);
            report.setReportName(ALL_MIGRATION_ISSUES_REPORT_NAME);
            report.setDisplayInGlobalApplicationIndex(true);
            reportService.setUniqueFilename(report, "migration_issues", "html");
            return report;
        }
    }
}
