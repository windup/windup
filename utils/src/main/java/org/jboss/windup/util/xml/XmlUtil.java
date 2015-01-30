package org.jboss.windup.util.xml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.MarshallingException;
import org.jboss.windup.util.exception.XPathException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil
{
    private static Logger LOG = Logging.get(XmlUtil.class);
    protected static final Map<String, String> objs;

    static
    {
        objs = new HashMap<String, String>();
        objs.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    public static String nodeListToString(NodeList nodeList)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            sb.append(nodeToString(node));
        }
        return sb.toString();
    }

    public static String nodeToString(Node node)
    {
        StringWriter sw = new StringWriter();
        if (node instanceof Attr)
        {
            Attr attr = (Attr) node;
            return attr.getValue();
        }
        try
        {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        }
        catch (TransformerException te)
        {
            LOG.warning("Transformer Exception: " + te.getMessage());
        }
        return sw.toString();
    }

    public static Map<String, String> getSchemaLocations(Document doc)
    {
        Set<String> namespaces = new HashSet<String>();
        namespaces.addAll(LocationAwareXmlReader.getNamespaces(doc));

        String schemaLocation;
        try
        {
            schemaLocation = XmlUtil.xpathExtract(doc, "//@xsi:schemaLocation", objs);
        }
        catch (MarshallingException e)
        {
            throw new RuntimeException("Exception extracting xpath.", e);
        }

        Map<String, String> result = new HashMap<String, String>();
        if (StringUtils.isNotBlank(schemaLocation))
        {
            schemaLocation = StringUtils.trim(schemaLocation);
            String[] locations = schemaLocation.split("\\s+");
            for (int i = 0, j = locations.length - 1; i < j; i++)
            {
                if (namespaces.contains(locations[i]))
                {
                    result.put(locations[i], locations[i + 1]);
                }
            }
        }

        for (String r : result.keySet())
        {
            namespaces.remove(r);
        }

        // if there are namespaces without namespace locations, load here.
        if (namespaces.size() > 0)
        {
            for (String namespace : namespaces)
            {
                result.put(namespace, null);
            }
        }

        return result;
    }

    public static boolean xpathExists(Node document, String xpathExpression, Map<String, String> namespaceMapping) throws XPathException,
                MarshallingException
    {
        Boolean result = (Boolean) executeXPath(document, xpathExpression, namespaceMapping, XPathConstants.BOOLEAN);
        return result != null && result;
    }

    public static String xpathExtract(Node document, String xpathExpression, Map<String, String> namespaceMapping) throws XPathException,
                MarshallingException
    {
        return (String) executeXPath(document, xpathExpression, namespaceMapping, XPathConstants.STRING);
    }

    public static NodeList xpathNodeList(Node document, String xpathExpression, Map<String, String> namespaceMapping) throws XPathException,
                MarshallingException
    {
        return (NodeList) executeXPath(document, xpathExpression, namespaceMapping, XPathConstants.NODESET);
    }

    public static NodeList xpathNodeList(Node document, XPathExpression xpathExpression) throws XPathException, MarshallingException
    {
        return (NodeList) executeXPath(document, xpathExpression, XPathConstants.NODESET);
    }

    public static Object executeXPath(Node document, String xpathExpression, Map<String, String> namespaceMapping, QName result)
                throws XPathException, MarshallingException
    {
        NamespaceMapContext mapContext = new NamespaceMapContext(namespaceMapping);
        try
        {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            xpath.setNamespaceContext(mapContext);
            XPathExpression expr = xpath.compile(xpathExpression);

            return executeXPath(document, expr, result);
        }
        catch (XPathExpressionException e)
        {
            throw new XPathException("Xpath(" + xpathExpression + ") cannot be compiled", e);
        }
        catch (Exception e)
        {
            throw new MarshallingException("Exception unmarshalling XML.", e);
        }
    }

    public static Object executeXPath(Node document, XPathExpression expr, QName result) throws XPathException, MarshallingException
    {
        try
        {
            return expr.evaluate(document, result);
        }
        catch (Exception e)
        {
            throw new MarshallingException("Exception unmarshalling XML.", e);
        }
    }
}
