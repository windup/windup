package org.jboss.windup.config.parser.xml;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.xml.XmlUtil;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.joox.JOOX.$;

/**
 * Handles parsing the "label" element
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
@NamespaceElementHandler(elementName = "label", namespace = LabelProviderHandler.WINDUP_LABEL_NAMESPACE)
public class LabelHandler implements ElementHandler<Label>
{
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String SUPPORTED = "supported";
    public static final String UNSUITABLE = "unsuitable";
    public static final String NEUTRAL = "neutral";

    @Override
    public Label processElement(ParserContext context, Element element)
    {
        String id = $(element).attr("id");
        String name = null;
        String description = null;
        Set<String> supported = new HashSet<>();
        Set<String> unsuitable = new HashSet<>();
        Set<String> neutral = new HashSet<>();

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            if (StringUtils.equals(NAME, child.getTagName())) {
                name = $(child).text();
            } else if (StringUtils.equals(DESCRIPTION, child.getTagName())) {
                description = $(child).text();
            } else if (StringUtils.equals(SUPPORTED, child.getTagName())) {
                $(child).children().get().forEach(tagElements -> {
                    supported.add(context.processElement(tagElements));
                });
            } else if (StringUtils.equals(UNSUITABLE, child.getTagName())) {
                $(child).children().get().forEach(tagElements -> {
                    unsuitable.add(context.processElement(tagElements));
                });
            } else if (StringUtils.equals(NEUTRAL, child.getTagName())) {
                $(child).children().get().forEach(tagElements -> {
                    neutral.add(context.processElement(tagElements));
                });
            }
        }

        Label label = new Label(id, name, description, XmlUtil.nodeToString(element));
        label.addSupportedTags(supported);
        label.addUnsuitableTags(unsuitable);
        label.addNeutralTags(neutral);

        return label;
    }

}
