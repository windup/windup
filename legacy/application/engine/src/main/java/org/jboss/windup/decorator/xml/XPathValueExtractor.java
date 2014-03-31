package org.jboss.windup.decorator.xml;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.hint.ResultProcessor;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.util.NamespaceMapContext;
import org.jboss.windup.metadata.util.LocationAwareXmlReader;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathValueExtractor implements MetaDecorator<XmlMetadata>, InitializingBean {

	private static final Log LOG = LogFactory.getLog(XPathSummaryDecorator.class);
	private static final XPathFactory factory = XPathFactory.newInstance();

	private List<ResultProcessor> hints = new LinkedList<ResultProcessor>();
	private XPath xpath;
	private XPathExpression expression;

	private Map<String, String> namespaces;
	private String xpathExpression;
	
	private String contextTarget;
	
	public void setContextTarget(String contextTarget) {
		this.contextTarget = contextTarget;
	}
	
	@Override
	public void processMeta(final XmlMetadata meta) {
		if (meta.getParsedDocument() == null) {
			LOG.warn("Skipping XPathClassifyingDecorator: " + meta.getFilePointer().getAbsolutePath() + " because the document is unparsed.");
			return;
		}

		try {
			Document doc = meta.getParsedDocument();
			if (doc == null) {
				throw new NullPointerException();
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing: " + xpathExpression);
			}
			final NodeList nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

			if (nodes != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found results for: " + meta.getFilePointer().getAbsolutePath());
				}
				for (int i = 0; i < nodes.getLength(); i++) {
					Integer lineNumber = (Integer) LocationAwareXmlReader.getLineNumber(nodes.item(i));
					String match = convertNode(nodes.item(i));
					
					if(StringUtils.isNotBlank(match)) {
						meta.getContext().put(contextTarget, match);
					}
					
				}
			}
		}
		catch (Exception e) {
			LOG.error("Exception during XPath.", e);
		}
	}
	

	protected String convertNode(Node node) throws TransformerException {
		StringWriter sw = new StringWriter();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		sw.append(node.getTextContent());
		return sw.toString();
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.xpath = factory.newXPath();
		NamespaceMapContext context = new NamespaceMapContext(namespaces);
		xpath.setNamespaceContext(context);
		expression = xpath.compile(xpathExpression);
	}

}
