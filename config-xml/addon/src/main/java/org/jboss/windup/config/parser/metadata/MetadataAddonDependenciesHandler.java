package org.jboss.windup.config.parser.metadata;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "dependencies", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataAddonDependenciesHandler implements ElementHandler<List<AddonId>> {

    @Override
    public List<AddonId> processElement(ParserContext context, Element element) throws ConfigurationException {
        List<Element> children = $(element).children().get();
        List<AddonId> addonIds = new ArrayList<>();
        for (Element child : children) {
            Object result = context.processElement(child);
            switch ($(child).tag()) {
                case "addon":
                    addonIds.add((AddonId) result);
                    break;
            }

        }
        return addonIds;
    }

}
