package org.jboss.windup.rules.apps.javaee.rules.jboss;

import static org.joox.JOOX.$;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
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
 * Discovers JBoss EJB XML files and parses the related metadata Handles XML files prior to EAP 6. (jboss.xml)
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverEjbConfigurationXmlRuleProvider.class, perform = "Discover JBoss EJB XML Files")
public class ResolveJBossLegacyEjbXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveJBossLegacyEjbXmlRuleProvider.class.getName());

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "jboss").withProperty(FileModel.FILE_NAME, "jboss.xml");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        Document doc = xmlFileService.loadDocumentQuiet(event, context, payload);

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

        //ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        //classificationService.attachClassification(event, context, payload, "JBoss Legacy EJB XML",
        //            "JBoss Enterprise Java Bean XML Descriptor prior to EAP 6.");

        VendorSpecificationExtensionService vendorSpecificationService = new VendorSpecificationExtensionService(event.getGraphContext());
        //mark as vendor extension; create reference to ejb-jar.xml
        vendorSpecificationService.associateAsVendorExtension(payload, "ejb-jar.xml");

        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(payload, "JBoss EJB XML", TechnologyTagLevel.IMPORTANT);

        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), payload.getProjectModel());

        // first, find all resource managers for later resolution.
        Map<String, String> resourceManagerReferences = new HashMap<>();
        for (Element resourceRef : $(doc).find("resource-managers").children("resource-manager").get())
        {
            String resourceName = $(resourceRef).child("res-name").text();
            String resourceJNDI = $(resourceRef).child("res-jndi-name").text();
            resourceManagerReferences.put(resourceName, resourceJNDI);
            LOG.info("Found Resource Manager: " + resourceName + ", " + resourceJNDI);
        }


        // register beans to JNDI: http://grepcode.com/file/repository.jboss.org/nexus/content/repositories/releases/org.jboss.ejb3/jboss-ejb3-core/0.1.0/test/naming/META-INF/jboss1.xml?av=f
        for (Element resourceRef : $(doc).find("resource-ref").get())
        {
            processBinding(envRefService, jndiResourceService, applications, resourceManagerReferences, resourceRef, "res-ref-name",
                        "jndi-name");
        }
        for (Element resourceRef : $(doc).find("resource-env-ref").get())
        {
            processBinding(envRefService, jndiResourceService, applications, resourceManagerReferences, resourceRef,
                        "resource-env-ref-name", "jndi-name");
        }
        for (Element resourceRef : $(doc).find("message-destination-ref").get())
        {
            processBinding(envRefService, jndiResourceService, applications, resourceManagerReferences, resourceRef,
                        "message-destination-ref-name", "jndi-name");
        }
        for (Element resourceRef : $(doc).find("ejb-ref").get())
        {
            processBinding(envRefService, jndiResourceService, applications, resourceManagerReferences, resourceRef, "ejb-ref-name",
                        "jndi-name");
        }
        for (Element resourceRef : $(doc).find("ejb-local-ref").get())
        {
            processBinding(envRefService, jndiResourceService, applications, resourceManagerReferences, resourceRef, "ejb-ref-name",
                        "local-jndi-name");
        }


        for (Element ejbRef : $(doc).find("session").get())
        {
            String ejbName = $(ejbRef).child("ejb-name").content();
            String sessionClustered = $(ejbRef).child("clustered").content();
            sessionClustered = StringUtils.trim(sessionClustered);

            //transaction timeout
            Map<String, Integer> txTimeouts = parseTxTimeout(ejbRef, ejbName);

            if (StringUtils.isNotBlank(ejbName))
            {
                LOG.info("Looking up name: " + ejbName);
                for (EjbSessionBeanModel ejb : ejbSessionBeanService.findAllByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, ejbName))
                {
                    String jndi = $(ejbRef).child("jndi-name").content();
                    String localJNDI = $(ejbRef).child("local-jndi-name").content();
                    if (StringUtils.isNotBlank(jndi))
                    {
                        JNDIResourceModel jndiRef = jndiResourceService.createUnique(applications, jndi);
                        ejb.setGlobalJndiReference(jndiRef);
                    }

                    if (StringUtils.isNotBlank(localJNDI))
                    {
                        JNDIResourceModel jndiRef = jndiResourceService.createUnique(applications, localJNDI);
                        ejb.setLocalJndiReference(jndiRef);
                    }

                    if(StringUtils.equalsIgnoreCase("true", sessionClustered)) {
                        ejb.setClustered(true);
                    }
                    ejb.setTxTimeouts(txTimeouts);
                }
            }
        }

        // bind the MDBs to the JMS Destination.
        for (Element messageDrivenRef : $(doc).find("message-driven").get())
        {
            // register the EJB to the JNDI location, if it exists.
            String ejbName = $(messageDrivenRef).child("ejb-name").text();

            Map<String, Integer> txTimeouts = parseTxTimeout(messageDrivenRef, ejbName);

            LOG.info("Found MDB: " + ejbName);
            if (StringUtils.isNotBlank(ejbName))
            {
                for (EjbMessageDrivenModel mdb : mdbService.findAllByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, ejbName))
                {
                    String destination = $(messageDrivenRef).child("destination-jndi-name").text();

                    if (StringUtils.isNotBlank(destination))
                    {
                        JmsDestinationModel jndiRef = jmsDestinationService.createUnique(applications, destination);
                        mdb.setDestination(jndiRef);
                    }
                    mdb.setTxTimeouts(txTimeouts);
                }
            }
        }
    }

    private Map<String, Integer> parseTxTimeout(Element elementRef, String ejbName)
    {
        Map<String, Integer> transactionTimeouts = new HashMap<>();
        for (Element methodRef : $(elementRef).child("method-attributes").find("method").get())
        {
            String methodName = $(methodRef).child("method-name").content();
            String transactionTimeout = $(methodRef).child("transaction-timeout").content();
            if(StringUtils.isNotBlank(transactionTimeout)) {
                try {
                    Integer txTimeout = Integer.parseInt(transactionTimeout);
                    transactionTimeouts.put(methodName, txTimeout);
                }
                catch(Exception e) {
                    LOG.info("EJB: "+ejbName+" contains bad reference to TX Timeout on Method: "+methodName);
                }
            }
        }

        return transactionTimeouts;
    }


    private void processBinding(EnvironmentReferenceService envRefService, JNDIResourceService jndiResourceService, Set<ProjectModel> applications,
                Map<String, String> resourceManagerReferences, Element resourceRef, String tagName, String tagJndi)
    {
        String jndiLocation = $(resourceRef).child(tagJndi).text();
        String resourceRefName = $(resourceRef).child(tagName).text();
        String resourceName = $(resourceRef).child("resource-name").text();

        LOG.info("Processing binding: "+$(resourceRef).toString());
        LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceRefName);

        // resolve jndilocation from resourcename...: https://docs.jboss.org/ejb3/app-server/tutorial/jboss_resource_ref/META-INF/jboss.xml
        if (StringUtils.isBlank(jndiLocation) && StringUtils.isNotBlank(resourceName))
        {
            jndiLocation = resourceManagerReferences.get(resourceName);
        }


        if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceRefName))
        {
            JNDIResourceModel resource = jndiResourceService.createUnique(applications, jndiLocation);
            LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceRefName);
            // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
            for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceRefName))
            {
                envRefService.associateEnvironmentToJndi(resource, ref);
            }
        }
    }

}
