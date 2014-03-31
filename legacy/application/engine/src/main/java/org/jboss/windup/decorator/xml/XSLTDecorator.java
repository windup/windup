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

import java.io.File;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.metadata.decoration.Link;
import org.jboss.windup.metadata.decoration.effort.Effort;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.springframework.beans.factory.InitializingBean;


public class XSLTDecorator implements MetaDecorator<XmlMetadata>, InitializingBean {
	private static final Log LOG = LogFactory.getLog(XSLTDecorator.class);

	private String xsltLocation;

	private String outputExtension;
	private String outputDescription;

	private Transformer xsltTransformer;
	private Map<String, String> xsltParameters;

	private Effort effort = new UnknownEffort();

	public Effort getEffort() {
		return effort;
	}

	public void setXsltParameters(Map<String, String> xsltParameters) {
		this.xsltParameters = xsltParameters;
	}

	public void setEffort(Effort effort) {
		this.effort = effort;
	}

	public void setXsltLocation(String xsltLocation) {
		this.xsltLocation = xsltLocation;
	}

	public void setOutputDescription(String outputDescription) {
		this.outputDescription = outputDescription;
	}

	public void setOutputExtension(String outputExtension) {
		this.outputExtension = outputExtension;
	}

	@Override
	public void processMeta(XmlMetadata file) {
		String relativeDirectory = StringUtils.substringBeforeLast(file.getFilePointer().getAbsolutePath(), File.separator);
		String fileName = StringUtils.substringAfterLast(file.getFilePointer().getAbsolutePath(), File.separator);

		fileName = StringUtils.replace(fileName, ".", "-");
		fileName = fileName + outputExtension;

		File relativeFile = new File(relativeDirectory + File.separator + fileName);

		Source xmlSource = new DOMSource(file.getParsedDocument());
		Result xmlResult = new StreamResult(relativeFile);

		try {
			xsltTransformer.transform(xmlSource, xmlResult);

			Link linkResult = new Link();
			linkResult.setDescription(outputDescription);
			linkResult.setLink(fileName);
			linkResult.setEffort(effort);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Created link: " + ReflectionToStringBuilder.toString(linkResult));
			}

			file.getDecorations().add(linkResult);
		}
		catch (TransformerException e) {
			LOG.error("Exception transforming XML.", e);
		}
	}

	@Override
	public void afterPropertiesSet() {
		LOG.debug("Getting XSLT Location: " + xsltLocation);
		Source xsltSource = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(xsltLocation));
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setURIResolver(new URIResolver() {
				@Override
				public Source resolve(String href, String base) throws TransformerException {
					// fetch local only, for speed reasons.
					if (StringUtils.contains(href, "http://")) {
						LOG.warn("Trying to fetch remote URL for XSLT.  This is not possible; for speed reasons: " + href + ": " + base);
						return null;
					}
					return new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(href));
				}
			});

			xsltTransformer = tf.newTransformer(xsltSource);
			if (xsltParameters != null) {
				for (String key : xsltParameters.keySet()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Setting property: " + key + " -> " + xsltParameters.get(key));
					}
					xsltTransformer.setParameter(key, xsltParameters.get(key));
				}
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Created XSLT successfully: " + xsltLocation);
			}
		}
		catch (Exception e) {
			LOG.error("Exception creating XSLT: " + xsltLocation, e);
		}
	}
}
