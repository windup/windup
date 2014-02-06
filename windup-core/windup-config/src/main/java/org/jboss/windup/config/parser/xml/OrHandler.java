package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.condition.OrCondition;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="or", namespace="http://windup.jboss.org/v1/xml")
public class OrHandler implements ElementHandler<OrCondition<?>> {

	@Override
	public OrCondition<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		OrCondition or = new OrCondition();
		List<Element> children = $(element).children().get();
		for(Element child : children) {
			Object obj = handlerManager.processElement(child);
			or.getConditions().add(obj);
		}
		return or;
	}

}
