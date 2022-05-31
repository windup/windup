package org.jboss.windup.config.parser.xml;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.w3c.dom.Element;

import java.util.List;

import static org.joox.JOOX.$;

@NamespaceElementHandler(elementName = "rules", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class RulesHandler implements ElementHandler<Void> {
    @Override
    public Void processElement(ParserContext handlerManager, Element element) {
        List<Element> children = $(element).children().get();
        for (Element child : children) {
            handlerManager.processElement(child);
        }
        return null;
    }

}
