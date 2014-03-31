package org.jboss.windup.addon.config.parser;

import org.jboss.windup.addon.config.ConfigurationException;
import org.w3c.dom.Element;

public interface ElementHandler<T>
{
   public T processElement(ParserContext handlerManager, Element element) throws ConfigurationException;
}
