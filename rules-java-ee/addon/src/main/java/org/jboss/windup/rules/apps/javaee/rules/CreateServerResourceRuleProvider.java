package org.jboss.windup.rules.apps.javaee.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsConnectionFactoryModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.service.DataSourceService;
import org.jboss.windup.rules.apps.javaee.service.JNDIResourceService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of Server Resources within the application (eg, datasources, jms resources).
 *
 */
public class CreateServerResourceRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logging.get(CreateServerResourceRuleProvider.class);

    public static final String TEMPLATE_JPA_REPORT = "/reports/templates/server.ftl";

    public CreateServerResourceRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateServerResourceRuleProvider.class, "Create Server Resource Report")
                    .setPhase(ReportGenerationPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder resourceModelsFound = Query.fromType(JNDIResourceModel.class);

        GraphOperation addReport = new GraphOperation()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                ProjectModel projectModel = windupConfiguration.getInputPath().getProjectModel();
                if (projectModel == null)
                {
                    throw new WindupException("Error, no project found in: " + windupConfiguration.getInputPath().getFilePath());
                }
                createJNDIReport(event.getGraphContext(), projectModel);
            }

            @Override
            public String toString()
            {
                return "CreateJPAReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(resourceModelsFound)
                    .perform(addReport);
    }

    private void createJNDIReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(400);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Server Resources");
        applicationReportModel.setReportIconClass("glyphicon server-resource-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_JPA_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        DataSourceService datasourceService = new DataSourceService(context);
        JNDIResourceService jndiResourceService = new JNDIResourceService(context);
        GraphService<WindupVertexListModel> listService = new GraphService<WindupVertexListModel>(context, WindupVertexListModel.class);

        WindupVertexListModel datasourceList = listService.create();
        WindupVertexListModel jmsList = listService.create();
        WindupVertexListModel jmsConnectionFactoryList = listService.create();
        WindupVertexListModel otherJndiList = listService.create();

        for (JNDIResourceModel jndi : jndiResourceService.findAll())
        {
            if (jndi instanceof DataSourceModel)
            {
                datasourceList.addItem(jndi);
            }
            else if (jndi instanceof JmsDestinationModel)
            {
                jmsList.addItem(jndi);
            }
            else if (jndi instanceof JmsConnectionFactoryModel)
            {
                jmsConnectionFactoryList.addItem(jndi);
            }
            else
            {
                otherJndiList.addItem(jndi);
            }
        }

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(2);
        additionalData.put("datasources", datasourceList);
        additionalData.put("jmsDestinations", jmsList);
        additionalData.put("jmsConnectionFactories", jmsConnectionFactoryList);
        additionalData.put("otherResources", otherJndiList);
        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "server_resource_" + projectModel.getName(), "html");
    }
}
