package org.jboss.windup.config.parser.xml.when;

import static org.joox.JOOX.$;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.xml.NamespaceEntry;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "namespace", namespace = "http://windup.jboss.org/v1/xml")
public class NamespaceHandler implements ElementHandler<NamespaceEntry>
{
   @Override
   public NamespaceEntry processElement(ParserContext handlerManager, Element element)
   {
       String prefix = $(element).attr("prefix");
       String namespaceURI = $(element).attr("uri");
       NamespaceEntry entry = new NamespaceEntry(prefix, namespaceURI);
       return entry;
   }

}
