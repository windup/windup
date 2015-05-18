package org.jboss.windup.rules.apps.javaee.rules.websphere;

import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.rules.DiscoverEjbConfigurationXmlRuleProvider;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers Websphere EJB Extension XML files and parses the related metadata
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class ResolveWebsphereEjbExtensionXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveWebsphereEjbExtensionXmlRuleProvider.class.getSimpleName());

    public ResolveWebsphereEjbExtensionXmlRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveWebsphereEjbExtensionXmlRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(DiscoverEjbConfigurationXmlRuleProvider.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover Websphere EJB XML Files";
    }

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
        classificationService.attachClassification(payload, "Websphere EJB Ext", "Websphere Enterprise Java Bean Extension XML Descriptor.");

        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(payload, "Websphere EJB Ext", TechnologyTagLevel.IMPORTANT);
    }

}
