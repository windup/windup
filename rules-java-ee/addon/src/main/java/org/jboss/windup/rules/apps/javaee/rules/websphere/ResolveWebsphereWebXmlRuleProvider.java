package org.jboss.windup.rules.apps.javaee.rules.websphere;

import static org.joox.JOOX.$;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.rules.DiscoverWebXmlRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.EnvironmentReferenceService;
import org.jboss.windup.rules.apps.javaee.service.JNDIResourceService;
import org.jboss.windup.rules.apps.javaee.service.VendorSpecificationExtensionService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers Websphere Web XML files and parses the related metadata
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class ResolveWebsphereWebXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveWebsphereWebXmlRuleProvider.class.getSimpleName());

    public ResolveWebsphereWebXmlRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveWebsphereWebXmlRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(DiscoverWebXmlRuleProvider.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover IBM Websphere Web Binding Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.FILE_NAME, "ibm-web-bnd.xmi");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        EnvironmentReferenceService envRefService = new EnvironmentReferenceService(event.getGraphContext());
        JNDIResourceService jndiResourceService = new JNDIResourceService(event.getGraphContext());
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());

        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        ClassificationModel classification = classificationService.attachClassification(context, payload, "Websphere Web Binding", "Websphere Web Binding XML Descriptor");
        classification.setEffort(1);
        
        Document doc = xmlFileService.loadDocumentQuiet(context, payload);

        VendorSpecificationExtensionService vendorSpecificationService = new VendorSpecificationExtensionService(event.getGraphContext());
        //mark as vendor extension; create reference to web.xml
        vendorSpecificationService.associateAsVendorExtension(payload, "web.xml");
        
        TechnologyTagModel technologyTag = technologyTagService.addTagToFileModel(payload, "Websphere Web XML", TechnologyTagLevel.IMPORTANT);
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
    }

    private void processBinding(EnvironmentReferenceService envRefService, JNDIResourceService jndiResourceService, Element resourceRef, String tagName)
    {
        String jndiLocation = $(resourceRef).attr("jndiName");
        String resourceId = $(resourceRef).child(tagName).attr("href");
        resourceId = StringUtils.substringAfter(resourceId, "WEB-INF/web.xml#");
        
        if(StringUtils.isBlank(resourceId)) {
            LOG.info("Issue Element: "+$(resourceRef).toString());
            return;
        }

        if (StringUtils.isNotBlank(jndiLocation))
        {
            JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);
            LOG.info("JNDI: " + jndiLocation + " Resource: " + resourceId);
            // now, look up the resource
            for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.REFERENCE_ID, resourceId))
            {
                envRefService.associateEnvironmentToJndi(resource, ref);
            }
        }
    }

}
