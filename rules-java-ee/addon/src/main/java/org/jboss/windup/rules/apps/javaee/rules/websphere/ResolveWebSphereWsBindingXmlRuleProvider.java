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
import org.jboss.windup.rules.apps.xml.DiscoverXmlFilesRuleProvider;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers WebSphere Web Service Binding XML files and parses the related metadata
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverXmlFilesRuleProvider.class, perform = "Discover WebSphere Web Service Binding XML Files")
public class ResolveWebSphereWsBindingXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "ibm-webservices-bnd.xmi")
                    .withProperty(XmlFileModel.ROOT_TAG_NAME, "WSBinding");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        ClassificationModel classificationModel = classificationService.attachClassification(event, context, payload, IssueCategoryRegistry.MANDATORY,
                    "WebSphere web service binding descriptor (ibm-webservices-bnd)",
                    "WebSphere Webservice Binding XML Deployment Descriptor.  \n"
                                + "This deployment descriptor extension is IBM specific and it needs to be migrated to JBossWS.  \n"
                                + "JBossWS implements the latest JAX-WS specification, which users can reference for any vendor-agnostic web service usage need.  \n"
                                + "You can migrate deployment descriptors following the links below.  \n");
        classificationModel.setEffort(3);
        GraphContext graphContext = event.getGraphContext();
        LinkService linkService = new LinkService(graphContext);
        LinkModel documentationEAP6Link = linkService.create();
        documentationEAP6Link.setDescription("JAX-WS Web Services (EAP 6)");
        documentationEAP6Link.setLink(
                    "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Development_Guide/chap-JAX-WS_Web_Services.html");
        classificationService.attachLink(classificationModel, documentationEAP6Link);
        LinkModel documentationEAP7Link = linkService.create();
        documentationEAP7Link.setDescription("Developing JAX-WS Web Services (EAP 7)");
        documentationEAP7Link.setLink(
                    "https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.0/html/developing_web_services_applications/developing_jax_ws_web_services");
        classificationService.attachLink(classificationModel, documentationEAP7Link);
        LinkModel documentationCommunityLink = linkService.create();
        documentationCommunityLink.setDescription("JBossWS configuration (community documentation)");
        documentationCommunityLink.setLink("https://docs.jboss.org/author/display/JBWS/Predefined+client+and+endpoint+configurations");
        classificationService.attachLink(classificationModel, documentationCommunityLink);

        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(payload, "WebSphere WS Binding", TechnologyTagLevel.IMPORTANT);

    }

}
