package org.jboss.windup.rules.apps.javaee.rules;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceTagType;
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
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, perform = "Discover web.xml files")
public class DiscoverWebXmlRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(DiscoverWebXmlRuleProvider.class.getName());

    private static final String TECH_TAG = "Web XML";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    private static final String REGEX_DTD = "(?i).*web.application.*";

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "web-app");
    }

    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        Document doc = xmlFileService.loadDocumentQuiet(event, context, payload);
        if (doc != null && isWebXml(payload, doc))
        {
            addWebXmlMetadata(event, context, event.getGraphContext(), payload, doc);
        }
    }

    private boolean isWebXml(XmlFileModel xml, Document doc)
    {
        // check it's doctype against the known doctype.
        return !(xml.getDoctype() != null && !processDoctypeMatches(xml.getDoctype()));
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

    private void addWebXmlMetadata(GraphRewrite event, EvaluationContext evaluationContext, GraphContext context, XmlFileModel xml, Document doc)
    {
        ClassificationService classificationService = new ClassificationService(context);
        TechnologyTagService technologyTagService = new TechnologyTagService(context);

        classificationService.attachClassification(event, evaluationContext, xml, "Web XML", " Web Application Deployment Descriptors");
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
            if (Pattern.matches(REGEX_DTD, doctypeMetaModel.getPublicId()))
            {
                return true;
            }
        }

        if (StringUtils.isNotBlank(doctypeMetaModel.getSystemId()))
        {
            if (Pattern.matches(REGEX_DTD, doctypeMetaModel.getSystemId()))
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
        for (Element resourceRef : $(element).find("resource-ref").get())
        {
            processElement(environmentReferenceService, resources, resourceRef, "res-type", "res-ref-name",  EnvironmentReferenceTagType.RESOURCE_REF);
        }
        for (Element resourceRef : $(element).find("ejb-ref").get())
        {
            processElement(environmentReferenceService, resources, resourceRef, "ejb-ref-type", "ejb-ref-name", EnvironmentReferenceTagType.EJB_REF);
        }
        for (Element resourceRef : $(element).find("ejb-local-ref").get())
        {
            processElement(environmentReferenceService, resources, resourceRef, "ejb-ref-type", "ejb-ref-name", EnvironmentReferenceTagType.EJB_LOCAL_REF);
        }
        for (Element resourceRef : $(element).find("message-destination-ref").get())
        {
            processElement(environmentReferenceService, resources, resourceRef, "message-destination-type", "message-destination-ref-name", EnvironmentReferenceTagType.MSG_DESTINATION_REF);
        }
        return resources;
    }

    private void processElement(EnvironmentReferenceService environmentReferenceService, List<EnvironmentReferenceModel> resources, Element element, String typeLocation, String nameLocation, EnvironmentReferenceTagType refType)
    {
        String id = $(element).attr("id");
        String type = $(element).child(typeLocation).text();
        String name = $(element).child(nameLocation).text();

        type = StringUtils.trim(type);
        name = StringUtils.trim(name);

        EnvironmentReferenceModel ref = environmentReferenceService.findEnvironmentReference(name, refType);
        if (ref == null)
        {
            ref = environmentReferenceService.create();
            ref.setName(name);
            ref.setReferenceType(type);
            ref.setReferenceTagType(refType);

            LOG.info("Added: "+ref);
        }
        else {
            if(ref.getReferenceTagType() != null && (ref.getReferenceTagType() != refType)) {
                LOG.warning("Expected type: "+EnvironmentReferenceTagType.RESOURCE_REF +" but actually: "+ref.getReferenceType());
            }
        }

        ref.setReferenceId(id);
        resources.add(ref);
    }

}
