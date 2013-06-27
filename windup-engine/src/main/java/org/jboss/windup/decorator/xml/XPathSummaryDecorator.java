/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
*/
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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.gate.GateDecorator;
import org.jboss.windup.hint.ResultProcessor;
import org.jboss.windup.metadata.decoration.Summary;
import org.jboss.windup.metadata.decoration.XmlLine;
import org.jboss.windup.metadata.decoration.effort.Effort;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.util.NamespaceMapContext;
import org.jboss.windup.metadata.util.LocationAwareXmlReader;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XPathSummaryDecorator extends GateDecorator<XmlMetadata> implements InitializingBean {

	private static final Log LOG = LogFactory.getLog(XPathSummaryDecorator.class);
	private static final XPathFactory factory = XPathFactory.newInstance();

	private List<ResultProcessor> hints = new LinkedList<ResultProcessor>();
	private XPath xpath;
	private XPathExpression expression;

	private String matchDescription;

	private Map<String, String> namespaces;
	private String xpathExpression;
	private boolean inline = false;
	private Effort effort;

	public void setEffort(Effort effort) {
		this.effort = effort;
	}

	public Effort getEffort() {
		return effort;
	}

	public void setHints(List<ResultProcessor> hints) {
		this.hints = hints;
	}

	public void setXpath(XPath xpath) {
		this.xpath = xpath;
	}

	public void setInline(String inline) {
		this.inline = BooleanUtils.toBoolean(inline);
	}

	public void setMatchDescription(String matchDescription) {
		this.matchDescription = matchDescription;
	}

	public void setNamespaces(Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}

	public void setXpathExpression(String xpathExpression) {
		this.xpathExpression = xpathExpression;
	}


    @Override
    protected boolean continueProcessing(XmlMetadata meta) {
        boolean isProcessed = false;

        if (meta.getParsedDocument() == null) {
            LOG.warn("Skipping XPathClassifyingDecorator: " +
                meta.getFilePointer().getAbsolutePath() +
                " because the document is unparsed.");
            return isProcessed;
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

            if (nodes != null && nodes.getLength() > 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found results for: " + meta.getFilePointer().getAbsolutePath());
                }
                for (int i = 0; i < nodes.getLength(); i++) {
                    Integer lineNumber = (Integer) LocationAwareXmlReader.getLineNumber(nodes.item(i));
                    String match = convertNode(nodes.item(i));
                    if (inline && lineNumber != null) {
                        createLineNumberMeta(meta, lineNumber, this.matchDescription, nodes.item(i));
                    }
                    else {
                        createSummaryMeta(meta, this.matchDescription, match);
                    }
                }
                isProcessed = true;
            }
        }
        catch (Exception e) {
            LOG.error("Exception during XPath.", e);
        }
        return isProcessed;
    }


	protected void createSummaryMeta(final XmlMetadata meta, String description, String match) {
		Summary result = new Summary();
		result.setDescription(description);
		result.setPattern(match);
		result.setEffort(effort);
		for (ResultProcessor hint : hints) {
			hint.process(result);
		}
		meta.getDecorations().add(result);
	}

	protected void createLineNumberMeta(final XmlMetadata meta, Integer lineNumber, String descripiton, Node match) {
		XmlLine result = new XmlLine();
		result.setDescription(descripiton);

		try {
			result.setPattern(convertNode(match));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		result.setMatchedNode(match);
		result.setLineNumber(lineNumber);
		result.setEffort(effort);
		for (ResultProcessor hint : hints) {
			hint.process(result);
		}
		meta.getDecorations().add(result);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.xpath = factory.newXPath();
		NamespaceMapContext context = new NamespaceMapContext(namespaces);
		xpath.setNamespaceContext(context);
		expression = xpath.compile(xpathExpression);
	}

	protected String convertNode(Node node) throws TransformerException {
		StringWriter sw = new StringWriter();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		sw.append(node.getTextContent());
		return sw.toString();
	}
}
