package org.jboss.windup.addon.config.parser.xml.where;

import static org.joox.JOOX.$;

import org.jboss.windup.addon.config.parser.ElementHandler;
import org.jboss.windup.addon.config.parser.NamespaceElementHandler;
import org.jboss.windup.addon.config.parser.ParserContext;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "matches", namespace = "http://windup.jboss.org/v1/xml")
public class MatchesHandler implements ElementHandler<Void>
{
   @Override
   public Void processElement(ParserContext context, Element element)
   {
      context.getWhere().matches($(element).attr("pattern"));
      return null;
   }
}
