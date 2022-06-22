package org.jboss.windup.config.parser.xml.when;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.ocpsoft.rewrite.config.True;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "true", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class TrueHandler implements ElementHandler<True> {
    @Override
    public True processElement(ParserContext handlerManager, Element element) {
        return new True();
    }
}
