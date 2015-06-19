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
import org.jboss.windup.rules.apps.javaee.model.EjbRemoteServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JaxRSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JaxWSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.jboss.windup.rules.apps.javaee.model.RemoteServiceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of EJB data (eg, a list of EJB session beans).
 *
 */
public class CreateRemoteReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/remote.ftl";

    public CreateRemoteReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateRemoteReportRuleProvider.class, "Create Remote Service Report")
                    .setPhase(ReportGenerationPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
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

                            // reference to input project model
                            ProjectModel projectModel = configurationModel.getInputPath().getProjectModel();
                            createReport(event.getGraphContext(), projectModel);
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
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(300);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Remote Services");
        applicationReportModel.setReportIconClass("glyphicon service-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        GraphService<RemoteServiceModel> remoteServices = new GraphService<RemoteServiceModel>(context, RemoteServiceModel.class);
        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

        WindupVertexListModel<JaxRSWebServiceModel> jaxRsList = listService.create();
        WindupVertexListModel<JaxWSWebServiceModel> jaxWsList = listService.create();
        WindupVertexListModel<EjbRemoteServiceModel> ejbRemoteList = listService.create();
        WindupVertexListModel<RMIServiceModel> rmiList = listService.create();

        for (RemoteServiceModel remoteServiceModel : remoteServices.findAll())
        {
            if (remoteServiceModel instanceof JaxRSWebServiceModel)
            {
                jaxRsList.addItem((JaxRSWebServiceModel)remoteServiceModel);
            }
            else if (remoteServiceModel instanceof JaxWSWebServiceModel)
            {
                jaxWsList.addItem((JaxWSWebServiceModel)remoteServiceModel);
            }
            else if (remoteServiceModel instanceof EjbRemoteServiceModel)
            {
            	ejbRemoteList.addItem((EjbRemoteServiceModel)remoteServiceModel);
            }
            else if (remoteServiceModel instanceof RMIServiceModel)
            {
            	rmiList.addItem((RMIServiceModel)remoteServiceModel);
            }
        }

        Map<String, WindupVertexFrame> data = new HashMap<>(4);
        data.put("jaxRsServices", jaxRsList);
        data.put("jaxWsServices", jaxWsList);
        data.put("ejbRemoteServices", ejbRemoteList);
        data.put("rmiServices", rmiList);
        applicationReportModel.setRelatedResource(data);

        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "remotereport_" + projectModel.getName(), "html");
    }
}
