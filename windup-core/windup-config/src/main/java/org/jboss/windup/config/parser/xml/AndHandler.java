package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.condition.AndCondition;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="and", namespace="http://windup.jboss.org/v1/xml")
public class AndHandler implements ElementHandler<AndCondition<?>> {

	@Override
	public AndCondition<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		AndCondition and = new AndCondition();
		List<Element> children = $(element).children().get();
		for(Element child : children) {
			Object obj = handlerManager.processElement(child);
			and.getConditions().add(obj);
		}
		return and;
	}

}
