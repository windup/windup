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
package org.jboss.windup.metadata.type;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.SoftReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.Summary;
import org.jboss.windup.metadata.decoration.AbstractDecoration.NotificationLevel;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;
import org.jboss.windup.metadata.util.LocationAwareXmlReader;
import org.w3c.dom.Document;


/**
 * Keeps an internal reference to the XML DOM version of the XML document, in addition to the file reference.
 * This allows XPath Queries and XSLT reports to generate extremely fast against the XmlMetadata data in the
 * decorator pipeline.
 * 
 * Note: Make sure to release the document when the XML Interrogator returns to free the reference to the XML
 * document from memory!
 * 
 * @author bdavis
 * 
 */
public class XmlMetadata extends FileMetadata {
	private static final Log LOG = LogFactory.getLog(XmlMetadata.class);

	protected SoftReference<Document> parsedDocumentRef;

	@Override
	public void setFilePointer(File filePointer) {
		super.setFilePointer(filePointer);
	}

	protected Document hydrateDocument() {
		if (parsedDocumentRef == null) {
			FileInputStream fis = null;
			try {
				fis = FileUtils.openInputStream(filePointer);
				Document parsedDocument = LocationAwareXmlReader.readXML(fis);

				LOG.debug("Hydrating XML Document: " + filePointer.getAbsolutePath());
				parsedDocumentRef = new SoftReference<Document>(parsedDocument);
			}
			catch (Exception e) {
				LOG.error("Bad XML? " + filePointer.getAbsolutePath());
				LOG.info("Skipping file. Continuing Windup Processing...");

				Summary sr = new Summary();
				sr.setDescription("Bad XML? Unable to parse.");
				sr.setLevel(NotificationLevel.CRITICAL);
				sr.setEffort(new UnknownEffort());
				this.getDecorations().add(sr);
				this.parsedDocumentRef = null;

				return null;
			}
			finally {
				IOUtils.closeQuietly(fis);
			}
		}
		return parsedDocumentRef.get();
	}

	public Document getParsedDocument() {
		return hydrateDocument();
	}

	public void releaseParsedDocument() {
		parsedDocumentRef = null;
	}

	public void setParsedDocument(Document parsedDocument) {
		this.parsedDocumentRef = new SoftReference<Document>(parsedDocument);
	}

	@Override
	public String toString() {
		return "XmlMetadata [decorations=" + decorations + ", filePointer=" + filePointer + "]";
	}
}
