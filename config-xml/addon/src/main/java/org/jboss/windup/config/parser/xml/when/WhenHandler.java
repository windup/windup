package org.jboss.windup.config.parser.xml.when;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.ocpsoft.rewrite.config.And;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static org.joox.JOOX.$;

/**
 * Parses the "when" element, which will contain {@link Condition} that will be combined together via {@link And}.
 */
@NamespaceElementHandler(elementName = "when", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class WhenHandler implements ElementHandler<And> {
    @Override
    public And processElement(ParserContext handlerManager, Element element) {
        List<Condition> conditions = new ArrayList<>();
        List<Element> children = $(element).children().get();
        for (Element child : children) {
            Condition condition = handlerManager.processElement(child);
            conditions.add(condition);
        }
        return And.all(conditions.toArray(new Condition[conditions.size()]));
    }
}
