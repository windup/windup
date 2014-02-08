package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.base.Action;
import org.jboss.windup.config.condition.Condition;
import org.jboss.windup.config.condition.When;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.xml.NamespacePrefix;
import org.jboss.windup.config.xml.XmlAction;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="xml-action", namespace="http://windup.jboss.org/v1/xml")
public class XmlActionHandler implements ElementHandler<XmlAction<?>> {

	@Override
	public XmlAction<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		XmlAction xmlAction = new XmlAction();
		List<Element> children = $(element).children().get();
		for(Element child : children) {
			//get the child of when...
			Object obj = handlerManager.processElement(child);
			if(obj instanceof When) {
				Condition condition = ((When)obj).getCondition();
				xmlAction.setCondition(condition);
				xmlAction.getActions().add(((When)obj).getAction());
			}
			else if(obj instanceof NamespacePrefix) {
				xmlAction.getNamespacePrefixes().add(obj);
			}
			else if(obj instanceof Action<?>) {
				xmlAction.getActions().add(obj);
			}
			
		}
		return xmlAction;
	}

}
