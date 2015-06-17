package org.jboss.windup.rules.apps.javaee.rules;

import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
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
import org.jboss.windup.rules.apps.javaee.model.JPAConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.JPAEntityModel;
import org.jboss.windup.rules.apps.javaee.model.JPANamedQueryModel;
import org.jboss.windup.rules.apps.javaee.service.JPAConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.JPAEntityService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of JPA files within the application (eg, session configuration or entity lists).
 *
 */
public class CreateJPAReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_JPA_REPORT = "/reports/templates/jpa.ftl";

    public CreateJPAReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateJPAReportRuleProvider.class, "Create JPA Report")
                    .setPhase(ReportGenerationPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder applicationProjectModelsFound = Query.fromType(JPAConfigurationFileModel.class)
                    .or(Query.fromType(JPAEntityModel.class))
                    .or(Query.fromType(JPANamedQueryModel.class));

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
                createJPAReport(event.getGraphContext(), projectModel);
            }

            @Override
            public String toString()
            {
                return "CreateJPAReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(applicationProjectModelsFound)
                    .perform(addReport);
    }

    private void createJPAReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(400);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("JPA");
        applicationReportModel.setReportIconClass("glyphicon jpa-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_JPA_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        JPAConfigurationFileService jpaConfigurationFileService = new JPAConfigurationFileService(context);
        JPAEntityService jpaEntityService = new JPAEntityService(context);
        GraphService<JPANamedQueryModel> jpaNamedQueryService = new GraphService<>(context, JPANamedQueryModel.class);
        
        GraphService<WindupVertexListModel> listService = new GraphService<WindupVertexListModel>(context, WindupVertexListModel.class);

        WindupVertexListModel jpaConfigList = listService.create();
        for (JPAConfigurationFileModel jpaConfig : jpaConfigurationFileService.findAll())
        {
            jpaConfigList.addItem(jpaConfig);
        }

        WindupVertexListModel entityList = listService.create();
        for (JPAEntityModel entityModel : jpaEntityService.findAll())
        {
            entityList.addItem(entityModel);
        }
        
        WindupVertexListModel namedQueryList = listService.create();
        for (JPANamedQueryModel model : jpaNamedQueryService.findAll())
        {
            namedQueryList.addItem(model);
        }
        

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(2);
        additionalData.put("jpaConfiguration", jpaConfigList);
        additionalData.put("jpaEntities", entityList);
        additionalData.put("jpaNamedQueries", namedQueryList);
        
        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "jpa_" + projectModel.getName(), "html");
    }
}
