package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.Rules;
import org.jboss.windup.config.base.Action;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="rules", namespace="http://windup.jboss.org/v1/xml")
public class RulesHandler implements ElementHandler<Rules> {

	@Override
	public Rules processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		Rules rules = new Rules();
		List<Element> children = $(element).children().get();
		for(Element child : children) {
			Object childElement = handlerManager.processElement(child);
			rules.getActions().add((Action<?>)childElement);
		}
		return rules;
	}

}
