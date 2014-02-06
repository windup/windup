package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.condition.Condition;
import org.jboss.windup.config.condition.NotCondition;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="not", namespace="http://windup.jboss.org/v1/xml")
public class NotHandler implements ElementHandler<NotCondition<?>> {

	@Override
	public NotCondition<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		NotCondition not = new NotCondition();
		List<Element> children = $(element).children().get();
		
		for(Element child : children) {
			Object obj = handlerManager.processElement(child);
			not.setCondition((Condition)obj);
		}
		return not;
	}

}
