package org.jboss.windup.config.parser;

import static org.joox.JOOX.$;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.ConfigurationException;
import org.jboss.windup.config.parser.util.Annotations;
import org.jboss.windup.config.parser.util.HandlerId;
import org.ocpsoft.common.services.ServiceLoader;
import org.ocpsoft.common.util.Iterators;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderPerform;
import org.ocpsoft.rewrite.config.ConfigurationRuleParameterWhere;
import org.w3c.dom.Element;

public class ParserContext
{
   private final Map<HandlerId, ElementHandler<?>> handlers = new HashMap<>();
   private final ConfigurationBuilder builder;
   private ConfigurationRuleBuilderPerform rule;
   private ConfigurationRuleParameterWhere where;

   @SuppressWarnings({ "rawtypes", "unchecked" })
   public ParserContext(ConfigurationBuilder builder)
   {
      List<ElementHandler> loadedHandlers = Iterators.asList(ServiceLoader.load(ElementHandler.class));
      for (ElementHandler handler : loadedHandlers)
      {
         NamespaceElementHandler annotation = Annotations.getAnnotation(handler.getClass(),
                  NamespaceElementHandler.class);
         if (annotation != null)
         {
            handlers.put(new HandlerId(annotation.namespace(), annotation.elementName()), handler);
         }
      }

      this.builder = builder;
   }

   @SuppressWarnings("unchecked")
   public <T> T processElement(Element element) throws ConfigurationException
   {
      String namespace = $(element).namespaceURI();
      String tagName = $(element).tag();
      ElementHandler<?> handler = handlers.get(new HandlerId(namespace, tagName));
      if (handler != null)
      {
         Object o = handler.processElement(this, element);
         return (T) o;
      }
      throw new ConfigurationException("No Handler registered for element named [" + tagName
               + "] in namespace: [" + namespace + "]");
   }

   public ConfigurationBuilder getBuilder()
   {
      return builder;
   }

   public void setRuleBuilder(ConfigurationRuleBuilderPerform perform)
   {
      this.rule = perform;
   }

   public ConfigurationRuleBuilderPerform getRule()
   {
      return rule;
   }

   public void setWhereBuilder(ConfigurationRuleParameterWhere where)
   {
      this.where = where;
   }

   public ConfigurationRuleParameterWhere getWhere()
   {
      return where;
   }
}
