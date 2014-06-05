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
package org.jboss.windup.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LocationAwareXmlReader {
	private static final SAXParserFactory factory = SAXParserFactory.newInstance();
	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	
	static {
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		factory.setXIncludeAware(false);
	}

	public static Document readXML(final InputStream is) throws IOException, SAXException {
		final Document doc;
		SAXParser parser;
		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			parser = factory.newSAXParser();
			doc = docBuilder.newDocument();
		}
		catch (final ParserConfigurationException e) {
			throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
		}
		
		final DefaultHandler handler = new LocationAwareContentHandler(doc);
		
		
		parser.parse(is, handler);
		return doc;
	}
	
	public static Integer getLineNumber(Node node) {
		return (Integer) node.getUserData(LocationAwareContentHandler.LINE_NUMBER_KEY_NAME);
	}
	
	public static Set<String> getNamespaces(Document doc) {
		return (Set<String>) doc.getUserData(LocationAwareContentHandler.NAMESPACE_KEY_NAME);
	}
}
