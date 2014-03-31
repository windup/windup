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
package org.jboss.windup.decorator.xml.ejb;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.metadata.decoration.Line;
import org.jboss.windup.metadata.decoration.effort.StoryPointEffort;
import org.jboss.windup.metadata.decoration.hint.MarkdownHint;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.util.LocationAwareXmlReader;
import org.jboss.windup.util.XmlElementUtil;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class EJBValidatingDecorator implements MetaDecorator<XmlMetadata>, InitializingBean {
	private static final Log LOG = LogFactory.getLog(EJBValidatingDecorator.class);
	private static final XPathFactory factory = XPathFactory.newInstance();

	private static final String XPATH_EJB_RELATIONSHIP = "//*[local-name()='ejb-relation']/*[local-name()='ejb-relationship-role'][2]";
	private static final String XPATH_EJB_NAME_PROTOTYPE = "//*[local-name()='entity'][ejb-name='${entity-name}']";
	private XPath xpath;
	private XPathExpression ejbRelationshipExpression;

	@Override
	public void processMeta(XmlMetadata meta) {
		// first, find any relationship elements...
		Set<String> ejbNames = new HashSet<String>();

		try {
			Document doc = meta.getParsedDocument();
			if (doc == null) {
				throw new NullPointerException();
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing: " + ejbRelationshipExpression);
			}
			final NodeList nodes = (NodeList) ejbRelationshipExpression.evaluate(doc, XPathConstants.NODESET);

			if (nodes != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found results for: " + meta.getFilePointer().getAbsolutePath());
				}
				for (int i = 0; i < nodes.getLength(); i++) {
					Element relationshipRoleSource = XmlElementUtil.getChildByTagName((Element) nodes.item(i), "relationship-role-source");
					Element ejbName = XmlElementUtil.getChildByTagName(relationshipRoleSource, "ejb-name");

					if (LOG.isDebugEnabled()) {
						LOG.debug("Found relationship: " + ejbName.getTextContent());
					}
					ejbNames.add(ejbName.getTextContent());
				}
			}
			// now, validate that all the EJBs provide local interfaces.
			for (String ejbName : ejbNames) {
				String ejbLookup = StringUtils.replace(XPATH_EJB_NAME_PROTOTYPE, "${entity-name}", ejbName);

				Element ejbEntityNode = (Element) xpath.evaluate(ejbLookup, doc, XPathConstants.NODE);
				Integer lineNumber = (Integer) LocationAwareXmlReader.getLineNumber(ejbEntityNode);
				// validate that the local and local-home tags exist.

				Element localTag = XmlElementUtil.getChildByTagName(ejbEntityNode, "local");
				Element localHomeTag = XmlElementUtil.getChildByTagName(ejbEntityNode, "local-home");

				if (LOG.isDebugEnabled()) {
					LOG.info("XPath: " + ejbLookup);
					LOG.debug("Line: " + lineNumber);
					LOG.debug("Local tag null: " + (localTag == null));
					LOG.debug("Local-home tag null: " + (localHomeTag == null));
				}

				if (localTag == null) {
					Line result = new Line();
					result.setDescription("Entity: " + ejbName + " does not expose required local interface.");
					result.setLineNumber(lineNumber);
					result.setPattern("//entity[ejb-name]/local");
					result.setEffort(new StoryPointEffort(1));
					MarkdownHint simpleHint = new MarkdownHint();
					simpleHint.setMarkdown("Create a local interface for the Entity bean to support Container Managed Relationship (CMR).");
					result.getHints().add(simpleHint);
					meta.getDecorations().add(result);
				}
				if (localHomeTag == null) {
					Line result = new Line();
					result.setDescription("Entity: " + ejbName + " does not expose required local-home interface.");
					result.setLineNumber(lineNumber);
					result.setPattern("//entity[ejb-name]/local-home");
					result.setEffort(new StoryPointEffort(1));
					
					MarkdownHint simpleHint = new MarkdownHint();
					simpleHint.setMarkdown("Create a local-home interface for the Entity bean to support Container Managed Relationship (CMR).");
					
					result.getHints().add(simpleHint);
					meta.getDecorations().add(result);
				}
			}

		}
		catch (Exception e) {
			LOG.error("Exception during XPath.", e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.xpath = factory.newXPath();
		ejbRelationshipExpression = xpath.compile(XPATH_EJB_RELATIONSHIP);
	}

}
