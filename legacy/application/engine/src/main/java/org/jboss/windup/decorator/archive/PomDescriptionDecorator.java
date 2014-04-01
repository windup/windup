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
package org.jboss.windup.decorator.archive;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.metadata.decoration.Summary;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;


public class PomDescriptionDecorator implements MetaDecorator<XmlMetadata>, InitializingBean {
	private static final Log LOG = LogFactory.getLog(PomVersionDecorator.class);

	private static final XPathFactory factory = XPathFactory.newInstance();

	private static final String DESCRIPTION = "/*[local-name()='project']/*[local-name()='description']";
	protected XPathExpression xPath;

	@Override
	public void processMeta(XmlMetadata file) {
		Document doc = file.getParsedDocument();
		try {
			String description = extractStringValue(xPath, doc);
			if (StringUtils.isNotBlank(description)) {
				Summary result = new Summary();
				result.setDescription(description);

				// add the link to the archive.
				file.getArchiveMeta().getDecorations().add(result);
			}
		}
		catch (XPathExpressionException e) {
			LOG.error("Exception running xpath expression.", e);
		}
	}

	protected String extractStringValue(XPathExpression expression, Document doc) throws XPathExpressionException {
		return (String) expression.evaluate(doc, XPathConstants.STRING);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		XPath xpath = factory.newXPath();
		xPath = xpath.compile(DESCRIPTION);
	}
}
