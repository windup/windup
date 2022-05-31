package org.jboss.windup.config.parser.metadata;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "tag", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataTagHandler implements ElementHandler<String> {
    @Override
    public String processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String tag = element.getTextContent();
        if (tag != null) {
            tag = tag.trim();
        }
        return tag;
    }
}
