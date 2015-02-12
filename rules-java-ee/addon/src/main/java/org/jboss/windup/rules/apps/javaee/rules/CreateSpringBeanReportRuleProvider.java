package org.jboss.windup.rules.apps.javaee.rules;

import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGeneration;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateEntityService;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report on the spring configuration (spring beans, etc).
 *
 */
public class CreateSpringBeanReportRuleProvider extends WindupRuleProvider
{
    public static final String TEMPLATE_SPRING_REPORT = "/reports/templates/spring.ftl";

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ReportGeneration.class;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        // only build this when there are spring beans to report.
        ConditionBuilder applicationProjectModelsFound = Query.fromType(SpringBeanModel.class);

        GraphOperation addReport = new GraphOperation()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                ProjectModel projectModel = windupConfiguration.getInputPath().getProjectModel();
                if (projectModel == null)
                {
                    throw new WindupException("Error, no project found in: " + windupConfiguration.getInputPath().getFilePath());
                }
                createSpringBeanReport(event.getGraphContext(), projectModel);
            }

            @Override
            public String toString()
            {
                return "CreateSpringBeanReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(applicationProjectModelsFound)
                    .perform(addReport);
    }

    private void createSpringBeanReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(500);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Spring Bean Report");
        applicationReportModel.setReportIconClass("glyphicon glyphicon-leaf");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-leaf");
        applicationReportModel.setTemplatePath(TEMPLATE_SPRING_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        SpringBeanService springBeanService = new SpringBeanService(context);
        HibernateEntityService hibernateEntityService = new HibernateEntityService(context);
        GraphService<WindupVertexListModel> listService = new GraphService<WindupVertexListModel>(context, WindupVertexListModel.class);

        WindupVertexListModel springBeanList = listService.create();
        for (SpringBeanModel springBeanModel : springBeanService.findAll())
        {
            springBeanList.addItem(springBeanModel);
        }

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(2);
        additionalData.put("springBeans", springBeanList);
        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "springbeans_" + projectModel.getName(), "html");
    }
}
