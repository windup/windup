package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.jboss.windup.config.condition.XPathExistsCondition;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.xml.NamespacePrefix;
import org.joox.Match;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="xpath-exists", namespace="http://windup.jboss.org/v1/xml")
public class XPathExistsHandler implements ElementHandler<XPathExistsCondition<?>> {

	
	public XPathExistsHandler() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public XPathExistsCondition<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		XPathExistsCondition<?> condition = new XPathExistsCondition();
		String regex = $(element).attr("matches");
	
		//move up the condition stack until the when.
		Match match = $(element).parentsUntil("when").parent().parent().parent();
		
		Map<String, String> namespaceMapping = new HashMap<String, String>();
		List<Element> elements = match.children("namespace").get();
		for(Element el : elements) {
			NamespacePrefix prefix = (NamespacePrefix)el.getUserData("NAMESPACE");
			if(prefix != null) {
				namespaceMapping.put(prefix.getPrefix(), prefix.getNamespace());
			}
		}
		
		final XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		//NamespaceMapContext context = new NamespaceMapContext(namespaceMapping);
		//xpath.setNamespaceContext(context);
		//expression = xpath.compile(xpathExpression);
		//condition.setPattern(regexPattern);
		return condition;
	}

}
