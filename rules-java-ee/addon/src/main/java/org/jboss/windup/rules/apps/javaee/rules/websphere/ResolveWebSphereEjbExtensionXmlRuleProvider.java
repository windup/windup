package org.jboss.windup.rules.apps.javaee.rules.websphere;


import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.rules.DiscoverEjbConfigurationXmlRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.VendorSpecificationExtensionService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers WebSphere EJB Extension XML files and parses the related metadata
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverEjbConfigurationXmlRuleProvider.class, perform = "Discover WebSphere EJB XML Files")
public class ResolveWebSphereEjbExtensionXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "ibm-ejb-jar-ext.xmi")
                    .withProperty(XmlFileModel.ROOT_TAG_NAME, "EJBJarExtension");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        classificationService.attachClassification(event, context, payload, IssueCategoryRegistry.MANDATORY, "WebSphere EJB Ext", "WebSphere Enterprise Java Bean Extension XML Descriptor.");

        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(payload, "WebSphere EJB Ext", TechnologyTagLevel.IMPORTANT);

        VendorSpecificationExtensionService vendorSpecificationService = new VendorSpecificationExtensionService(event.getGraphContext());
        vendorSpecificationService.associateAsVendorExtension(payload, "ejb-jar.xml");
    }

}
