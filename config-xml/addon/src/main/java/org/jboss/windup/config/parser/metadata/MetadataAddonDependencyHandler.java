package org.jboss.windup.config.parser.metadata;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.WindupXMLRulesetParsingException;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = MetadataAddonDependencyHandler.ADDON_DEPENDENCY_ELEMENT, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataAddonDependencyHandler implements ElementHandler<AddonId> {
    private static final String ID = "id";
    public static final String ADDON_DEPENDENCY_ELEMENT = "addon";

    @Override
    public AddonId processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String id = element.getAttribute(ID);
        if (StringUtils.isBlank(id)) {
            throw new WindupXMLRulesetParsingException("The '" + ADDON_DEPENDENCY_ELEMENT + "' element must have a non-empty '" + ID + "' attribute");
        }
        AddonId addonId = AddonId.fromCoordinates(id);
        return addonId;
    }

}
