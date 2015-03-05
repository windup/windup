package org.jboss.windup.config.parser.metadata;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.furnace.versions.VersionException;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.WindupXMLRulesetParsingException;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = MetadataTargetTechnologyHandler.METADATA_TARGET_TECHNOLOGY_ELEMENT, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataTargetTechnologyHandler implements ElementHandler<TechnologyReference>
{

    private static String ID = "id";
    private static String VERSION_RANGE = "versionRange";
    public static final String METADATA_TARGET_TECHNOLOGY_ELEMENT = "targetTechnology";

    @Override
    public TechnologyReference processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String id = element.getAttribute(ID);
        String versionRange = element.getAttribute(VERSION_RANGE);
        if (StringUtils.isBlank(id))
        {
            throw new WindupXMLRulesetParsingException("The '" + METADATA_TARGET_TECHNOLOGY_ELEMENT + "' element must have a non-empty '" + ID + "' attribute");
        }
        if (StringUtils.isBlank(versionRange))
        {
            throw new WindupXMLRulesetParsingException("The '" + METADATA_TARGET_TECHNOLOGY_ELEMENT + "' element must have a non-empty '" + VERSION_RANGE
                        + "' attribute");
        }
        try{
            Versions.parseVersionRange(versionRange);
        } catch(VersionException ex) {
            throw new WindupXMLRulesetParsingException("The '" + VERSION_RANGE + "' attribute with value \"" + versionRange+ "\" in the element " +METADATA_TARGET_TECHNOLOGY_ELEMENT + " is not a valid version",ex);
        }
        
        TechnologyReference reference = new TechnologyReference(id, versionRange);
        return reference;
    }

}
