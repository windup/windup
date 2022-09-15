package org.jboss.windup.rules.apps.javaee.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
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
 */
@RuleMetadata(phase = ReportGenerationPhase.class, id = "Create JPA Report")
public class CreateJPAReportRuleProvider extends AbstractRuleProvider {
    public static final String TEMPLATE_JPA_REPORT = "/reports/templates/jpa.ftl";
    public static final String REPORT_DESCRIPTION = "This report contains details JPA related resources that were found in the application.";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        GraphOperation addReport = new GraphOperation() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context) {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                for (FileModel inputPath : windupConfiguration.getInputPaths()) {
                    ProjectModel application = inputPath.getProjectModel();
                    if (application == null) {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createJPAReport(event.getGraphContext(), application);
                }
            }

            @Override
            public String toString() {
                return "CreateJPAReport";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .perform(addReport);
    }

    private void createJPAReport(GraphContext context, ProjectModel application) {

        JPAConfigurationFileService jpaConfigurationFileService = new JPAConfigurationFileService(context);
        JPAEntityService jpaEntityService = new JPAEntityService(context);
        GraphService<JPANamedQueryModel> jpaNamedQueryService = new GraphService<>(context, JPANamedQueryModel.class);


        List<JPAConfigurationFileModel> jpaConfigList = new ArrayList<>();
        for (JPAConfigurationFileModel jpaConfig : jpaConfigurationFileService.findAll()) {
            Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(context, jpaConfig.getProjectModel());
            if (applications.contains(application))
                jpaConfigList.add(jpaConfig);
        }

        List<JPAEntityModel> entityList = new ArrayList<>();
        for (JPAEntityModel entityModel : jpaEntityService.findAll()) {
            if (Iterables.contains(entityModel.getApplications(), application))
                entityList.add(entityModel);
        }

        List<JPANamedQueryModel> namedQueryList = new ArrayList<>();
        for (JPANamedQueryModel namedQuery : jpaNamedQueryService.findAll()) {
            if (Iterables.contains(namedQuery.getJpaEntity().getApplications(), application))
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
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon jpa-nav-logo");
        applicationReportModel.setProjectModel(application);
        applicationReportModel.setTemplatePath(TEMPLATE_JPA_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "jpa_" + application.getName(), "html");
    }
}
