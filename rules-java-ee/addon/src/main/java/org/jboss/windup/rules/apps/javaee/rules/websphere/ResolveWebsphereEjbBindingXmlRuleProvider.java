package org.jboss.windup.rules.apps.javaee.rules.websphere;

import static org.joox.JOOX.$;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
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
 * Discovers Websphere EJB XML files and parses the related metadata
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class ResolveWebsphereEjbBindingXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveWebsphereEjbBindingXmlRuleProvider.class.getSimpleName());

    public ResolveWebsphereEjbBindingXmlRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveWebsphereEjbBindingXmlRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(DiscoverEjbConfigurationXmlRuleProvider.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover Weblogic EJB XML Files";
    }

    @Override
    public ConditionBuilder when()
    {

        return Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "ibm-ejb-jar-bnd.xmi")
                    .withProperty(XmlFileModel.ROOT_TAG_NAME, "EJBJarBinding");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        EnvironmentReferenceService envRefService = new EnvironmentReferenceService(event.getGraphContext());

        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        JNDIResourceService jndiResourceService = new JNDIResourceService(event.getGraphContext());
        JmsDestinationService jmsDestinationService = new JmsDestinationService(event.getGraphContext());
        GraphService<EjbSessionBeanModel> ejbSessionBeanService = new GraphService<>(event.getGraphContext(), EjbSessionBeanModel.class);
        GraphService<EjbMessageDrivenModel> mdbService = new GraphService<>(event.getGraphContext(), EjbMessageDrivenModel.class);

         
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        ClassificationModel classification = classificationService.attachClassification(context, payload, "Websphere EJB Binding", "Websphere Enterprise Java Bean Binding XML Descriptor");
        classification.setEffort(1);
        
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(payload, "Websphere EJB", TechnologyTagLevel.IMPORTANT);

        Document doc = xmlFileService.loadDocumentQuiet(context, payload);

        if(doc == null) {
            return;
        }

        VendorSpecificationExtensionService vendorSpecificationService = new VendorSpecificationExtensionService(event.getGraphContext());
        //mark as vendor extension; create reference to ejb-jar.xml
        vendorSpecificationService.associateAsVendorExtension(payload, "ejb-jar.xml");
        
        // register beans to JNDI
        for (Element resourceRef : $(doc).find("ejbBindings").get())
        {
            String href = $(resourceRef).child("enterpriseBean").attr("href");
            String resourceId = StringUtils.substringAfterLast(href, "ejb-jar.xml#");
            String jndiLocation = $(resourceRef).attr("jndiName");
            
            //determine type:
            String type = $(resourceRef).child("enterpriseBean").attr("type");
            
            LOG.info("Type: "+type);

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceId))
            {
                JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);
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
            processBinding(envRefService, jndiResourceService, resourceRef, "bindingResourceRef");
        }
        for (Element resourceRef : $(doc).find("ejbRefBindings").get())
        {
            processBinding(envRefService, jndiResourceService, resourceRef, "bindingEjbRef");
        }
        for (Element resourceRef : $(doc).find("messageDestinationRefBindings").get())
        {
            processBinding(envRefService, jndiResourceService, resourceRef, "bindingMessageDestinationRef");
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
                        JmsDestinationModel jndiRef = jmsDestinationService.createUnique(destination);
                        mdb.setDestination(jndiRef);
                    }
                }
            }
        }

    }
  
    
    private void processBinding(EnvironmentReferenceService envRefService, JNDIResourceService jndiResourceService, Element resourceRef, String tagName)
    {
        String href = $(resourceRef).child(tagName).attr("href");
        String resourceId = StringUtils.substringAfterLast(href, "ejb-jar.xml#");
        String jndiLocation = $(resourceRef).attr("jndiName");

        if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceId))
        {
            JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);
            LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceId);
            // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
            for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.REFERENCE_ID, resourceId))
            {
                envRefService.associateEnvironmentToJndi(resource, ref);
            }
        }
    }

}
