package org.jboss.windup.rules.apps.javaee.rules.jboss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;
import org.jboss.windup.rules.apps.javaee.model.association.VendorSpecificationExtensionModel;
import org.jboss.windup.rules.apps.javaee.service.VendorSpecificationExtensionService;
import org.jboss.windup.util.Util;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a jboss-ejb3.xml for jndi bindings
 */
@RuleMetadata(phase = MigrationRulesPhase.class, id = "Generate jboss-web.xml")
public class GenerateJBossWebDescriptorRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logger.getLogger(GenerateJBossWebDescriptorRuleProvider.class.getName());
    public static final String JBOSS_WEB_TEMPLATE = "/reports/templates/jboss/jboss-web.ftl";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
        .addRule()
        .when(Query.fromType(WebXmlModel.class))
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
                    transformWebXml(context, event.getGraphContext(), projectModel);
                }
            }

            @Override
            public String toString()
            {
                return "Generate jboss-web.xml";
            }
        });
    }

    private void transformWebXml(EvaluationContext evaluationContext, GraphContext context, ProjectModel projectModel)
    {
        LinkService linkService = new LinkService(context);
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        VendorSpecificationExtensionService vendorSpecificService = new VendorSpecificationExtensionService(context);

        for (WebXmlModel webDescriptor : findAllWebXmlsInProject(context,projectModel))
        {
            ApplicationReportModel applicationReportModel = applicationReportService.create();
            applicationReportModel.setReportPriority(300);
            applicationReportModel.setDisplayInApplicationReportIndex(false);
            applicationReportModel.setReportName("jboss-web.xml");
            applicationReportModel.setProjectModel(projectModel);
            applicationReportModel.setTemplatePath(JBOSS_WEB_TEMPLATE);
            applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

            GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

            WindupVertexListModel environmentReferences = listService.create();
            for (EnvironmentReferenceModel ev : webDescriptor.getEnvironmentReferences())
            {
                LOG.info("Reference: "+ev);
                environmentReferences.addItem(ev);
            }

            Map<String, WindupVertexFrame> additionalData = new HashMap<>(4);
            additionalData.put("environmentReferences", environmentReferences);
            applicationReportModel.setRelatedResource(additionalData);

            ReportService reportService = new ReportService(context);
            reportService.setUniqueFilename(applicationReportModel, "jboss-web_" + projectModel.getName(), "xml");

            LOG.info("Generated jboss-web.xml for " + webDescriptor.getFilePath() + " at: " + applicationReportModel.getReportFilename());
            LinkModel link = linkService.create();
            link.setDescription("JBoss Web XML Descriptor - Generated by " + Util.WINDUP_BRAND_NAME_LONG);
            link.setLink(applicationReportModel.getReportFilename());

            webDescriptor.addLinkToTransformedFile(link);

            LinkModel generatedDescriptor = linkService.create();
            generatedDescriptor.setDescription("JBoss Web XML Descriptor - Generated by " + Util.WINDUP_BRAND_NAME_LONG);
            generatedDescriptor.setLink(applicationReportModel.getReportFilename());

            for (VendorSpecificationExtensionModel vendorSpecificExtension : vendorSpecificService.getVendorSpecificationExtensions(webDescriptor))
            {
                LOG.info("Vendor specific: " + vendorSpecificExtension.getFileName());
                vendorSpecificExtension.addLinkToTransformedFile(generatedDescriptor);
            }
        }
    }

    private Iterable<WebXmlModel> findAllWebXmlsInProject(GraphContext context, ProjectModel projectModel)
    {
        GraphService<WebXmlModel> webDescriptors = new GraphService<>(context, WebXmlModel.class);
        List<WebXmlModel> resultModels = new ArrayList<>();
        for (WebXmlModel webXmlModel : webDescriptors.findAll())
        {
            if(webXmlModel.getProjectModel().getRootProjectModel().equals(projectModel))
            {
                resultModels.add(webXmlModel);
            }

        }
        return resultModels;
    }

}
