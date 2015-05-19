package org.jboss.windup.rules.apps.javaee.rules.jboss;

import static org.joox.JOOX.$;

import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.rules.DiscoverWebXmlRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.EnvironmentReferenceService;
import org.jboss.windup.rules.apps.javaee.service.JNDIResourceService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers JBoss Web XML files and parses the related metadata
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class ResolveJBossWebXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveJBossWebXmlRuleProvider.class.getSimpleName());

    public ResolveJBossWebXmlRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveJBossWebXmlRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(DiscoverWebXmlRuleProvider.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover JBoss Web XML Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "jboss-web");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        EnvironmentReferenceService envRefService = new EnvironmentReferenceService(event.getGraphContext());
        JNDIResourceService jndiResourceService = new JNDIResourceService(event.getGraphContext());
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        technologyTagService.addTagToFileModel(payload, "JBoss Web XML", TechnologyTagLevel.IMPORTANT);

        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        classificationService.attachClassification(payload, "JBoss Web XML", "JBoss Web XML Descriptor.");

        Document doc = xmlFileService.loadDocumentQuiet(payload);

        for (Element resourceRef : $(doc).find("resource-ref").get())
        {
            String jndiLocation = $(resourceRef).child("jndi-name").text();
            String resourceName = $(resourceRef).child("res-ref-name").text();

            JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);

            // now, look up the resource
            for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceName))
            {
                envRefService.associateEnvironmentToJndi(event, resource, ref);
            }

        }

    }

}
