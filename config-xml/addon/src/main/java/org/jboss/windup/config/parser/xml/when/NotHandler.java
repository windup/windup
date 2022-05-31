package org.jboss.windup.config.parser.xml.when;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Not;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

@NamespaceElementHandler(elementName = "not", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class NotHandler implements ElementHandler<Not> {

    @Override
    public Not processElement(ParserContext handlerManager, Element element) {
        Element child = $(element).children().get().get(0);
        Condition condition = handlerManager.processElement(child);
        return Not.any(condition);
    }

}
