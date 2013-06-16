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
package org.jboss.windup.decorator.gate;

import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.util.NamespaceMapContext;
import org.springframework.beans.factory.InitializingBean;


public class XPathGateDecorator extends GateDecorator<XmlMetadata> implements InitializingBean {
	private static final Log LOG = LogFactory.getLog(XPathGateDecorator.class);
	private static final XPathFactory factory = XPathFactory.newInstance();

	protected XPath xpath;
	protected XPathExpression expression;
	protected Map<String, String> namespaces;
	protected String xpathExpression;

	public void setNamespaces(Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}

	public void setXpathExpression(String xpathExpression) {
		this.xpathExpression = xpathExpression;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			this.xpath = factory.newXPath();
			NamespaceMapContext context = new NamespaceMapContext(namespaces);
			xpath.setNamespaceContext(context);
			expression = xpath.compile(xpathExpression);
		}
		catch (Exception e) {
			LOG.error("Exception while setting XPath: " + xpathExpression, e);
			throw e;
		}
	}

	@Override
	protected boolean continueProcessing(XmlMetadata meta) {
		if (meta.getParsedDocument() == null) {
			LOG.warn("Skipping XPathClassifyingDecorator; no parsed doc: " + meta.getFilePointer().getAbsolutePath());
			return false;
		}

		try {
			final Boolean nodes = (Boolean) expression.evaluate(meta.getParsedDocument(), XPathConstants.BOOLEAN);
			if (nodes != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Returning: " + nodes.booleanValue() + " for XPath: " + xpathExpression + " on file: " + meta.getFilePointer().getAbsolutePath());
				}
				return nodes.booleanValue();
			}
		}
		catch (Exception e) {
			LOG.error("Exception running xpath.", e);
		}

		return false;
	}
}
