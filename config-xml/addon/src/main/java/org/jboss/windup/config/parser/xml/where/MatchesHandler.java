package org.jboss.windup.config.parser.xml.where;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

@NamespaceElementHandler(elementName = "matches", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MatchesHandler implements ElementHandler<Void> {
    @Override
    public Void processElement(ParserContext context, Element element) {
        context.getWhere().matches($(element).attr("pattern"));
        return null;
    }
}
