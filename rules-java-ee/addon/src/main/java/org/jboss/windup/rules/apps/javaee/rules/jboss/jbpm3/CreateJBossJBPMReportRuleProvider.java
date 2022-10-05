package org.jboss.windup.rules.apps.javaee.rules.jboss.jbpm3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.Jbpm3ProcessModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of JBPM 3 Process Image Report
 */
@RuleMetadata(phase = ReportGenerationPhase.class, id = "Create JBPM Report")
public class CreateJBossJBPMReportRuleProvider extends AbstractRuleProvider {
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/jbpm.ftl";
    public static final String REPORT_DESCRIPTION = "This report contains all of the JBPM related resources that were discovered during analysis.";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(Jbpm3ProcessModel.class))
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        // configuration of current execution
                        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                        for (FileModel inputPath : configurationModel.getInputPaths()) {
                            ProjectModel application = inputPath.getProjectModel();
                            createJbpmReport(event.getGraphContext(), application);
                        }
                    }

                    @Override
                    public String toString() {
                        return "CreateJBPM3Report";
                    }
                });
    }

    private void createJbpmReport(GraphContext context, ProjectModel application) {
        GraphService<Jbpm3ProcessModel> jbpmProcessService = new GraphService<>(context, Jbpm3ProcessModel.class);
        List<Jbpm3ProcessModel> processModelList = new ArrayList<>();
        for (Jbpm3ProcessModel processModel : jbpmProcessService.findAll()) {
            Set<ProjectModel> applicationsContainingFile = ProjectTraversalCache.getApplicationsForProject(context, processModel.getProjectModel());
            if (applicationsContainingFile.contains(application))
                processModelList.add(processModel);
        }

        // Return early if none were found
        if (processModelList.isEmpty())
            return;

        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(300);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("JBPM");
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon bpm-nav-logo");
        applicationReportModel.setProjectModel(application);
        applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

        @SuppressWarnings("unchecked")
        WindupVertexListModel<Jbpm3ProcessModel> processes = listService.create();
        processes.addAll(processModelList);

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(1);
        additionalData.put("processes", processes);
        applicationReportModel.setRelatedResource(additionalData);

        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "jbpmreport_" + application.getName(), "html");
    }
}
