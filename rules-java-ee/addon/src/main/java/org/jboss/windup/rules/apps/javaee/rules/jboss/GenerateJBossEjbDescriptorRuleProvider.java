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
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.model.association.VendorSpecificationExtensionModel;
import org.jboss.windup.rules.apps.javaee.service.VendorSpecificationExtensionService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a jboss-ejb3.xml for jndi bindings
 *
 */
public class GenerateJBossEjbDescriptorRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logger.getLogger(GenerateJBossEjbDescriptorRuleProvider.class.getSimpleName());
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/jboss/jboss-ejb3.ftl";

    public GenerateJBossEjbDescriptorRuleProvider()
    {
        super(MetadataBuilder.forProvider(GenerateJBossEjbDescriptorRuleProvider.class, "Generate jboss-ejb3.xml")
                    .setPhase(MigrationRulesPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(EjbDeploymentDescriptorModel.class))
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
                            return "Generate jboss-ejb3.xml";
                        }
                    });

    }

    private void createReport(GraphContext context, ProjectModel projectModel)
    {
        GraphService<EjbDeploymentDescriptorModel> ejbDescriptors = new GraphService<>(context, EjbDeploymentDescriptorModel.class);
        ClassificationService classificationService = new ClassificationService(context);
        VendorSpecificationExtensionService vendorSpecificService = new VendorSpecificationExtensionService(context);
        LinkService linkService = new LinkService(context);

        for (EjbDeploymentDescriptorModel ejbDescriptor : ejbDescriptors.findAll())
        {
            ApplicationReportService applicationReportService = new ApplicationReportService(context);
            ApplicationReportModel applicationReportModel = applicationReportService.create();
            applicationReportModel.setReportPriority(300);
            applicationReportModel.setDisplayInApplicationReportIndex(false);
            applicationReportModel.setReportName("jboss-ejb3.xml");
            applicationReportModel.setProjectModel(projectModel);
            applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
            applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

            GraphService<WindupVertexListModel> listService = new GraphService<WindupVertexListModel>(context, WindupVertexListModel.class);

            WindupVertexListModel sessionBeans = listService.create();
            for (EjbSessionBeanModel sb : ejbDescriptor.getEjbSessionBeans())
            {
                sessionBeans.addItem(sb);
            }

            WindupVertexListModel messageDrivenBeans = listService.create();
            for (EjbMessageDrivenModel mb : ejbDescriptor.getMessageDriven())
            {
                messageDrivenBeans.addItem(mb);
            }

            Map<String, WindupVertexFrame> additionalData = new HashMap<>(4);
            additionalData.put("sessionBeans", sessionBeans);
            additionalData.put("messageDriven", messageDrivenBeans);
            applicationReportModel.setRelatedResource(additionalData);

            ReportService reportService = new ReportService(context);
            reportService.setUniqueFilename(applicationReportModel, "jboss-ejb3_" + projectModel.getName(), "xml");

            LOG.info("Generated jboss-ejb3.xml for " + ejbDescriptor.getFilePath() + " at: " + applicationReportModel.getReportFilename());

            LinkModel generatedDescriptor = linkService.create();
            generatedDescriptor.setDescription("JBoss EJB XML Descriptor - Generated by Windup");
            generatedDescriptor.setLink(applicationReportModel.getReportFilename());

            //link up with the generated XML
            for (ClassificationModel classificationModel : classificationService.getClassificationByName(ejbDescriptor, "EJB XML"))
            {
                classificationService.attachLink(classificationModel, generatedDescriptor);
            }
            
            for (VendorSpecificationExtensionModel vendorSpecificExtension : vendorSpecificService.getVendorSpecificationExtensions(ejbDescriptor)) {
                LOG.info("Vendor specific: "+vendorSpecificExtension.getFileName());
                ClassificationModel classification = classificationService.attachClassification(vendorSpecificExtension, "EJB Specification Extension", "Vendor Specific EJB Specification Extension");
                classificationService.attachLink(classification, generatedDescriptor);
            }
        }

    }
}
