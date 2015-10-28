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
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsConnectionFactoryModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.ThreadPoolModel;
import org.jboss.windup.rules.apps.javaee.service.JNDIResourceService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of Server Resources within the application (eg, datasources, jms resources).
 *
 */
public class CreateServerResourceReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_JPA_REPORT = "/reports/templates/server.ftl";

    public CreateServerResourceReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateServerResourceReportRuleProvider.class, "Create Server Resource Report")
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
                    createServerResourcesReport(event.getGraphContext(), projectModel);
                }
            }

            @Override
            public String toString()
            {
                return "CreateServerResourcesReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(addReport);
    }

    private void createServerResourcesReport(GraphContext context, ProjectModel projectModel)
    {
        JNDIResourceService jndiResourceService = new JNDIResourceService(context);
        GraphService<ThreadPoolModel> threadPoolService = new GraphService<>(context, ThreadPoolModel.class);

        List<DataSourceModel> datasourceList = new ArrayList<>();
        List<JmsDestinationModel> jmsList = new ArrayList<>();
        List<JmsConnectionFactoryModel> jmsConnectionFactoryList = new ArrayList<>();
        List<JNDIResourceModel> otherJndiList = new ArrayList<>();
        List<ThreadPoolModel> threadPoolList = new ArrayList<>();

        for (JNDIResourceModel jndi : jndiResourceService.findAll())
        {
            if (!jndi.isAssociatedWithApplication(projectModel))
                continue;

            if (jndi instanceof DataSourceModel)
            {
                datasourceList.add((DataSourceModel) jndi);
            }
            else if (jndi instanceof JmsDestinationModel)
            {
                jmsList.add((JmsDestinationModel) jndi);
            }
            else if (jndi instanceof JmsConnectionFactoryModel)
            {
                jmsConnectionFactoryList.add((JmsConnectionFactoryModel) jndi);
            }
            else
            {
                otherJndiList.add(jndi);
            }
        }

        for (ThreadPoolModel tp : threadPoolService.findAll())
        {
            if (tp.getApplication().equals(projectModel))
                threadPoolList.add(tp);
        }

        if (datasourceList.isEmpty() && jmsList.isEmpty() && jmsConnectionFactoryList.isEmpty() && otherJndiList.isEmpty()
                    && threadPoolList.isEmpty())
            return;

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(400);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Server Resources");
        applicationReportModel.setReportIconClass("glyphicon server-resource-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_JPA_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(2);
        additionalData.put("datasources", listService.create().addAll(datasourceList));
        additionalData.put("jmsDestinations", listService.create().addAll(jmsList));
        additionalData.put("jmsConnectionFactories", listService.create().addAll(jmsConnectionFactoryList));
        additionalData.put("otherResources", listService.create().addAll(otherJndiList));
        additionalData.put("threadPools", listService.create().addAll(threadPoolList));
        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "server_resource_" + projectModel.getName(), "html");
    }
}
