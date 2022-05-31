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
import org.jboss.windup.rules.apps.javaee.model.EjbRemoteServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JaxRSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JaxWSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.jboss.windup.rules.apps.javaee.model.RemoteServiceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a report of EJB data (eg, a list of EJB session beans).
 */
@RuleMetadata(phase = ReportGenerationPhase.class, id = "Create Remote Services Report")
public class CreateRemoteReportRuleProvider extends AbstractRuleProvider {
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/remote.ftl";
    public static final String REPORT_DESCRIPTION = "This report displays all remote services references that were found within the application.";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(RemoteServiceModel.class))
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        // configuration of current execution
                        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                        for (FileModel inputPath : configurationModel.getInputPaths()) {
                            ProjectModel projectModel = inputPath.getProjectModel();
                            createReport(event.getGraphContext(), projectModel);
                        }
                    }

                    @Override
                    public String toString() {
                        return "CreateRemoteServiceReport";
                    }
                });

    }

    @SuppressWarnings("unchecked")
    private void createReport(GraphContext context, ProjectModel projectModel) {
        GraphService<RemoteServiceModel> remoteServices = new GraphService<>(context, RemoteServiceModel.class);

        List<JaxRSWebServiceModel> jaxRsList = new ArrayList<>();
        List<JaxWSWebServiceModel> jaxWsList = new ArrayList<>();
        List<EjbRemoteServiceModel> ejbRemoteList = new ArrayList<>();
        List<RMIServiceModel> rmiList = new ArrayList<>();

        for (RemoteServiceModel remoteServiceModel : remoteServices.findAll()) {
            if (!remoteServiceModel.isAssociatedWithApplication(projectModel))
                continue;

            if (remoteServiceModel instanceof JaxRSWebServiceModel) {
                jaxRsList.add((JaxRSWebServiceModel) remoteServiceModel);
            } else if (remoteServiceModel instanceof JaxWSWebServiceModel) {
                jaxWsList.add((JaxWSWebServiceModel) remoteServiceModel);
            } else if (remoteServiceModel instanceof EjbRemoteServiceModel) {
                ejbRemoteList.add((EjbRemoteServiceModel) remoteServiceModel);
            } else if (remoteServiceModel instanceof RMIServiceModel) {
                rmiList.add((RMIServiceModel) remoteServiceModel);
            }
        }

        if (jaxRsList.isEmpty() && jaxWsList.isEmpty() && ejbRemoteList.isEmpty() && rmiList.isEmpty())
            return;

        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(300);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Remote Services");
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon service-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        Map<String, WindupVertexFrame> data = new HashMap<>(4);
        data.put("jaxRsServices", listService.create().addAll(jaxRsList));
        data.put("jaxWsServices", listService.create().addAll(jaxWsList));
        data.put("ejbRemoteServices", listService.create().addAll(ejbRemoteList));
        data.put("rmiServices", listService.create().addAll(rmiList));
        applicationReportModel.setRelatedResource(data);

        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "remotereport_" + projectModel.getName(), "html");
    }
}
