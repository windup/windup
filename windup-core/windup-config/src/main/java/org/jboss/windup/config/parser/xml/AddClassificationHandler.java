package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import org.jboss.windup.config.actions.AddClassificationAction;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="add-classification", namespace="http://windup.jboss.org/v1/xml")
public class AddClassificationHandler implements ElementHandler<AddClassificationAction<?>> {

	@Override
	public AddClassificationAction<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		AddClassificationAction action = new AddClassificationAction();
		String description = $(element).attr("description");
		action.setDescription(description);
		//TODO: setup groovy.
		
		return action;
	}

}
