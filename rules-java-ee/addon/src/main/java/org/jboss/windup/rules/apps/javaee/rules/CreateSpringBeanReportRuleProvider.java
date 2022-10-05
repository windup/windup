package org.jboss.windup.rules.apps.javaee.rules;

import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.Technology;
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
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report on the Spring configuration (Spring beans, etc.).
 */
@RuleMetadata(
        phase = ReportGenerationPhase.class,
        id = "Create Spring Bean Report"
)
public class CreateSpringBeanReportRuleProvider extends AbstractRuleProvider {
    public static final String TEMPLATE_SPRING_REPORT = "/reports/templates/spring.ftl";
    public static final String REPORT_DESCRIPTION = "This report contains a list of Spring beans found during the analysis.";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        // only build this when there are spring beans to report.
        ConditionBuilder applicationProjectModelsFound = Query.fromType(SpringBeanModel.class);

        GraphOperation addReport = new GraphOperation() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context) {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService
                        .getConfigurationModel(event.getGraphContext());
                for (FileModel inputPath : windupConfiguration.getInputPaths()) {
                    ProjectModel application = inputPath.getProjectModel();
                    if (application == null) {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createSpringBeanReport(event.getGraphContext(), application);
                }
            }

            @Override
            public String toString() {
                return "CreateSpringBeanReport";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .when(applicationProjectModelsFound)
                .perform(addReport);
    }

    private void createSpringBeanReport(GraphContext context, ProjectModel application) {
        SpringBeanService springBeanService = new SpringBeanService(context);
        Iterable<SpringBeanModel> models = springBeanService.findAllByApplication(application);
        if (!models.iterator().hasNext()) {
            return;
        }
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(500);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Spring Beans");
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-leaf");
        applicationReportModel.setProjectModel(application);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-leaf");
        applicationReportModel.setTemplatePath(TEMPLATE_SPRING_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

        @SuppressWarnings("unchecked")
        WindupVertexListModel<SpringBeanModel> springBeanList = listService.create();
        springBeanList.addAll(models);

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(2);
        additionalData.put("springBeans", springBeanList);
        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "springbeans_" + application.getName(), "html");
    }
}
