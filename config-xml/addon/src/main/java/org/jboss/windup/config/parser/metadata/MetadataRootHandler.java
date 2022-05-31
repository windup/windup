package org.jboss.windup.config.parser.metadata;

import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RulesetMetadata;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

import java.util.List;

import static org.joox.JOOX.$;

@NamespaceElementHandler(elementName = "metadata", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataRootHandler implements ElementHandler<RulesetMetadata> {

    public static final String DEPENDENCIES = "dependencies";
    public static final String SOURCE_TECHNOLOGY = "sourceTechnology";
    public static final String TARGET_TECHNOLOGY = "targetTechnology";
    public static final String TAG = "tag";
    public static final String EXECUTE_AFTER = "executeAfter";
    public static final String EXECUTE_BEFORE = "executeBefore";

    @Override
    @SuppressWarnings("unchecked")
    public RulesetMetadata processElement(ParserContext context, Element element) throws ConfigurationException {
        List<Element> children = $(element).children().get();
        MetadataBuilder metadataBuilder = context.getBuilder().getMetadataBuilder();
        for (Element child : children) {
            Object result = context.processElement(child);

            switch ($(child).tag()) {
                case MetadataProviderOverrideHandler.OVERRIDE_PROVIDER:
                    metadataBuilder.setOverrideProvider((Boolean) result);
                    break;

                case MetadataDescriptionHandler.DESCRIPTION:
                    metadataBuilder.setDescription((String) result);
                    break;

                case DEPENDENCIES:
                    for (AddonId id : (List<AddonId>) result) {
                        metadataBuilder.addRequiredAddon(id);
                    }
                    break;

                case SOURCE_TECHNOLOGY:
                    metadataBuilder.addSourceTechnology((TechnologyReference) result);
                    break;

                case TARGET_TECHNOLOGY:
                    metadataBuilder.addTargetTechnology((TechnologyReference) result);
                    break;

                case TAG:
                    metadataBuilder.addTag((String) result);
                    break;

                case EXECUTE_AFTER:
                    metadataBuilder.addExecuteAfterId((String) result);
                    break;

                case EXECUTE_BEFORE:
                    metadataBuilder.addExecuteBeforeId((String) result);
                    break;
            }
        }
        return metadataBuilder;
    }

}
