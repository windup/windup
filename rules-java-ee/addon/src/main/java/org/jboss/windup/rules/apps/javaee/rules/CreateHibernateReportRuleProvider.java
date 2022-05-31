package org.jboss.windup.rules.apps.javaee.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
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
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.HibernateEntityService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a report of Hibernate files within the application (eg, session configuration or entity lists).
 */
@RuleMetadata(phase = ReportGenerationPhase.class, id = "Create Hibernate Report")
public class CreateHibernateReportRuleProvider extends AbstractRuleProvider {
    public static final String TEMPLATE_HIBERNATE_REPORT = "/reports/templates/hibernate.ftl";
    public static final String REPORT_DESCRIPTION = "The Hibernate report contains details on all Hibernate related resources that were found in the application.";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        ConditionBuilder applicationProjectModelsFound = Query.fromType(HibernateConfigurationFileModel.class).or(
                Query.fromType(HibernateEntityModel.class));

        GraphOperation addReport = new GraphOperation() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context) {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                for (FileModel inputPath : windupConfiguration.getInputPaths()) {
                    ProjectModel application = inputPath.getProjectModel();
                    if (application == null) {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createHibernateReport(event.getGraphContext(), application);
                }
            }

            @Override
            public String toString() {
                return "CreateHibernateReport";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .when(applicationProjectModelsFound)
                .perform(addReport);
    }

    private void createHibernateReport(GraphContext context, ProjectModel application) {
        HibernateConfigurationFileService hibernateConfigurationFileService = new HibernateConfigurationFileService(context);
        HibernateEntityService hibernateEntityService = new HibernateEntityService(context);
        List<HibernateConfigurationFileModel> configurationFileModels = new ArrayList<>();
        List<HibernateEntityModel> entityModels = new ArrayList<>();
        for (HibernateConfigurationFileModel hibernateConfig : hibernateConfigurationFileService.findAllByApplication(application)) {
            configurationFileModels.add(hibernateConfig);
        }
        for (HibernateEntityModel entityModel : hibernateEntityService.findAllByApplication(application)) {
            entityModels.add(entityModel);
        }

        // Skip if there is no data for this application
        if (configurationFileModels.isEmpty() && entityModels.isEmpty())
            return;

        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(400);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Hibernate");
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon hibernate-nav-logo");
        applicationReportModel.setProjectModel(application);
        applicationReportModel.setTemplatePath(TEMPLATE_HIBERNATE_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);


        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

        @SuppressWarnings("unchecked")
        WindupVertexListModel<HibernateConfigurationFileModel> hibernateConfigList = listService.create();
        hibernateConfigList.addAll(configurationFileModels);

        @SuppressWarnings("unchecked")
        WindupVertexListModel<HibernateEntityModel> entityList = listService.create();
        entityList.addAll(entityModels);


        Map<String, WindupVertexFrame> additionalData = new HashMap<>(2);
        additionalData.put("hibernateConfiguration", hibernateConfigList);
        additionalData.put("hibernateEntities", entityList);
        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "hibernate_" + application.getName(), "html");
    }
}
