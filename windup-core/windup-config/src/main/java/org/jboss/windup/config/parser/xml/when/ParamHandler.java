package org.jboss.windup.config.parser.xml.when;

import static org.joox.JOOX.$;

import org.jboss.windup.config.condition.ParamCondition;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "param", namespace = "http://windup.jboss.org/v1/xml")
public class ParamHandler implements ElementHandler<Condition>
{
   @Override
   public Condition processElement(ParserContext handlerManager, Element element)
   {
      return new ParamCondition($(element).attr("name"), $(element).attr("value"));
   }
}
