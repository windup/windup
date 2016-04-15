package org.jboss.windup.rules.apps.javaee.rules.websphere;


import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.resource.FileModel;
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
 *
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverXmlFilesRuleProvider.class)
public class ResolveWebSphereWsBindingXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    @Override
    public String toStringPerform()
    {
        return "Discover WebSphere Web Service Binding XML Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "ibm-webservicesclient-bnd.xmi")
                    .withProperty(XmlFileModel.ROOT_TAG_NAME, "ClientBinding");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        classificationService.attachClassification(context, payload, "WebSphere WS Binding", "WebSphere Webservice Binding XML Descriptor.");

        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(payload, "WebSphere WS Binding", TechnologyTagLevel.IMPORTANT);

    }

}
