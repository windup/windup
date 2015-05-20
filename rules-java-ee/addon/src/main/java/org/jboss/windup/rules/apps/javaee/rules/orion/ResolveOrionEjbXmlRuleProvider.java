package org.jboss.windup.rules.apps.javaee.rules.orion;

import static org.joox.JOOX.$;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
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
 * Discovers Orion EJB XML files and parses the related metadata
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class ResolveOrionEjbXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveOrionEjbXmlRuleProvider.class.getSimpleName());

    public ResolveOrionEjbXmlRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveOrionEjbXmlRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(DiscoverEjbConfigurationXmlRuleProvider.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover Orion EJB XML Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "orion-ejb-jar");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        EnvironmentReferenceService envRefService = new EnvironmentReferenceService(event.getGraphContext());
        JNDIResourceService jndiResourceService = new JNDIResourceService(event.getGraphContext());
        JmsDestinationService jmsDestinationService = new JmsDestinationService(event.getGraphContext());
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        GraphService<EjbSessionBeanModel> ejbSessionBeanService = new GraphService<>(event.getGraphContext(), EjbSessionBeanModel.class);
        GraphService<EjbMessageDrivenModel> mdbService = new GraphService<>(event.getGraphContext(), EjbMessageDrivenModel.class);

        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());

        Document doc = xmlFileService.loadDocumentQuiet(payload);

        TechnologyTagModel technologyTag = technologyTagService.addTagToFileModel(payload, "Orion EJB XML", TechnologyTagLevel.IMPORTANT);
        for (Element resourceRef : $(doc).find("resource-ref-mapping").get())
        {
            String jndiLocation = $(resourceRef).attr("location");
            String resourceName = $(resourceRef).attr("name");

            if (StringUtils.isNotBlank(jndiLocation) && StringUtils.isNotBlank(resourceName))
            {
                JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);
                LOG.info("JNDI Name: " + jndiLocation + " to Resource: " + resourceName);
                // now, look up the resource which is resolved by DiscoverEjbConfigurationXmlRuleProvider
                for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceName))
                {
                    envRefService.associateEnvironmentToJndi(resource, ref);
                }
            }
        }

        for (Element ejbRef : $(doc).find("session-deployment").get())
        {
            String ejbName = $(ejbRef).attr("name");

            if (StringUtils.isNotBlank(ejbName))
            {
                LOG.info("Looking up name: " + ejbName);
                for (EjbSessionBeanModel ejb : ejbSessionBeanService.findAllByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, ejbName))
                {
                    String destination = $(ejbRef).attr("location");

                    if (StringUtils.isNotBlank(destination))
                    {
                        JNDIResourceModel jndiRef = jndiResourceService.createUnique(destination);
                        ejb.setJndiReference(jndiRef);
                    }
                }
            }
        }

        // bind the EJB beans to JNDI.
        for (Element messageDrivenRef : $(doc).find("message-driven-deployment").get())
        {
            // register the EJB to the JNDI location, if it exists.
            String ejbName = $(messageDrivenRef).attr("name");

            if (StringUtils.isNotBlank(ejbName))
            {
                LOG.info("Looking up name: " + ejbName);
                for (EjbMessageDrivenModel mdb : mdbService.findAllByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, ejbName))
                {
                    String destination = $(messageDrivenRef).attr("destination-location");

                    for (Element configProperty : $(messageDrivenRef).find("config-property").get())
                    {
                        String name = $(configProperty).child("config-property-name").text();
                        String value = $(configProperty).child("config-property-value").text();

                        if (StringUtils.isBlank(destination) && StringUtils.equals("DestinationName", name))
                        {
                            destination = value;
                        }
                    }

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
