package org.jboss.windup.engine.util;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.jboss.windup.engine.util.exception.MarshallingException;
import org.jboss.windup.engine.util.xml.NamespaceMapContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil {

	public static boolean xpathExists(Node document, String xpathExpression, Map<String, String> namespaceMapping) throws MarshallingException {
		Boolean result = (Boolean)executeXPath(document, xpathExpression, namespaceMapping, XPathConstants.BOOLEAN);
		return result != null && result;
	}
	
	public static String xpathExtract(Node document, String xpathExpression, Map<String, String> namespaceMapping) throws MarshallingException {
		return (String)executeXPath(document, xpathExpression, namespaceMapping, XPathConstants.STRING);
	}

	public static NodeList xpathNodeList(Node document, String xpathExpression, Map<String, String> namespaceMapping) throws MarshallingException {
		return (NodeList)executeXPath(document, xpathExpression, namespaceMapping, XPathConstants.NODESET);
	}
	
	public static Object executeXPath(Node document, String xpathExpression, Map<String, String> namespaceMapping, QName result) throws MarshallingException {
		NamespaceMapContext mapContext = new NamespaceMapContext(namespaceMapping);
		try {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			xpath.setNamespaceContext(mapContext);
			XPathExpression expr = xpath.compile(xpathExpression);

			return expr.evaluate(document, result);
		}
		catch(Exception e) {
			throw new MarshallingException("Exception unmarshalling XML.", e);
		}
	}
}
