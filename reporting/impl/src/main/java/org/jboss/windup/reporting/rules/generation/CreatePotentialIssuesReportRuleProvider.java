package org.jboss.windup.reporting.rules.generation;

import java.util.Collections;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.MigrationIssuesReportModel;
import org.jboss.windup.reporting.model.TaggableModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(disabled = true, phase = ReportGenerationPhase.class)
public class CreatePotentialIssuesReportRuleProvider extends AbstractRuleProvider
{
    public static final String POTENTIAL_ISSUES = "Potential Issues";
    public static final String TEMPLATE_CATCHALL_REPORT = "/reports/templates/migration-issues.ftl";

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
                return "CreatePotentialIssuesReportRuleProvider";
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
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel report = applicationReportService.create();
        report.setReportPriority(102);
        report.setReportIconClass("glyphicon glyphicon-warning-sign");
        report.setTemplatePath(TEMPLATE_CATCHALL_REPORT);
        report.setTemplateType(TemplateType.FREEMARKER);
        report.setDisplayInApplicationReportIndex(true);
        report.setProjectModel(projectModel);
        report.setReportName(POTENTIAL_ISSUES);
        String description = "The " + report.getReportName()
                    + " report is a numerical summary of potential issues. While they may potentially require attention, " +
                    " we do not currently have detailed migration guidance available for these items. If you see issues here" +
                    " that require effort, please send them to us for further assistance. ";
        report.setDescription(description);

        MigrationIssuesReportModel migrationIssuesReport = new GraphService<>(context, MigrationIssuesReportModel.class)
                    .addTypeToModel(report);
        migrationIssuesReport.setIncludeTags(Collections.singleton(TaggableModel.CATCHALL_TAG));

        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(report, "potential_issues", "html");
    }

}
