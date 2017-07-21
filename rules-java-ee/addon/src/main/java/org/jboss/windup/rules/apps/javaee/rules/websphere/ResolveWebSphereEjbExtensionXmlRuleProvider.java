package org.jboss.windup.rules.apps.javaee.rules.websphere;


import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.rules.DiscoverEjbConfigurationXmlRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.VendorSpecificationExtensionService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers WebSphere EJB Extension XML files and parses the related metadata
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * @author <a href="mailto:mnovotny@redhat.com">Marek Novotny</a>
 * 
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverEjbConfigurationXmlRuleProvider.class, perform = "Discover WebSphere EJB XML Files")
public class ResolveWebSphereEjbExtensionXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "ibm-ejb-jar-ext.xmi")
                    .withProperty(XmlFileModel.ROOT_TAG_NAME, "EJBJarExtension");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        
        ClassificationModel classificationModel = classificationService.attachClassification(event, context, payload, IssueCategoryRegistry.MANDATORY,
                    "WebSphere EJB extension descriptor (ibm-ejb-jar-ext)",
                    "WebSphere Enterprise Java Bean Extension XML Descriptor is used to specify extensions to be (de-)activated in the EJB Container."
                                + " \n "
                                + "JBoss EAP uses Java EE `jboss-ejb.xml` file descriptor or EAP specific `jboss-ejb3.xml` descriptor file. EJB 3.2 doesn't require descriptor file to be in deployment.");
        classificationModel.setEffort(3);
        
        GraphContext graphContext = event.getGraphContext();
        LinkService linkService = new LinkService(graphContext);
        
        LinkModel link = linkService.create();
        link.setDescription("Websphere AS - EJB 3.0 application bindings overview");
        link.setLink("https://www.ibm.com/support/knowledgecenter/en/SSAW57_7.0.0/com.ibm.websphere.nd.doc/info/ae/ae/cejb_bindingsejbfp.html");

        classificationService.attachLink(classificationModel, link);
        
        LinkModel ejb3RefLink = linkService.create();
        ejb3RefLink.setDescription("EAP 7 - jboss-ejb3.xml Deployment Descriptor Reference");
        ejb3RefLink.setLink("https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.0/html-single/developing_ejb_applications/#jboss_ejb3_xml_deployment_descriptor_reference");
        classificationService.attachLink(classificationModel, ejb3RefLink);
        
        LinkModel ejb3RefLink_ = linkService.create();
        ejb3RefLink_.setDescription("EAP 6 - jboss-ejb3.xml Deployment Descriptor Reference");
        ejb3RefLink_.setLink("https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6/html-single/Development_Guide/index.html#jboss-ejb3xml_Deployment_Descriptor_Reference");
        classificationService.attachLink(classificationModel, ejb3RefLink_);
        
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(payload, "WebSphere EJB Ext", TechnologyTagLevel.IMPORTANT);

        VendorSpecificationExtensionService vendorSpecificationService = new VendorSpecificationExtensionService(event.getGraphContext());
        vendorSpecificationService.associateAsVendorExtension(payload, "ejb-jar.xml");
    }

}
