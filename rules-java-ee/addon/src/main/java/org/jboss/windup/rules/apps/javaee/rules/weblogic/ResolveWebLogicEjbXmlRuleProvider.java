package org.jboss.windup.rules.apps.javaee.rules.weblogic;

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
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.ThreadPoolModel;
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
 * Discovers WebLogic EJB XML files and parses the related metadata
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverEjbConfigurationXmlRuleProvider.class, perform = "Discover WebLogic EJB XML Files")
public class ResolveWebLogicEjbXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveWebLogicEjbXmlRuleProvider.class.getName());

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "weblogic-ejb-jar");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel weblogicEjbXml)
    {
        EnvironmentReferenceService envRefService = new EnvironmentReferenceService(event.getGraphContext());
        JNDIResourceService jndiResourceService = new JNDIResourceService(event.getGraphContext());
        JmsDestinationService jmsDestinationService = new JmsDestinationService(event.getGraphContext());
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        VendorSpecificationExtensionService vendorSpecificationService = new VendorSpecificationExtensionService(event.getGraphContext());

        GraphService<ThreadPoolModel> threadPoolService = new GraphService<>(event.getGraphContext(), ThreadPoolModel.class);
        GraphService<EjbSessionBeanModel> ejbSessionBeanService = new GraphService<>(event.getGraphContext(), EjbSessionBeanModel.class);
        GraphService<EjbMessageDrivenModel> mdbService = new GraphService<>(event.getGraphContext(), EjbMessageDrivenModel.class);

        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        //ClassificationModel classif = classificationService.attachClassification(event, context, weblogicEjbXml, "WebLogic EJB XML", "WebLogic Enterprise Java Bean XML Descriptor.");
        
        // TODO -- this classification duplicates a hint/clsf in the
        // weblogic-xml-descriptor-04000 XML rule. This should probably get a
        // better fix, but for now the important thing is to avoid duplicating
        // the effort added by that hint.
        // TODO: additionally from feedback we turn off the classification completely
        //classif.setEffort(0);
        //IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(event.getRewriteContext());
        //classif.setIssueCategory(issueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.MANDATORY));

        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(weblogicEjbXml, "WebLogic EJB XML", TechnologyTagLevel.IMPORTANT);

        Document doc = xmlFileService.loadDocumentQuiet(event, context, weblogicEjbXml);

        // mark as vendor extension; create reference to ejb-jar.xml
        vendorSpecificationService.associateAsVendorExtension(weblogicEjbXml, "ejb-jar.xml");

        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), weblogicEjbXml.getProjectModel());
        for (Element resourceRef : $(doc).find("resource-description").get())
        {
            String jndiLocation = $(resourceRef).child("jndi-name").text();
            String resourceName = $(resourceRef).child("res-ref-name").text();

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceName))
            {
                JNDIResourceModel resource = jndiResourceService.createUnique(applications, jndiLocation);
                LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceName);
                // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
                for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceName))
                {
                    envRefService.associateEnvironmentToJndi(resource, ref);
                }
            }
        }

        // register beans to JNDI
        for (Element resourceRef : $(doc).find("ejb-local-reference-description").get())
        {
            String resourceName = $(resourceRef).child("ejb-ref-name").text();
            String jndiLocation = $(resourceRef).child("jndi-name").text();

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceName))
            {
                JNDIResourceModel resource = jndiResourceService.createUnique(applications, jndiLocation);
                LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceName);
                // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
                for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceName))
                {
                    envRefService.associateEnvironmentToJndi(resource, ref);
                }

                for (EjbSessionBeanModel ejb : ejbSessionBeanService.findAllByProperty(EjbSessionBeanModel.EJB_BEAN_NAME, resourceName))
                {
                    ejb.setGlobalJndiReference(resource);
                }
            }
        }

        // bind the EJB beans to JNDI.
        for (Element enterpriseBeanTag : $(doc).find("weblogic-enterprise-bean").get())
        {

            // register the EJB to the JNDI location, if it exists.
            String localJndiLocation = $(enterpriseBeanTag).child("local-jndi-name").text();
            String jndiLocation = $(enterpriseBeanTag).child("jndi-name").text();
            String ejbName = $(enterpriseBeanTag).child("ejb-name").text();

            // resolve cluster values
            String sessionClustered = $(enterpriseBeanTag).find("stateless-bean-is-clusterable").text();
            sessionClustered = StringUtils.trim(sessionClustered);
            if (StringUtils.isBlank(sessionClustered))
            {
                // not stateless or not set.
                sessionClustered = $(enterpriseBeanTag).find("home-is-clusterable").text();
                sessionClustered = StringUtils.trim(sessionClustered);
            }

            // parse thread pool information
            ThreadPoolModel threadPoolModel = null;
            for (Element poolDescriptor : $(enterpriseBeanTag).find("pool").get())
            {
                String maxSize = $(poolDescriptor).child("max-beans-in-free-pool").text();
                String minSize = $(poolDescriptor).child("initial-beans-in-free-pool").text();
                threadPoolModel = threadPoolService.create();
                threadPoolModel.setApplications(ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), weblogicEjbXml.getProjectModel()));
                threadPoolModel.setPoolName(ejbName + "-ThreadPool");

                if (StringUtils.isNotBlank(maxSize))
                {
                    try
                    {
                        threadPoolModel.setMaxPoolSize(Integer.parseInt(maxSize));
                    }
                    catch (Exception e)
                    {
                        LOG.warning("Unable to parse max pool size: " + maxSize);
                    }
                }

                if (StringUtils.isNotBlank(minSize))
                {
                    try
                    {
                        threadPoolModel.setMinPoolSize(Integer.parseInt(minSize));
                    }
                    catch (Exception e)
                    {
                        LOG.warning("Unable to parse min pool size: " + minSize);
                    }
                }
                break;
            }
            // set thread pool
            if (threadPoolModel != null)
            {
                for (EjbSessionBeanModel sessionBean : ejbSessionBeanService.findAllByProperty(EjbSessionBeanModel.EJB_BEAN_NAME, ejbName))
                {
                    sessionBean.setThreadPool(threadPoolModel);
                }
                for (EjbMessageDrivenModel mdb : mdbService.findAllByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, ejbName))
                {
                    mdb.setThreadPool(threadPoolModel);
                }
            }

            Map<String, Integer> txTimeouts = parseTxTimeout(enterpriseBeanTag, ejbName);

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(ejbName))
            {
                JNDIResourceModel jndiRef = jndiResourceService.createUnique(applications, jndiLocation);
                // look up the EJB by the name, and associate to JNDI.
                for (EjbSessionBeanModel sessionBean : ejbSessionBeanService.findAllByProperty(EjbSessionBeanModel.EJB_BEAN_NAME, ejbName))
                {
                    LOG.info("Registering EJB: " + ejbName + " to JNDI: " + jndiLocation);
                    sessionBean.setGlobalJndiReference(jndiRef);
                }
            }

            if (StringUtils.isNotBlank(localJndiLocation) && StringUtils.isNotBlank(ejbName))
            {
                // look up the EJB by the name, and associate to JNDI.
                JNDIResourceModel localJndiRef = jndiResourceService.createUnique(applications, localJndiLocation);

                for (EjbSessionBeanModel sessionBean : ejbSessionBeanService.findAllByProperty(EjbSessionBeanModel.EJB_BEAN_NAME, ejbName))
                {
                    LOG.info("Registering EJB: " + ejbName + " to JNDI: " + jndiLocation);
                    sessionBean.setLocalJndiReference(localJndiRef);
                }
            }

            if (txTimeouts.size() > 0 && StringUtils.isNotBlank(ejbName))
            {
                for (EjbSessionBeanModel sessionBean : ejbSessionBeanService.findAllByProperty(EjbSessionBeanModel.EJB_BEAN_NAME, ejbName))
                {
                    sessionBean.setTxTimeouts(txTimeouts);
                }
            }

            // extract the JNDI location of any message driven beans.
            for (Element messageDrivenDescriptor : $(enterpriseBeanTag).find("message-driven-descriptor").get())
            {
                for (EjbMessageDrivenModel mdb : mdbService.findAllByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, ejbName))
                {
                    String destination = $(messageDrivenDescriptor).child("destination-jndi-name").text();
                    if (StringUtils.isNotBlank(destination))
                    {
                        JmsDestinationModel jndiRef = jmsDestinationService.createUnique(applications, destination);
                        mdb.setDestination(jndiRef);
                    }

                    if (txTimeouts.size() > 0)
                    {
                        mdb.setTxTimeouts(txTimeouts);
                    }
                }
            }

            // sets the clustered value to the session bean.
            if (StringUtils.equalsIgnoreCase("true", sessionClustered))
            {
                for (EjbSessionBeanModel sessionBean : ejbSessionBeanService.findAllByProperty(EjbSessionBeanModel.EJB_BEAN_NAME, ejbName))
                {
                    LOG.info("Setting bean as clustered: " + ejbName);
                    sessionBean.setClustered(true);
                }
            }
        }
    }

    private Map<String, Integer> parseTxTimeout(Element enterpriseBeanTag, String ejbName)
    {
        Map<String, Integer> transactionTimeouts = new HashMap<>();
        String transactionTimeoutSeconds = $(enterpriseBeanTag).child("transaction-descriptor").child("trans-timeout-seconds").text();
        String methodName = "*";
        if (StringUtils.isNotBlank(transactionTimeoutSeconds))
        {
            try
            {
                Integer txTimeout = Integer.parseInt(transactionTimeoutSeconds);
                transactionTimeouts.put(methodName, txTimeout);
            }
            catch (Exception e)
            {
                LOG.info("EJB: " + ejbName + " contains bad reference to TX Timeout on Method: " + methodName);
            }
        }

        return transactionTimeouts;
    }
}
