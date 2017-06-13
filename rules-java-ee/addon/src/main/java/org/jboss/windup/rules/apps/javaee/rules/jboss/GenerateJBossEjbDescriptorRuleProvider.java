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
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.model.association.VendorSpecificationExtensionModel;
import org.jboss.windup.rules.apps.javaee.service.VendorSpecificationExtensionService;
import org.jboss.windup.util.Util;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a jboss-ejb3.xml for JNDI bindings.
 */
@RuleMetadata(phase = MigrationRulesPhase.class, id = "Generate jboss-ejb3.xml")
public class GenerateJBossEjbDescriptorRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logger.getLogger(GenerateJBossEjbDescriptorRuleProvider.class.getName());
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/jboss/jboss-ejb3.ftl";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
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

                for (FileModel inputPath : configurationModel.getInputPaths())
                {
                    ProjectModel application = inputPath.getProjectModel();
                    createReport(event, context, event.getGraphContext(), application);
                }
            }

            @Override
            public String toString()
            {
                return "Generate jboss-ejb3.xml";
            }
        });
    }

    private void createReport(GraphRewrite event, EvaluationContext evaluationContext, GraphContext context, ProjectModel projectModel)
    {
        ClassificationService classificationService = new ClassificationService(context);
        VendorSpecificationExtensionService vendorSpecificService = new VendorSpecificationExtensionService(context);
        LinkService linkService = new LinkService(context);

        for (EjbDeploymentDescriptorModel ejbDescriptor : findAllEjbDescsInProject(context,projectModel))
        {
            ApplicationReportService applicationReportService = new ApplicationReportService(context);
            ApplicationReportModel applicationReportModel = applicationReportService.create();
            applicationReportModel.setReportPriority(300);
            applicationReportModel.setDisplayInApplicationReportIndex(false);
            applicationReportModel.setReportName("jboss-ejb3.xml");
            applicationReportModel.setProjectModel(projectModel);
            applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
            applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

            GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

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

            LinkModel newDescriptorLink = linkService.create();
            newDescriptorLink.setDescription("JBoss EJB XML Descriptor - Generated by " + Util.WINDUP_BRAND_NAME_LONG);
            newDescriptorLink.setLink(applicationReportModel.getReportFilename());

            for (VendorSpecificationExtensionModel vendorSpecificExtension : vendorSpecificService.getVendorSpecificationExtensions(ejbDescriptor))
            {
                LOG.info("Vendor specific: " + vendorSpecificExtension.getFileName());
                //classificationService.attachClassification(event, evaluationContext, vendorSpecificExtension, "EJB Specification Extension",
                //            "Vendor Specific EJB Specification Extension");
                vendorSpecificExtension.addLinkToTransformedFile(newDescriptorLink);
                vendorSpecificExtension.setGenerateSourceReport(true);
            }

            // Link up with the generated XML.
            ejbDescriptor.addLinkToTransformedFile(newDescriptorLink);
        }

    }

    private Iterable<EjbDeploymentDescriptorModel> findAllEjbDescsInProject(GraphContext context, ProjectModel application)
    {
        GraphService<EjbDeploymentDescriptorModel> ejbDescriptorService = new GraphService<>(context, EjbDeploymentDescriptorModel.class);
        List<EjbDeploymentDescriptorModel> resultModels = new ArrayList<>();
        for (EjbDeploymentDescriptorModel ejbDesc : ejbDescriptorService.findAll())
        {
            if(ejbDesc.getProjectModel().getRootProjectModel().equals(application))
            {
                resultModels.add(ejbDesc);
            }
        }
        return resultModels;
    }
}
