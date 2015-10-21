package org.jboss.windup.rules.apps.javaee.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
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
        GraphOperation addReport = new GraphOperation()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                for (FileModel inputPath : windupConfiguration.getInputPaths())
                {
                    ProjectModel projectModel = inputPath.getProjectModel();
                    if (projectModel == null)
                    {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createJPAReport(event.getGraphContext(), projectModel);
                }
            }

            @Override
            public String toString()
            {
                return "CreateJPAReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(addReport);
    }

    private void createJPAReport(GraphContext context, ProjectModel projectModel)
    {

        JPAConfigurationFileService jpaConfigurationFileService = new JPAConfigurationFileService(context);
        JPAEntityService jpaEntityService = new JPAEntityService(context);
        GraphService<JPANamedQueryModel> jpaNamedQueryService = new GraphService<>(context, JPANamedQueryModel.class);
        

        List<JPAConfigurationFileModel> jpaConfigList = new ArrayList<>();
        for (JPAConfigurationFileModel jpaConfig : jpaConfigurationFileService.findAll())
        {
            if (jpaConfig.getApplication().equals(projectModel))
                jpaConfigList.add(jpaConfig);
        }

        List<JPAEntityModel> entityList = new ArrayList<>();
        for (JPAEntityModel entityModel : jpaEntityService.findAll())
        {
            if (entityModel.getApplication().equals(projectModel))
                entityList.add(entityModel);
        }

        List<JPANamedQueryModel> namedQueryList = new ArrayList<>();
        for (JPANamedQueryModel namedQuery : jpaNamedQueryService.findAll())
        {
            if (namedQuery.getJpaEntity().getApplication().equals(projectModel))
                namedQueryList.add(namedQuery);
        }

        if (jpaConfigList.isEmpty() && entityList.isEmpty() && namedQueryList.isEmpty())
            return;

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(3);
        additionalData.put("jpaConfiguration", listService.create().addAll(jpaConfigList));
        additionalData.put("jpaEntities", listService.create().addAll(entityList));
        additionalData.put("jpaNamedQueries", listService.create().addAll(namedQueryList));

        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(400);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("JPA");
        applicationReportModel.setReportIconClass("glyphicon jpa-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_JPA_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "jpa_" + projectModel.getName(), "html");
    }
}
