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

/**
 * Generate a report on all files that have been marked as "lift & shift" (no migration effort needed).
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class CreateJavaLiftAndShiftReportRuleProvider extends WindupRuleProvider
{
    public static final String LIFT_AND_SHIFT = "Lift & Shift";
    public static final String TEMPLATE = "/reports/templates/java_application_lift_and_shift.ftl";

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
                return "CreateJavaLiftAndShiftReport";
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
        applicationReportModel.setReportPriority(200);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName(LIFT_AND_SHIFT);
        applicationReportModel.setMainApplicationReport(false);
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);
        applicationReportModel.setDisplayInApplicationList(false);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, projectModel.getName() + "_liftandshift", "html");

        return applicationReportModel;
    }

}
