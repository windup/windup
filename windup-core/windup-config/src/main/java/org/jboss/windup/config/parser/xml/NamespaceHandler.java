package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.xml.NamespacePrefix;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="namespace", namespace="http://windup.jboss.org/v1/xml")
public class NamespaceHandler implements ElementHandler<NamespacePrefix> {

	@Override
	public NamespacePrefix processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		NamespacePrefix node = new NamespacePrefix();
		node.setNamespace($(element).attr("uri"));
		node.setPrefix($(element).attr("prefix"));
		
		element.setUserData("NAMESPACE", node, null);
		return node;
	}

}
