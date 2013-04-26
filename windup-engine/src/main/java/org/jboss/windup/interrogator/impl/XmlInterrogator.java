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
package org.jboss.windup.interrogator.impl;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.resource.decoration.Summary;
import org.jboss.windup.resource.decoration.AbstractDecoration.NotificationLevel;
import org.jboss.windup.resource.type.FileMeta;
import org.jboss.windup.resource.type.XmlMeta;
import org.jboss.windup.resource.type.ZipEntryMeta;
import org.w3c.dom.Document;


/**
 * Interrogates XML files. Extracts the XML, and creates an XmlMeta object, which is passed down
 * the decorator pipeline.
 * 
 * @author bdavis
 * 
 */
public class XmlInterrogator extends ExtensionInterrogator<XmlMeta> {
	private static final Log LOG = LogFactory.getLog(XmlInterrogator.class);

	@Override
	public void processMeta(XmlMeta fileMeta) {
		Document document = fileMeta.getParsedDocument();
		if (document == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Document was null.  Problem parsing: " + fileMeta.getFilePointer().getAbsolutePath());
			}
			// attach the bad file so we see it in the reports...
			fileMeta.getArchiveMeta().getEntries().add(fileMeta);
			return;
		}

		super.processMeta(fileMeta);
	}

	@Override
	public boolean isOfInterest(XmlMeta fileMeta) {
		return true;
	}

	@Override
	public XmlMeta archiveEntryToMeta(ZipEntryMeta archiveEntry) {
		File file = archiveEntry.getFilePointer();

		LOG.debug("Processing XML: " + file.getAbsolutePath());

		FileMeta meta = null;

		if (file.length() > 1048576L * 1) {
			LOG.warn("XML larger than 1 MB: " + file.getAbsolutePath() + "; Skipping processing.");
			meta = new FileMeta();
			meta.setArchiveMeta(archiveEntry.getArchiveMeta());
			meta.setFilePointer(file);

			Summary sr = new Summary();
			sr.setDescription("File is too large; skipped.");
			sr.setLevel(NotificationLevel.WARNING);
			meta.getDecorations().add(sr);
		}
		else {
			XmlMeta xmlMeta = new XmlMeta();
			xmlMeta.setArchiveMeta(archiveEntry.getArchiveMeta());
			xmlMeta.setFilePointer(file);
			meta = xmlMeta;

			return xmlMeta;
		}
		return null;
	}

	@Override
	public XmlMeta fileEntryToMeta(FileMeta entry) {
		File file = entry.getFilePointer();

		LOG.debug("Processing XML: " + file.getAbsolutePath());

		FileMeta meta = null;

		if (file.length() > 1048576L * 1) {
			LOG.warn("XML larger than 1 MB: " + file.getAbsolutePath() + "; Skipping processing.");
			meta = new FileMeta();
			//meta.setArchiveMeta(archiveEntry.getArchiveMeta());
			meta.setFilePointer(file);
			meta.setArchiveMeta(entry.getArchiveMeta());
			
			Summary sr = new Summary();
			sr.setDescription("File is too large; skipped.");
			sr.setLevel(NotificationLevel.WARNING);
			meta.getDecorations().add(sr);
		}
		else {
			XmlMeta xmlMeta = new XmlMeta();
			xmlMeta.setArchiveMeta(entry.getArchiveMeta());
			xmlMeta.setFilePointer(file);
			meta = xmlMeta;

			return xmlMeta;
		}
		return null;
	}
}
