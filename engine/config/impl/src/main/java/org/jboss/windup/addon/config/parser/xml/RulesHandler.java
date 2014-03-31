package org.jboss.windup.addon.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.addon.config.parser.ElementHandler;
import org.jboss.windup.addon.config.parser.NamespaceElementHandler;
import org.jboss.windup.addon.config.parser.ParserContext;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "rules", namespace = "http://windup.jboss.org/v1/xml")
public class RulesHandler implements ElementHandler<Void>
{
   @Override
   public Void processElement(ParserContext handlerManager, Element element)
   {
      List<Element> children = $(element).children().get();
      for (Element child : children)
      {
         handlerManager.processElement(child);
      }
      return null;
   }

}
