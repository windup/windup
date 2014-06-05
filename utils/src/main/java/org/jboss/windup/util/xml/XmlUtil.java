package org.jboss.windup.util.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.util.exception.MarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil {
	protected static final Map<String, String> objs;
	
	static {
		objs = new HashMap<String, String>();
		objs.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	}
	
	public static Map<String, String> getSchemaLocations(Document doc) {
		Set<String> namespaces = new HashSet<String>();
		namespaces.addAll(LocationAwareXmlReader.getNamespaces(doc));
		
		
		String schemaLocation;
		try {
			schemaLocation = XmlUtil.xpathExtract(doc, "//@xsi:schemaLocation", objs);
		} catch (MarshallingException e) {
			throw new RuntimeException("Exception extracting xpath.", e);
		}
		
		Map<String, String> result = new HashMap<String, String>();
		if(StringUtils.isNotBlank(schemaLocation)) {
			schemaLocation = StringUtils.trim(schemaLocation);
			String[] locations = schemaLocation.split("\\s+");
			for(int i=0, j=locations.length-1; i<j; i++) {
				if(namespaces.contains(locations[i])) {
					result.put(locations[i], locations[i+1]);
				}
			}
		}
		
		for(String r : result.keySet()) {
			namespaces.remove(r);
		}
		
		//if there are namespaces without namespace locations, load here.
		if(namespaces.size() > 0)  {
			for(String namespace : namespaces) {
				result.put(namespace, null);
			}
		}
		
		return result; 
	}
	
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
