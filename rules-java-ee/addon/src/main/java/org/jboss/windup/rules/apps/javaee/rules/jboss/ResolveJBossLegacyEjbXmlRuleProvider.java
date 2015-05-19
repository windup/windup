package org.jboss.windup.rules.apps.javaee.rules.jboss;

import static org.joox.JOOX.$;

import java.util.HashMap;
import java.util.Map;
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
 * Discovers JBoss EJB XML files and parses the related metadata Handles XML files prior to EAP 6. (jboss.xml)
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class ResolveJBossLegacyEjbXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveJBossLegacyEjbXmlRuleProvider.class.getSimpleName());

    public ResolveJBossLegacyEjbXmlRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveJBossLegacyEjbXmlRuleProvider.class)
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
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "jboss").withProperty(FileModel.FILE_NAME, "jboss.xml");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
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

        // first, find all resource managers for later resolution.
        Map<String, String> resourceManagerReferences = new HashMap<>();
        for (Element resourceRef : $(doc).find("resource-managers").children("resource-manager").get())
        {
            String resourceName = $(resourceRef).child("res-name").text();
            String resourceJNDI = $(resourceRef).child("res-jndi-name").text();
            resourceManagerReferences.put(resourceName, resourceJNDI);
            LOG.info("Found Resource Manager: " + resourceName + ", " + resourceJNDI);
        }

        // handle resource-ref
        for (Element resourceRef : $(doc).find("resource-ref").get())
        {
            String jndiLocation = $(resourceRef).child("jndi-name").text();
            String resourceRefName = $(resourceRef).child("res-ref-name").text();
            String resourceName = $(resourceRef).child("resource-name").text();

            // resolve jndilocation from resourcename...: https://docs.jboss.org/ejb3/app-server/tutorial/jboss_resource_ref/META-INF/jboss.xml
            if (StringUtils.isBlank(jndiLocation) && StringUtils.isNotBlank(resourceName))
            {
                jndiLocation = resourceManagerReferences.get(resourceName);
            }

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceRefName))
            {
                JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);
                LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceRefName);
                // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
                for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceRefName))
                {
                    envRefService.associateEnvironmentToJndi(resource, ref);
                }
            }
        }

        // handle resource-env-ref
        for (Element resourceRef : $(doc).find("resource-env-ref").get())
        {
            String jndiLocation = $(resourceRef).child("jndi-name").text();
            String resourceRefName = $(resourceRef).child("resource-env-ref-name").text();
            String resourceName = $(resourceRef).child("resource-name").text();

            // resolve jndilocation from resourcename...: https://docs.jboss.org/ejb3/app-server/tutorial/jboss_resource_ref/META-INF/jboss.xml
            if (StringUtils.isBlank(jndiLocation) && StringUtils.isNotBlank(resourceName))
            {
                jndiLocation = resourceManagerReferences.get(resourceName);
            }

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceRefName))
            {
                JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);
                LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceRefName);
                // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
                for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceRefName))
                {
                    envRefService.associateEnvironmentToJndi(resource, ref);
                }
            }
        }

        // bind the MDBs to the JMS Destination.
        for (Element messageDrivenRef : $(doc).find("message-driven").get())
        {
            // register the EJB to the JNDI location, if it exists.
            String ejbName = $(messageDrivenRef).child("ejb-name").text();

            LOG.info("Found MDB: " + ejbName);
            if (StringUtils.isNotBlank(ejbName))
            {
                for (EjbMessageDrivenModel mdb : mdbService.findAllByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, ejbName))
                {
                    String destination = $(messageDrivenRef).child("destination-jndi-name").text();

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
