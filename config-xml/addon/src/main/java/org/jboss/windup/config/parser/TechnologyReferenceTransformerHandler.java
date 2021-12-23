package org.jboss.windup.config.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.metadata.TechnologyReferenceAliasTranslator;
import org.jboss.windup.config.parser.metadata.MetadataSourceTechnologyHandler;
import org.jboss.windup.config.parser.metadata.MetadataTargetTechnologyHandler;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * This produces a list of {@link TechnologyReferenceAliasTranslator} objects.
 *
 * The format of the file is:
 *
 * <pre>
 * &lt;technology-reference-transfomers&gt;
 *  &lt;transform&gt;
 *      &lt;sourceTechnology id="..." versionRange="..."/&gt;
 *      &lt;targetTechnology id="..." versionRange="..."/&gt;
 *  &lt;/transform&gt;
 * &lt;/technology-reference-transfomers&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = "technology-reference-transfomers", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class TechnologyReferenceTransformerHandler implements ElementHandler<List<TechnologyReferenceAliasTranslator>>
{

    public static final String TRANSFORM = "transform";

    @Override
    public List<TechnologyReferenceAliasTranslator> processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        List<TechnologyReferenceAliasTranslator> transformers = new ArrayList<>();

        List<Element> children = $(element).children().get();
        for (Element child : children)
        {
            if (StringUtils.equals(TRANSFORM, child.getTagName()))
            {
                Element sourceElement = (Element)child.getElementsByTagName(MetadataSourceTechnologyHandler.METADATA_SOURCE_TECHNOLOGY_ELEMENT).item(0);
                TechnologyReference source = handlerManager.processElement(sourceElement);

                Element targetElement = (Element)child.getElementsByTagName(MetadataTargetTechnologyHandler.METADATA_TARGET_TECHNOLOGY_ELEMENT).item(0);
                TechnologyReference target = handlerManager.processElement(targetElement);

                TechnologyReferenceAliasTranslator transformer = new TechnologyReferenceAliasTranslator(source, target);
                transformers.add(transformer);
            }
        }

        return transformers;
    }
}
