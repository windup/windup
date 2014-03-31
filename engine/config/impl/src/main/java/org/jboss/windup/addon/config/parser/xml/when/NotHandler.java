package org.jboss.windup.addon.config.parser.xml.when;

import static org.joox.JOOX.$;

import org.jboss.windup.addon.config.parser.ElementHandler;
import org.jboss.windup.addon.config.parser.NamespaceElementHandler;
import org.jboss.windup.addon.config.parser.ParserContext;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Not;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "not", namespace = "http://windup.jboss.org/v1/xml")
public class NotHandler implements ElementHandler<Not>
{

   @Override
   public Not processElement(ParserContext handlerManager, Element element)
   {
      Element child = $(element).children().get().get(0);
      Condition condition = handlerManager.processElement(child);
      return Not.any(condition);
   }

}
