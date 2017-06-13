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
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
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
 * Discovers WebSphere Web XML files and parses the related metadata
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 *
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverWebXmlRuleProvider.class, perform = "Discover IBM WebSphere Web Binding Files")
public class ResolveWebSphereWebXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveWebSphereWebXmlRuleProvider.class.getName());

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

        // Classification done by websphere-xml-06000 - see WINDUPRULE-164

        Document doc = xmlFileService.loadDocumentQuiet(event, context, payload);

        VendorSpecificationExtensionService vendorSpecificationService = new VendorSpecificationExtensionService(event.getGraphContext());
        // mark as vendor extension; create reference to web.xml
        vendorSpecificationService.associateAsVendorExtension(payload, "web.xml");

        TechnologyTagModel technologyTag = technologyTagService.addTagToFileModel(payload, "WebSphere Web XML", TechnologyTagLevel.IMPORTANT);
        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), payload.getProjectModel());
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
    }

    private void processBinding(EnvironmentReferenceService envRefService, JNDIResourceService jndiResourceService, Set<ProjectModel> applications,
                Element resourceRef, String tagName)
    {
        String jndiLocation = $(resourceRef).attr("jndiName");
        String resourceId = $(resourceRef).child(tagName).attr("href");
        resourceId = StringUtils.substringAfter(resourceId, "WEB-INF/web.xml#");

        if (StringUtils.isBlank(resourceId))
        {
            LOG.info("Issue Element: " + $(resourceRef).toString());
            return;
        }

        if (StringUtils.isNotBlank(jndiLocation))
        {
            JNDIResourceModel resource = jndiResourceService.createUnique(applications, jndiLocation);
            LOG.info("JNDI: " + jndiLocation + " Resource: " + resourceId);
            // now, look up the resource
            for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.REFERENCE_ID, resourceId))
            {
                LOG.info(" - Associating JNDI: " + jndiLocation + " Resource: " + ref);
                envRefService.associateEnvironmentToJndi(resource, ref);
            }
        }
    }

}
