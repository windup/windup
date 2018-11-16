package org.jboss.windup.rules.apps.javaee.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.jboss.windup.rules.apps.javaee.model.SpringRestWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.SpringRemoteServiceModel;
import org.jboss.windup.rules.apps.javaee.model.*;
import org.jboss.windup.rules.apps.javaee.service.JaxWSWebServiceModelService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of EJB data (eg, a list of EJB session beans).
 */
@RuleMetadata(phase = ReportGenerationPhase.class, id = "Create Remote Services Report")
public class CreateRemoteReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/remote.ftl";
    public static final String REPORT_DESCRIPTION = "This report displays all remote services references that were found within the application.";

    private ApplicationReportModel applicationReportModel;

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
        .addRule()
        .when(Query.fromType(RemoteServiceModel.class))
        .perform(new GraphOperation()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                // configuration of current execution
                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                for (FileModel inputPath : configurationModel.getInputPaths())
                {
                    ProjectModel projectModel = inputPath.getProjectModel();
                    createReport(event.getGraphContext(), projectModel);
                }
            }

            @Override
            public String toString()
            {
                return "CreateRemoteServiceReport";
            }
        });

    }

    @SuppressWarnings("unchecked")
    private void createReport(GraphContext context, ProjectModel projectModel)
    {
        GraphService<RemoteServiceModel> remoteServices = new GraphService<>(context, RemoteServiceModel.class);

        List<WebServiceModel> jaxRsList = new ArrayList<>();
        List<WebServiceModel> jaxWsList = new ArrayList<>();
        List<RemoteServiceModel> ejbRemoteList = new ArrayList<>();
        List<RemoteServiceModel> rmiList = new ArrayList<>();
        List<RemoteServiceModel> amqpList = new ArrayList<>();
        List<RemoteServiceModel> jmsList = new ArrayList<>();
        List<RemoteServiceModel> hessianList = new ArrayList<>();
        List<RemoteServiceModel> httpinvokerList = new ArrayList<>();

        for (RemoteServiceModel remoteServiceModel : remoteServices.findAll())
        {
            if (!remoteServiceModel.isAssociatedWithApplication(projectModel))
                continue;

            if (remoteServiceModel instanceof JaxRSWebServiceModel)
            {
                jaxRsList.add((JaxRSWebServiceModel) remoteServiceModel);
            } else if (remoteServiceModel instanceof SpringRestWebServiceModel)
            {
                jaxRsList.add((SpringRestWebServiceModel) remoteServiceModel);
            }
            else if (remoteServiceModel instanceof JaxWSWebServiceModel)
            {
                jaxWsList.add((JaxWSWebServiceModel) remoteServiceModel);
            }
            else if (remoteServiceModel instanceof EjbRemoteServiceModel)
            {
                ejbRemoteList.add( remoteServiceModel);
            }
            else if (remoteServiceModel instanceof RMIServiceModel)
            {
                rmiList.add(remoteServiceModel);

            } else if (remoteServiceModel instanceof SpringRemoteServiceModel) {
                String packageName = ((SpringRemoteServiceModel) remoteServiceModel).getSpringExporterInterface().getPackageName();
                if (packageName.toLowerCase().contains(".rmi")) {
                    rmiList.add(remoteServiceModel);
                } else if (packageName.toLowerCase().contains(".amqp")) {
                    amqpList.add(remoteServiceModel);

                } else if (packageName.toLowerCase().contains(".jms")) {
                    jmsList.add(remoteServiceModel);

                } else if (packageName.toLowerCase().contains(".caucho")) {
                    hessianList.add(remoteServiceModel);

                } else if (packageName.toLowerCase().contains(".httpinvoker")) {
                    httpinvokerList.add(remoteServiceModel);

                } else if (packageName.toLowerCase().contains(".jaxws")) {
                    WebServiceModel webServiceModel = new JaxWSWebServiceModelService(context).getOrCreate(projectModel, ((SpringRemoteServiceModel) remoteServiceModel).getInterface(), ((SpringRemoteServiceModel) remoteServiceModel).getImplementationClass());
                    jaxWsList.add(webServiceModel);
                }
            }
        }

        if (jaxRsList.isEmpty() && jaxWsList.isEmpty() && ejbRemoteList.isEmpty() && rmiList.isEmpty() &&
                amqpList.isEmpty() && jmsList.isEmpty() && hessianList.isEmpty() && httpinvokerList.isEmpty())
            return;

        ApplicationReportModel applicationReportModel = createReportHeader(context, projectModel);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        Map<String, WindupVertexFrame> data = new HashMap<>(8);
        data.put("jaxRsServices", listService.create().addAll(jaxRsList));
        data.put("jaxWsServices", listService.create().addAll(jaxWsList));
        data.put("ejbRemoteServices", listService.create().addAll(ejbRemoteList));
        data.put("rmiServices", listService.create().addAll(rmiList));
        data.put("amqpServices", listService.create().addAll(amqpList));
        data.put("jmsServices", listService.create().addAll(jmsList));
        data.put("hessianServices", listService.create().addAll(hessianList));
        data.put("httpinvokerServices", listService.create().addAll(httpinvokerList));
        applicationReportModel.setRelatedResource(data);

        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "remotereport_" + projectModel.getName(), "html");
    }

    private ApplicationReportModel createReportHeader(GraphContext context, ProjectModel projectModel) {
        if (applicationReportModel == null) {
            ApplicationReportService applicationReportService = new ApplicationReportService(context);
            applicationReportModel = applicationReportService.create();
            applicationReportModel.setReportPriority(300);
            applicationReportModel.setDisplayInApplicationReportIndex(true);
            applicationReportModel.setReportName("Remote Services");
            applicationReportModel.setDescription(REPORT_DESCRIPTION);
            applicationReportModel.setReportIconClass("glyphicon service-nav-logo");
            applicationReportModel.setProjectModel(projectModel);
            applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
            applicationReportModel.setTemplateType(TemplateType.FREEMARKER);
        }
        return applicationReportModel;
    }
}
