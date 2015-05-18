package org.jboss.windup.rules.apps.javaee.rules.orion;

import static org.joox.JOOX.$;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
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
 * Discovers Orion Web XML files and parses the related metadata
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class ResolveOrionWebXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveOrionWebXmlRuleProvider.class.getSimpleName());

    public ResolveOrionWebXmlRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveOrionWebXmlRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(DiscoverWebXmlRuleProvider.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover Orion Web XML Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "orion-web-app");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        EnvironmentReferenceService envRefService = new EnvironmentReferenceService(event.getGraphContext());
        JNDIResourceService jndiResourceService = new JNDIResourceService(event.getGraphContext());
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());

        Document doc = xmlFileService.loadDocumentQuiet(payload);

        TechnologyTagModel technologyTag = technologyTagService.addTagToFileModel(payload, "Orion Web XML", TechnologyTagLevel.IMPORTANT);
        for (Element orionWeb : $(doc).child("orion-web-app"))
        {
            String majorVersion = $(orionWeb).attr("schema-major-version");
            String minorVersion = $(orionWeb).attr("schema-minor-version");

            if (StringUtils.isNotBlank(majorVersion))
            {
                String version = majorVersion;
                if (StringUtils.isNotBlank(minorVersion))
                {
                    version = version + "." + minorVersion;
                }
                technologyTag.setVersion(version);
            }
        }

        for (Element resourceRef : $(doc).find("resource-ref-mapping").get())
        {
            String jndiLocation = $(resourceRef).attr("location");
            String resourceName = $(resourceRef).attr("name");

            JNDIResourceModel resource = jndiResourceService.createUnique(jndiLocation);

            // now, look up the resource
            for (EnvironmentReferenceModel ref : envRefService.findAllByProperty(EnvironmentReferenceModel.NAME, resourceName))
            {
                envRefService.associateEnvironmentToJndi(resource, ref);
            }

        }

    }

}
