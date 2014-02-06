package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import org.jboss.windup.config.condition.MD5Condition;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="md5", namespace="http://windup.jboss.org/v1/xml")
public class MD5Handler implements ElementHandler<MD5Condition<?>> {

	@Override
	public MD5Condition<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		MD5Condition cond = new MD5Condition();
		String value = $(element).attr("equals");
		cond.setValue(value);
		return cond;
	}

}
