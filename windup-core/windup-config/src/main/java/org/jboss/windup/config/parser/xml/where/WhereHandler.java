package org.jboss.windup.config.parser.xml.where;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderPerform;
import org.ocpsoft.rewrite.config.ConfigurationRuleParameterWhere;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "where", namespace = "http://windup.jboss.org/v1/xml")
public class WhereHandler implements ElementHandler<Void>
{
   @Override
   public Void processElement(ParserContext context, Element element)
   {
      ConfigurationRuleBuilderPerform rule = context.getRule();

      String name = $(element).attr("name");

      ConfigurationRuleParameterWhere where = rule.where(name);
      context.setWhereBuilder(where);

      List<Element> children = $(element).children().get();
      for (Element child : children)
      {
         context.processElement(child);
      }

      return null;
   }
}
