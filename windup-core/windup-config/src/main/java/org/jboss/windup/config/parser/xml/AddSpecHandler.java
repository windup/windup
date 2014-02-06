package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import org.jboss.windup.config.actions.AddSpecAction;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="add-spec-version", namespace="http://windup.jboss.org/v1/xml")
public class AddSpecHandler implements ElementHandler<AddSpecAction<?>> {

	@Override
	public AddSpecAction<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		AddSpecAction action = new AddSpecAction();
		String version = $(element).attr("version");
		action.setSpecificationVersion(version);
		//TODO: setup groovy.
		
		return action;
	}

}
