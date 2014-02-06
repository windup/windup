package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.condition.Condition;
import org.jboss.windup.config.condition.When;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="when", namespace="http://windup.jboss.org/v1/xml")
public class WhenHandler implements ElementHandler<When> {

	@Override
	public When processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		When when = new When();
		List<Element> children = $(element).children().get();
		for(Element child : children) {
			Object obj = handlerManager.processElement(child);
			when.setCondition((Condition)obj);
		}
		return when;
	}

}
