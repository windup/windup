package org.jboss.windup.config.parser.xml.when;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.XPathMatchesCondition;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.xml.NamespaceEntry;
import org.jboss.windup.util.xml.NamespaceMapContext;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "xpath-exists", namespace = "http://windup.jboss.org/v1/xml")
public class XPathMatchesHandler implements ElementHandler<XPathMatchesCondition> {
    @Override
    public XPathMatchesCondition processElement(ParserContext handlerManager, Element element) {
        //TODO: Figure out if this can be set to the "current context" instead of rebuilding...
        NamespaceMapContext context = new NamespaceMapContext();
        
        /*
         * look back to the rule tag, and then look for namespaces. 
         * looks up to the namespace tags under the rule tag.
         * <rule><namespace ... /></rule>
         */
        List<Element> namespaces = $(element).parentsUntil("rule").parent().children("namespace").get();
        for(Element namespace : namespaces) {
            NamespaceEntry entry = handlerManager.processElement(namespace);
            context.addNamespaceEntry(entry);
        }
        
        String pattern = $(element).attr("matches");
        XPathMatchesCondition condition = new XPathMatchesCondition(pattern, context);
        return condition;
    }

}
