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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class LocationAwareContentHandler extends DefaultHandler2 {
	final public static String LINE_NUMBER_KEY_NAME = "ln";
	final public static String COLUMN_NUMBER_KEY_NAME = "cn";
	final public static String DOCTYPE_KEY_NAME = "dt";
	final public static String NAMESPACE_KEY_NAME = "nsuri";
	
	
	private final Set<String> namespaceURIs = new HashSet<String>();
	private final Document doc;
	private Locator locator;
	private Element current;
	private Doctype doctype;
	
	@Override
	public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
		doctype = new Doctype(name, publicId, systemId, baseURI);
		
		return new InputSource(new ByteArrayInputStream(new byte[0]));
	}

	public LocationAwareContentHandler(Document d) {
		this.doc = d;
	}

	@Override
	public void setDocumentLocator(final Locator locator) {
		this.locator = locator;
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attrs) throws SAXException {
		Element e = null;
		if (localName != null && !"".equals(localName)) {
			e = doc.createElementNS(uri, localName);
		}
		else {
			e = doc.createElement(qName);
		}
		
		storeLineInformation(e);
		if(StringUtils.isNotBlank(uri)) {
			namespaceURIs.add(uri);
		}
		
		if(doc.getUserData(NAMESPACE_KEY_NAME) == null) {
			doc.setUserData(NAMESPACE_KEY_NAME, namespaceURIs, null);
		}
		
		if (current == null) {
			doc.appendChild(e);
		}
		else {
			current.appendChild(e);
			doc.setUserData(DOCTYPE_KEY_NAME, doctype, null);
		}
		current = e;

		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Attr attr = null;
				if (attrs.getLocalName(i) != null && !"".equals(attrs.getLocalName(i))) {
					attr = doc.createAttributeNS(attrs.getURI(i), attrs.getLocalName(i));
					attr.setValue(attrs.getValue(i));
					storeLineInformation(attr);
					current.setAttributeNodeNS(attr);
				}
				else {
					attr = doc.createAttribute(attrs.getQName(i));
					attr.setValue(attrs.getValue(i));
					storeLineInformation(attr);
					current.setAttributeNode(attr);
				}
			}
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) {
		if (current == null) {
			return;
		}

		Node parent = current.getParentNode();
		// If the parent is the document itself, then we're done.
		if (parent.getParentNode() == null) {
			current.normalize();
			current = null;
		}
		else {
			current = (Element) current.getParentNode();
		}
	}
	
	private void storeLineInformation(Node e) {
	    e.setUserData(LINE_NUMBER_KEY_NAME, this.locator.getLineNumber(), null);
        e.setUserData(COLUMN_NUMBER_KEY_NAME, this.locator.getColumnNumber(), null);
	}

	@Override
	public void characters(char buf[], int offset, int length) {
		if (current != null) {
			Node n = doc.createTextNode(new String(buf, offset, length));
			storeLineInformation(n);
			current.appendChild(n);
		}
	}

	public class Doctype {
		public String name;
		public String publicId;
		public String systemId;
		public String baseURI;

		public Doctype(String name, String publicId, String systemId, String baseURI) {
			this.name = name;
			this.publicId = publicId;
			this.systemId = systemId;
			this.baseURI = baseURI;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPublicId() {
			return publicId;
		}

		public void setPublicId(String publicId) {
			this.publicId = publicId;
		}

		public String getSystemId() {
			return systemId;
		}

		public void setSystemId(String systemId) {
			this.systemId = systemId;
		}

		public String getBaseURI() {
			return baseURI;
		}

		public void setBaseURI(String baseURI) {
			this.baseURI = baseURI;
		}

		@Override
		public String toString() {
			return "Doctype [name=" + name + ", publicId=" + publicId
					+ ", systemId=" + systemId + ", baseURI=" + baseURI + "]";
		}
	}
}
