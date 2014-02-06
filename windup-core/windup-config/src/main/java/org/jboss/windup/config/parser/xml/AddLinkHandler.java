package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import org.jboss.windup.config.actions.AddLinkAction;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="add-link", namespace="http://windup.jboss.org/v1/xml")
public class AddLinkHandler implements ElementHandler<AddLinkAction<?>> {

	@Override
	public AddLinkAction<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		AddLinkAction action = new AddLinkAction();
		String description = $(element).attr("description");
		String href = $(element).attr("href");
		action.setDescription(description);
		action.setHref(href);
		//TODO: setup groovy.
		
		return action;
	}

}
