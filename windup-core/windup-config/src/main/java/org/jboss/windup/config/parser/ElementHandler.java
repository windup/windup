package org.jboss.windup.config.parser;

import org.w3c.dom.Element;

public interface ElementHandler<T> {
	public T processElement(HandlerManager handlerManager, Element element) throws ConfigurationException;
}
