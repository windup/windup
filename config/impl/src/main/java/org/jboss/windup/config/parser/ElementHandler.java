package org.jboss.windup.config.parser;

import org.jboss.windup.config.ConfigurationException;
import org.w3c.dom.Element;

public interface ElementHandler<T>
{
   public T processElement(ParserContext handlerManager, Element element) throws ConfigurationException;
}
