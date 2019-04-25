package org.jboss.windup.config.parser.xml;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Set;

import static org.joox.JOOX.$;

@NamespaceElementHandler(elementName = "labelset", namespace = LabelProviderHandler.WINDUP_LABEL_NAMESPACE)
public class LabelProviderHandler implements ElementHandler<Set<Label>> {

    public static final String WINDUP_LABEL_NAMESPACE = "http://windup.jboss.org/schema/jboss-labelset";

    public static final String TRANSFORM = "labels";

    @Override
    public Set<Label> processElement(ParserContext context, Element element) throws ConfigurationException {
        Set<Label> labels = null;

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            if (StringUtils.equals(TRANSFORM, child.getTagName())) {
                labels = context.processElement(child);
            }
        }

        return labels;
    }

}
