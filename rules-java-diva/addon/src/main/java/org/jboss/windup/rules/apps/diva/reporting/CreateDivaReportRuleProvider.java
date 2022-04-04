package org.jboss.windup.rules.apps.diva.reporting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.diva.model.DivaAppModel;
import org.jboss.windup.rules.apps.diva.model.DivaContextModel;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of Diva transaction analysis.
 */
@RuleMetadata(phase = ReportGenerationPhase.class, id = "Create Diva Report")
public class CreateDivaReportRuleProvider extends AbstractRuleProvider {
    public static final String TEMPLATE_DIVA_REPORT = "/reports/templates/diva.ftl";
    public static final String REPORT_DESCRIPTION = "This report contains details Diva related resources that were found in the application.";

    private static final Logger LOG = Logging.get(CreateDivaReportRuleProvider.class);

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
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
                    createDivaReport(event.getGraphContext(), application);
                }
            }

            @Override
            public String toString() {
                return "CreateDivaReport";
            }
        };

        return ConfigurationBuilder.begin().addRule().perform(addReport);
    }

    private void createDivaReport(GraphContext context, ProjectModel application) {
        GraphService<DivaAppModel> appModelService = new GraphService<>(context, DivaAppModel.class);
        GraphService<DivaContextModel> cxtModelService = new GraphService<>(context, DivaContextModel.class);

        List<DivaContextModel> cxts = cxtModelService.findAll();
        if (cxts.isEmpty()) {
            return;
        }

        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(300);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Transactions");
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon service-nav-logo");
        applicationReportModel.setProjectModel(application);
        applicationReportModel.setTemplatePath(TEMPLATE_DIVA_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        Map<String, WindupVertexFrame> data = new HashMap<>();
        data.put("contexts", listService.create().addAll(cxts));
        applicationReportModel.setRelatedResource(data);

        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "divareport_" + application.getName(), "html");
    }
}
