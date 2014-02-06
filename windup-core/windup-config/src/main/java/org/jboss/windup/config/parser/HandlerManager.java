package org.jboss.windup.config.parser;

import static org.joox.JOOX.$;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class HandlerManager {
	private static final Logger LOG = LoggerFactory.getLogger(HandlerManager.class);

	@Inject
	private Map<String, ElementHandler<?>> map;

	public Object processElement(Element element) throws ConfigurationException {
		String namespace = $(element).namespaceURI();
		String nodeType = $(element).tag();
		ElementHandler<?> handler = map.get(namespace+"::"+nodeType);
		if(handler != null) {
			Object o = handler.processElement(this, element);
			return o;
		}
		else {
			LOG.warn("Element not found: "+namespace+" -> "+nodeType);
		}
		
		return null;
	}
}
