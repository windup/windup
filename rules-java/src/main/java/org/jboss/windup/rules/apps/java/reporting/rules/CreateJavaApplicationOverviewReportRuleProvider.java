package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGeneration;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.OverviewReportLineMessageModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateJavaApplicationOverviewReportRuleProvider extends WindupRuleProvider
{
    public static final String OVERVIEW = "Overview";
    public static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/java_application.ftl";

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ReportGeneration.class;
    }

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
                ProjectModel projectModel = payload.getInputPath().getProjectModel();
                if (projectModel == null)
                {
                    throw new WindupException("Error, no project found in: " + payload.getInputPath().getFilePath());
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
                    .when(applicationProjectModelsFound)
                    .perform(addApplicationReport);

    }
    // @formatter:on

    private ApplicationReportModel createApplicationReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportModel applicationReportModel =
                    context.getFramed().addVertex(null, ApplicationReportModel.class);
        applicationReportModel.setReportPriority(100);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName(OVERVIEW);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-home");
        applicationReportModel.setMainApplicationReport(true);
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);
        applicationReportModel.setDisplayInApplicationList(true);
        GraphService<OverviewReportLineMessageModel> lineNotesService = new GraphService<OverviewReportLineMessageModel>(context,
                    OverviewReportLineMessageModel.class);
        Iterable<OverviewReportLineMessageModel> findAll = lineNotesService.findAll();
        for (OverviewReportLineMessageModel find : findAll)
        {
            String projectPrettyPath = projectModel.getRootFileModel().getPrettyPath();
            ProjectModel project = find.getProject();
            boolean found = false;
            while (project != null && !found)
            {
                if (projectPrettyPath.equals(project.getRootFileModel().getPrettyPath()))
                {
                    applicationReportModel.addApplicationReportLine(find);
                    found = true;
                }
                else
                {
                    project = project.getParentProject();
                }
            }
        }
        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, projectModel.getName(), "html");

        return applicationReportModel;
    }
}
