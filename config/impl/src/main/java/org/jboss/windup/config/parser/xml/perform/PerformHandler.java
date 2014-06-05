package org.jboss.windup.config.parser.xml.perform;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.config.Operations;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "perform", namespace = "http://windup.jboss.org/v1/xml")
public class PerformHandler implements ElementHandler<Operation>
{
   @Override
   public Operation processElement(ParserContext handlerManager, Element element) throws ConfigurationException
   {
      OperationBuilder result = Operations.create();
      List<Element> children = $(element).children().get();
      for (Element child : children)
      {
         Operation operation = handlerManager.processElement(child);
         result = result.and(operation);
      }
      return result;
   }
}
