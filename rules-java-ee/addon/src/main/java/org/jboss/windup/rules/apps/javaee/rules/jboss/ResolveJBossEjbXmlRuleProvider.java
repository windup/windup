package org.jboss.windup.rules.apps.javaee.rules.jboss;

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
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers JBoss EJB XML files and parses the related metadata Handles XML files prior to EAP 6. (jboss-ejb.xml)
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class ResolveJBossEjbXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveJBossEjbXmlRuleProvider.class.getSimpleName());

    public ResolveJBossEjbXmlRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveJBossEjbXmlRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(DiscoverEjbConfigurationXmlRuleProvider.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover JBoss EJB XML Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "ejb-jar")
                    .withProperty(FileModel.FILE_NAME, "jboss-ejb3.xml");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        // https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6/html/Development_Guide/jboss-ejb3xml_Deployment_Descriptor_Reference.html
        // check the root tag to make sure it is enterprise-beans
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        Document doc = xmlFileService.loadDocumentQuiet(payload);

        if ($(doc).find("enterprise-beans").isEmpty())
        {
            LOG.warning("Expected enterprise-beans tag. Ignoring: " + payload.getFileName());
            return;
        }

        EnvironmentReferenceService envRefService = new EnvironmentReferenceService(event.getGraphContext());
        JNDIResourceService jndiResourceService = new JNDIResourceService(event.getGraphContext());
        JmsDestinationService jmsDestinationService = new JmsDestinationService(event.getGraphContext());

        GraphService<EjbSessionBeanModel> ejbSessionBeanService = new GraphService<>(event.getGraphContext(), EjbSessionBeanModel.class);
        GraphService<EjbMessageDrivenModel> mdbService = new GraphService<>(event.getGraphContext(), EjbMessageDrivenModel.class);

        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        classificationService.attachClassification(payload, "JBoss Legacy EJB XML", "JBoss Enterprise Java Bean XML Descriptor prior to EAP 6.");

        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(payload, "JBoss EJB XML", TechnologyTagLevel.IMPORTANT);

        // handle resource-ref
        for (Element resourceRef : $(doc).find("enterprise-beans").find("resource-ref").get())
        {
            String resourceRefName = $(resourceRef).child("res-ref-name").text();
            String lookupLocation = $(resourceRef).child("lookup-name").text();
            String jndiLocation = $(resourceRef).child("jndi-name").text();

            if (StringUtils.isBlank(jndiLocation))
            {
                jndiLocation = lookupLocation;
            }

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceRefName))
            {
                JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);
                LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceRefName);
                // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
                for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceRefName))
                {
                    envRefService.associateEnvironmentToJndi(event, resource, ref);
                }
            }
        }

        // handle resource-env-ref
        for (Element resourceRef : $(doc).find("enterprise-beans").find("resource-env-ref").get())
        {
            String resourceRefName = $(resourceRef).child("resource-env-ref-name").text();
            String lookupLocation = $(resourceRef).child("lookup-name").text();
            String jndiLocation = $(resourceRef).child("jndi-name").text();

            if (StringUtils.isBlank(jndiLocation))
            {
                jndiLocation = lookupLocation;
            }

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceRefName))
            {
                JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);
                LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceRefName);
                // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
                for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceRefName))
                {
                    envRefService.associateEnvironmentToJndi(event, resource, ref);
                }
            }
        }

        // bind the MDBs to the JMS Destination.
        for (Element messageDrivenRef : $(doc).find("enterprise-beans").find("message-driven").get())
        {
            // register the EJB to the JNDI location, if it exists.
            String ejbName = $(messageDrivenRef).child("ejb-name").text();
            String destination = null;

            for (Element activationConfigProperty : $(messageDrivenRef).find("activation-config-property"))
            {
                String name = $(activationConfigProperty).child("activation-config-property-name").text();
                String value = $(activationConfigProperty).child("activation-config-property-value").text();

                if (StringUtils.equals("destination", name))
                {
                    destination = value;
                }

            }

            if (StringUtils.isNotBlank(ejbName))
            {
                for (EjbMessageDrivenModel mdb : mdbService.findAllByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, ejbName))
                {
                    if (StringUtils.isNotBlank(destination))
                    {
                        JmsDestinationModel jndiRef = jmsDestinationService.createUnique(destination);
                        mdb.setDestination(jndiRef);
                    }
                }
            }
        }

    }
}
