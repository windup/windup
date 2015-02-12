package org.jboss.windup.config.parser.xml.when;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.ocpsoft.rewrite.config.And;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

/**
 * Parses the "when" element, which will contain {@link Condition} that will be combined together via {@link And}.
 */
@NamespaceElementHandler(elementName = "when", namespace = "http://windup.jboss.org/v1/xml")
public class WhenHandler implements ElementHandler<And>
{
    @Override
    public And processElement(ParserContext handlerManager, Element element)
    {
        List<Condition> conditions = new ArrayList<>();
        List<Element> children = $(element).children().get();
        for (Element child : children)
        {
            Condition condition = handlerManager.processElement(child);
            conditions.add(condition);
        }
        return And.all(conditions.toArray(new Condition[conditions.size()]));
    }
}
