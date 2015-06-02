package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RulesetMetadata;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.metadata.MetadataDescriptionHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "metadata", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataRootHandler implements ElementHandler<RulesetMetadata>
{

    @Override
    @SuppressWarnings("unchecked")
    public RulesetMetadata processElement(ParserContext context, Element element) throws ConfigurationException
    {
        List<Element> children = $(element).children().get();
        MetadataBuilder metadataBuilder = context.getBuilder().getMetadataBuilder();
        for (Element child : children)
        {
            Object result = context.processElement(child);

            switch ($(child).tag())
            {
            case MetadataDescriptionHandler.DESCRIPTION:
                metadataBuilder.setDescription((String) result);
                break;

            case "dependencies":
                for (AddonId id : (List<AddonId>) result)
                {
                    metadataBuilder.addRequiredAddon(id);
                }
                break;

            case "sourceTechnology":
                metadataBuilder.addSourceTechnology((TechnologyReference) result);
                break;

            case "targetTechnology":
                metadataBuilder.addTargetTechnology((TechnologyReference) result);
                break;

            case "tag":
                metadataBuilder.addTag((String) result);
                break;

            case "executeAfter":
                metadataBuilder.addExecuteAfterId((String) result);
                break;

            case "executeBefore":
                metadataBuilder.addExecuteBeforeId((String) result);
                break;
            }
        }
        return metadataBuilder;
    }

}
