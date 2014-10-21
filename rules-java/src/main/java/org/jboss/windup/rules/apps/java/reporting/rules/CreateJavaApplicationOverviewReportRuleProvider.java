package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateJavaApplicationOverviewReportRuleProvider extends WindupRuleProvider
{
    public static final String OVERVIEW = "Overview";
    public static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/java_application.ftl";

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_GENERATION;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        GraphOperation createReport = new GraphOperation()
        {   
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                WindupConfigurationModel wcm = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                ProjectModel projectModel = wcm.getInputPath().getProjectModel();
                if (projectModel == null)
                {
                    throw new WindupException("Error, no project found in: " + wcm.getInputPath().getFilePath());
                }
                createApplicationReport(event.getGraphContext(), projectModel);
            }
            
            @Override
            public String toString()
            {
                return "CreateJavaApplicationOverviewReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(createReport);
    }
    // @formatter:on

    private ApplicationReportModel createApplicationReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportModel applicationReportModel =
                    context.getFramed().addVertex(null, ApplicationReportModel.class);
        applicationReportModel.setReportPriority(100);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName(OVERVIEW);
        applicationReportModel.setMainApplicationReport(true);
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);
        applicationReportModel.setDisplayInApplicationList(true);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, projectModel.getName(), "html");

        return applicationReportModel;
    }
}
