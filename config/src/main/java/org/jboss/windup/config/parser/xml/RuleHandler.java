package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderPerform;
import org.ocpsoft.rewrite.config.Operation;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "rule", namespace = "http://windup.jboss.org/v1/xml")
public class RuleHandler implements ElementHandler<Void>
{
   @Override
   public Void processElement(ParserContext context, Element element)
   {
      ConfigurationRuleBuilderCustom rule = context.getBuilder().addRule();

      List<Element> children = $(element).children().get();
      for (Element child : children)
      {
         Object result = context.processElement(child);

         switch ($(child).tag())
         {
         case "when":
            rule.when(((Condition) result));
            break;

         case "perform":
            ConfigurationRuleBuilderPerform perform = rule.perform(((Operation) result));
            context.setRuleBuilder(perform);
            break;

         case "otherwise":
            rule.perform(((Operation) result));

            break;
         case "where":
            break;
         }
      }
      return null;
   }

}
