package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import org.jboss.windup.config.condition.SHA1Condition;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="sha1", namespace="http://windup.jboss.org/v1/xml")
public class SHA1Handler implements ElementHandler<SHA1Condition<?>> {

	@Override
	public SHA1Condition<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		SHA1Condition sha1 = new SHA1Condition();
		String value = $(element).attr("equals");
		sha1.setValue(value);
		return sha1;
	}

}
