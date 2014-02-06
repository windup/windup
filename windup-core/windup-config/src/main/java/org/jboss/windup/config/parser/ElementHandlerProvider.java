package org.jboss.windup.config.parser;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementHandlerProvider {
	private static final Logger LOG = LoggerFactory.getLogger(ElementHandlerProvider.class);
	
	@Inject @Any Instance<ElementHandler<?>> handlers;
	
	@Produces
	public Map<String, ElementHandler<?>> produceElementHandlerMap() {
		Map<String, ElementHandler<?>> map = new HashMap<String, ElementHandler<?>>();
		for(ElementHandler<?> handler : handlers) {
			NamespaceElementHandler annotation = handler.getClass().getAnnotation(NamespaceElementHandler.class);
			String key = annotation.namespace() + "::" + annotation.elementName();
			LOG.info("Registering: "+key+" = " + handler.getClass().getName());
			map.put(key, handler);
		}
		return map;
	}
}
