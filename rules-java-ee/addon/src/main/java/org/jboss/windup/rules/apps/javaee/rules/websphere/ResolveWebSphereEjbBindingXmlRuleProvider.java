package org.jboss.windup.rules.apps.javaee.rules.websphere;

import static org.joox.JOOX.$;

import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.rules.DiscoverEjbConfigurationXmlRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.EnvironmentReferenceService;
import org.jboss.windup.rules.apps.javaee.service.JNDIResourceService;
import org.jboss.windup.rules.apps.javaee.service.JmsDestinationService;
import org.jboss.windup.rules.apps.javaee.service.VendorSpecificationExtensionService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers WebSphere EJB XML files and parses the related metadata
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * @author <a href="mailto:mnovotny@redhat.com">Marek Novotny</a>
 * 
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverEjbConfigurationXmlRuleProvider.class, perform = "Discover WebSphere EJB XML Files")
public class ResolveWebSphereEjbBindingXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveWebSphereEjbBindingXmlRuleProvider.class.getName());

    @Override
    public ConditionBuilder when()
    {

        return Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "ibm-ejb-jar-bnd.xmi")
                    .withProperty(XmlFileModel.ROOT_TAG_NAME, "EJBJarBinding");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        GraphContext graphContext = event.getGraphContext();
        
        EnvironmentReferenceService envRefService = new EnvironmentReferenceService(graphContext);

        XmlFileService xmlFileService = new XmlFileService(graphContext);
        JNDIResourceService jndiResourceService = new JNDIResourceService(graphContext);
        JmsDestinationService jmsDestinationService = new JmsDestinationService(graphContext);
        GraphService<EjbSessionBeanModel> ejbSessionBeanService = new GraphService<>(graphContext, EjbSessionBeanModel.class);
        GraphService<EjbMessageDrivenModel> mdbService = new GraphService<>(graphContext, EjbMessageDrivenModel.class);

        // Not Removed as per WINDUPRULE-214 - it duplicated GenerateJBossEjbDescriptorRuleProvider which reacts to associateAsVendorExtension() below.
        ClassificationService classificationService = new ClassificationService(graphContext);
        ClassificationModel classification = classificationService.attachClassification(event, context, payload, IssueCategoryRegistry.MANDATORY, "WebSphere EJB binding descriptor (ibm-ejb-jar-bnd)",
                    "WebSphere Enterprise Java Bean Binding XML Descriptor describes how to bind enterprise beans or its resources. For instance EJB JNDI or data sources for entity beans."
                    + " \n Red Hat JBoss EAP uses standard Java EE annotations or deployment descriptors like `ejb-jar.xml` or `jboss-ejb3.xml`. Please read JBoss EAP 7 documentation.");
        classification.setEffort(3);
        IssueCategoryModel cat = IssueCategoryRegistry.loadFromGraph(graphContext, IssueCategoryRegistry.MANDATORY);
        classification.setIssueCategory(cat);

        LinkService linkService = new LinkService(graphContext);
        LinkModel link = linkService.create();
        link.setDescription("Websphere AS - Application bindings");
        link.setLink("https://www.ibm.com/support/knowledgecenter/en/SSAW57_8.0.0/com.ibm.websphere.nd.doc/info/ae/ae/crun_app_bindings.html#crun_app_bindings__timbindings");
        classificationService.attachLink(classification, link);

        LinkModel eap7Link = linkService.create();
        eap7Link.setDescription("EAP 7 - Developing EJB Applications");
        eap7Link.setLink("https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.0/html-single/developing_ejb_applications/");
        classificationService.attachLink(classification, eap7Link);
        
        TechnologyTagService technologyTagService = new TechnologyTagService(graphContext);
        technologyTagService.addTagToFileModel(payload, "WebSphere EJB", TechnologyTagLevel.IMPORTANT);

        Document doc = xmlFileService.loadDocumentQuiet(event, context, payload);
        if (doc == null)
            return;

        VendorSpecificationExtensionService vendorSpecificationService = new VendorSpecificationExtensionService(graphContext);
        // mark as vendor extension; create reference to ejb-jar.xml
        vendorSpecificationService.associateAsVendorExtension(payload, "ejb-jar.xml");

        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(graphContext, payload.getProjectModel());

        // register beans to JNDI
        for (Element resourceRef : $(doc).find("ejbBindings").get())
        {
            String href = $(resourceRef).child("enterpriseBean").attr("href");
            String resourceId = StringUtils.substringAfterLast(href, "ejb-jar.xml#");
            String jndiLocation = $(resourceRef).attr("jndiName");

            // determine type:
            String type = $(resourceRef).child("enterpriseBean").attr("type");
            LOG.info("enterpriseBean type: " + type);

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceId))
            {
                JNDIResourceModel resource = jndiResourceService.createUnique(applications, jndiLocation);
                LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceId);
                // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
                for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.REFERENCE_ID, resourceId))
                {
                    envRefService.associateEnvironmentToJndi(resource, ref);
                }

                for (EjbSessionBeanModel ejb : ejbSessionBeanService.findAllByProperty(EjbSessionBeanModel.EJB_ID, resourceId))
                {
                    ejb.setGlobalJndiReference(resource);
                }
            }
        }

        // register beans to JNDI
        for (Element resourceRef : $(doc).find("resRefBindings").get())
        {
            processBinding(envRefService, jndiResourceService, applications, resourceRef, "bindingResourceRef");
        }
        for (Element resourceRef : $(doc).find("ejbRefBindings").get())
        {
            processBinding(envRefService, jndiResourceService, applications, resourceRef, "bindingEjbRef");
        }
        for (Element resourceRef : $(doc).find("messageDestinationRefBindings").get())
        {
            processBinding(envRefService, jndiResourceService, applications, resourceRef, "bindingMessageDestinationRef");
        }

        // Bind MDBs to Destinations
        for (Element resourceRef : $(doc).find("messageDestinationRefBindings").get())
        {
            String jndiLocation = $(resourceRef).attr("jndiName");

            // get the parent, as that has the reference to the MDB...
            String mdbRef = $(resourceRef).siblings("enterpriseBean").attr("href");
            String mdbId = StringUtils.substringAfterLast(mdbRef, "ejb-jar.xml#");

            if (StringUtils.isNotBlank(mdbId))
            {
                for (EjbMessageDrivenModel mdb : mdbService.findAllByProperty(EjbMessageDrivenModel.EJB_ID, mdbId))
                {
                    String destination = jndiLocation;
                    if (StringUtils.isNotBlank(destination))
                    {
                        JmsDestinationModel jndiRef = jmsDestinationService.createUnique(applications, destination);
                        mdb.setDestination(jndiRef);
                    }
                }
            }
        }

    }

    private void processBinding(EnvironmentReferenceService envRefService, JNDIResourceService jndiResourceService, Set<ProjectModel> applications,
                Element resourceRef, String tagName)
    {
        String href = $(resourceRef).child(tagName).attr("href");
        String resourceId = StringUtils.substringAfterLast(href, "ejb-jar.xml#");
        String jndiLocation = $(resourceRef).attr("jndiName");

        if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceId))
        {
            JNDIResourceModel resource = jndiResourceService.createUnique(applications, jndiLocation);
            LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceId);
            // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
            for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.REFERENCE_ID, resourceId))
            {
                envRefService.associateEnvironmentToJndi(resource, ref);
            }
        }
    }

}
