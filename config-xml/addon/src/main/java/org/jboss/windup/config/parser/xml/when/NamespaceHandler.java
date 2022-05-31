package org.jboss.windup.config.parser.xml.when;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.util.xml.NamespaceEntry;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

@NamespaceElementHandler(elementName = "namespace", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class NamespaceHandler implements ElementHandler<NamespaceEntry> {
    @Override
    public NamespaceEntry processElement(ParserContext handlerManager, Element element) {
        String prefix = $(element).attr("prefix");
        String namespaceURI = $(element).attr("uri");
        NamespaceEntry entry = new NamespaceEntry(prefix, namespaceURI);
        return entry;
    }

}
