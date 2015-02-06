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
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.HibernateEntityService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of Hibernate files within the application (eg, session configuration or entity lists).
 *
 */
public class CreateHibernateReportRuleProvider extends WindupRuleProvider
{
    public static final String TEMPLATE_HIBERNATE_REPORT = "/reports/templates/hibernate.ftl";

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ReportGeneration.class;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder applicationProjectModelsFound = Query.fromType(HibernateConfigurationFileModel.class).or(
                    Query.fromType(HibernateEntityModel.class));

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
                createHibernateReport(event.getGraphContext(), projectModel);
            }

            @Override
            public String toString()
            {
                return "CreateHibernateReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(applicationProjectModelsFound)
                    .perform(addReport);
    }

    private void createHibernateReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(400);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Hibernate Report");
        applicationReportModel.setReportIconClass("glyphicon hibernate-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_HIBERNATE_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        HibernateConfigurationFileService hibernateConfigurationFileService = new HibernateConfigurationFileService(context);
        HibernateEntityService hibernateEntityService = new HibernateEntityService(context);
        GraphService<WindupVertexListModel> listService = new GraphService<WindupVertexListModel>(context, WindupVertexListModel.class);

        WindupVertexListModel hibernateConfigList = listService.create();
        for (HibernateConfigurationFileModel hibernateConfig : hibernateConfigurationFileService.findAll())
        {
            hibernateConfigList.addItem(hibernateConfig);
        }

        WindupVertexListModel entityList = listService.create();
        for (HibernateEntityModel entityModel : hibernateEntityService.findAll())
        {
            entityList.addItem(entityModel);
        }

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(2);
        additionalData.put("hibernateConfiguration", hibernateConfigList);
        additionalData.put("hibernateEntities", entityList);
        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "hibernate_" + projectModel.getName(), "html");
    }
}
