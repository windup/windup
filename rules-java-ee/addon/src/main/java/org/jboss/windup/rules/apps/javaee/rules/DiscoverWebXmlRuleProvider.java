package org.jboss.windup.rules.apps.javaee.rules;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.IteratingRuleProvider;
import org.jboss.windup.config.phase.InitialAnalysis;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;
import org.jboss.windup.rules.apps.javaee.service.EnvironmentReferenceService;
import org.jboss.windup.rules.apps.javaee.service.WebXmlService;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.xml.DoctypeUtils;
import org.jboss.windup.util.xml.NamespaceUtils;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers web.xml files, parses them, and places relevant metadata into the graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class DiscoverWebXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final String TECH_TAG = "Web XML";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.IMPORTANT;

    private static final String dtdRegex = "(?i).*web.application.*";

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return InitialAnalysis.class;
    }

    @Override
    public String toStringPerform()
    {
        return "Discover web.xml files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "web-app");
    }

    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        Document doc = xmlFileService.loadDocumentQuiet(payload);
        if (doc != null && isWebXml(payload, doc))
        {
            addWebXmlMetadata(event.getGraphContext(), payload, doc);
        }
    }

    private boolean isWebXml(XmlFileModel xml, Document doc)
    {
        // check it's doctype against the known doctype.
        if (xml.getDoctype() != null && !processDoctypeMatches(xml.getDoctype()))
        {
            return false;
        }
        return true;
    }

    private String getVersion(XmlFileModel xml, Document doc)
    {
        String version = null;

        // check it's doctype against the known doctype.
        if (xml.getDoctype() != null)
        {
            // if it isn't matching doctype, then continue.
            if (processDoctypeMatches(xml.getDoctype()))
            {
                version = processDoctypeVersion(xml.getDoctype());
            }
        }
        else
        {
            // if there is no doctype, check the XSD..
            version = $(doc).attr("version");

            // if the version attribute isn't found, then grab it from the XSD name if we can.
            if (StringUtils.isBlank(version))
            {
                // get the first tag's namespace...
                String namespace = $(doc).find("web-app").namespaceURI();
                if (StringUtils.isBlank(namespace))
                {
                    namespace = doc.getFirstChild().getNamespaceURI();
                }
                // find that namespace, and try and pull the version from the XSD name...
                for (NamespaceMetaModel ns : xml.getNamespaces())
                {
                    if (StringUtils.equals(ns.getURI(), namespace))
                    {
                        version = NamespaceUtils.extractVersion(ns.getSchemaLocation());
                        break;
                    }
                }
            }
        }

        return version;
    }

    private void addWebXmlMetadata(GraphContext context, XmlFileModel xml, Document doc)
    {
        TechnologyTagService technologyTagService = new TechnologyTagService(context);
        TechnologyTagModel technologyTag = technologyTagService.addTagToFileModel(xml, TECH_TAG, TECH_TAG_LEVEL);
        WebXmlService webXmlService = new WebXmlService(context);

        String webXmlVersion = getVersion(xml, doc);

        // check the root XML node.
        WebXmlModel webXml = webXmlService.addTypeToModel(xml);

        // change "_" in the version to "."
        if (StringUtils.isNotBlank(webXmlVersion))
        {
            webXmlVersion = StringUtils.replace(webXmlVersion, "_", ".");
            webXml.setSpecificationVersion(webXmlVersion);

            // set the tag version
            technologyTag.setVersion(webXmlVersion);
        }

        String displayName = $(doc).child("display-name").text();
        displayName = StringUtils.trimToNull(displayName);
        if (StringUtils.isNotBlank(displayName))
        {
            webXml.setDisplayName(displayName);
        }

        // extract references.
        List<EnvironmentReferenceModel> refs = processEnvironmentReference(context, doc.getDocumentElement());
        for (EnvironmentReferenceModel ref : refs)
        {
            webXml.addEnvironmentReference(ref);
        }
    }

    private boolean processDoctypeMatches(DoctypeMetaModel doctypeMetaModel)
    {
        if (StringUtils.isNotBlank(doctypeMetaModel.getPublicId()))
        {
            if (Pattern.matches(dtdRegex, doctypeMetaModel.getPublicId()))
            {
                return true;
            }
        }

        if (StringUtils.isNotBlank(doctypeMetaModel.getSystemId()))
        {
            if (Pattern.matches(dtdRegex, doctypeMetaModel.getSystemId()))
            {
                return true;
            }

        }
        return false;
    }

    private String processDoctypeVersion(DoctypeMetaModel entry)
    {
        String publicId = entry.getPublicId();
        String systemId = entry.getSystemId();

        // extract the version information from the public / system ID.
        String versionInformation = DoctypeUtils.extractVersion(publicId, systemId);
        return versionInformation;
    }

    private List<EnvironmentReferenceModel> processEnvironmentReference(GraphContext context, Element element)
    {
        EnvironmentReferenceService environmentReferenceService = new EnvironmentReferenceService(context);
        List<EnvironmentReferenceModel> resources = new ArrayList<>();

        // find JMS references...
        List<Element> queueReferences = $(element).find("resource-ref").get();
        for (Element e : queueReferences)
        {
            String id = $(e).attr("id");
            String type = $(e).child("res-type").text();
            String name = $(e).child("res-ref-name").text();

            type = StringUtils.trim(type);
            name = StringUtils.trim(name);

            EnvironmentReferenceModel ref = environmentReferenceService.findEnvironmentReference(name, type);
            if (ref == null)
            {
                ref = environmentReferenceService.create();
                ref.setName(name);
                ref.setReferenceType(type);
            }
            ref.setReferenceId(id);
            resources.add(ref);
        }

        return resources;
    }
}
