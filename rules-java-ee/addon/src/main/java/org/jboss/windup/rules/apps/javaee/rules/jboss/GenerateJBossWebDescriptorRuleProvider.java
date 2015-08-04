package org.jboss.windup.rules.apps.javaee.rules.jboss;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;
import org.jboss.windup.rules.apps.javaee.model.association.VendorSpecificationExtensionModel;
import org.jboss.windup.rules.apps.javaee.service.VendorSpecificationExtensionService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a jboss-ejb3.xml for jndi bindings
 *
 */
public class GenerateJBossWebDescriptorRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logger.getLogger(GenerateJBossWebDescriptorRuleProvider.class.getSimpleName());
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/jboss/jboss-web.ftl";

    public GenerateJBossWebDescriptorRuleProvider()
    {
        super(MetadataBuilder.forProvider(GenerateJBossWebDescriptorRuleProvider.class, "Generate jboss-web.xml")
                    .setPhase(MigrationRulesPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
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

                            // reference to input project model
                            ProjectModel projectModel = configurationModel.getInputPath().getProjectModel();
                            createReport(context, event.getGraphContext(), projectModel);
                        }

                        @Override
                        public String toString()
                        {
                            return "Generate jboss-web.xml";
                        }
                    });

    }

    private void createReport(EvaluationContext evaluationContext, GraphContext context, ProjectModel projectModel)
    {
        GraphService<WebXmlModel> webDescriptors = new GraphService<>(context, WebXmlModel.class);
        ClassificationService classificationService = new ClassificationService(context);
        LinkService linkService = new LinkService(context);
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        VendorSpecificationExtensionService vendorSpecificService = new VendorSpecificationExtensionService(context);

        for (WebXmlModel webDescriptor : webDescriptors.findAll())
        {
            ApplicationReportModel applicationReportModel = applicationReportService.create();
            applicationReportModel.setReportPriority(300);
            applicationReportModel.setDisplayInApplicationReportIndex(false);
            applicationReportModel.setReportName("jboss-web.xml");
            applicationReportModel.setProjectModel(projectModel);
            applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
            applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

            GraphService<WindupVertexListModel> listService = new GraphService<WindupVertexListModel>(context, WindupVertexListModel.class);

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

            for (ClassificationModel classificationModel : classificationService.getClassificationByName(webDescriptor, "Web XML"))
            {
                LinkModel link = linkService.create();
                link.setDescription("JBoss Web XML Descriptor - Generated by Windup");
                link.setLink(applicationReportModel.getReportFilename());

                classificationModel.addLink(link);
            }

            LinkModel generatedDescriptor = linkService.create();
            generatedDescriptor.setDescription("JBoss Web XML Descriptor - Generated by Windup");
            generatedDescriptor.setLink(applicationReportModel.getReportFilename());

            // link up with the generated XML
            for (ClassificationModel classificationModel : classificationService.getClassificationByName(webDescriptor, "Web XML"))
            {
                classificationService.attachLink(classificationModel, generatedDescriptor);
            }

            for (VendorSpecificationExtensionModel vendorSpecificExtension : vendorSpecificService.getVendorSpecificationExtensions(webDescriptor))
            {
                LOG.info("Vendor specific: " + vendorSpecificExtension.getFileName());
                ClassificationModel classification = classificationService.attachClassification(evaluationContext, vendorSpecificExtension,
                            "Web Specification Extension", "Vendor Specific Web Specification Extension");
                classificationService.attachLink(classification, generatedDescriptor);
            }
        }
    }
}
