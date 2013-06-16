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
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.JspMetadata;
import org.jboss.windup.metadata.type.ZipEntryMetadata;


/**
 * Interrogates JSP files. Extracts the JSP file, creates a JspMetadata object, and passes it down the
 * decorator pipeline.
 * 
 * @author bdavis
 * 
 */
public class JspInterrogator extends ExtensionInterrogator<JspMetadata> {

	private static final Log LOG = LogFactory.getLog(JspInterrogator.class);

	@Override
	public JspMetadata fileEntryToMeta(FileMetadata entry) {
		File file = entry.getFilePointer();
		LOG.debug("Processing: " + file.getAbsolutePath());

		JspMetadata meta = new JspMetadata();
		meta.setArchiveMeta(entry.getArchiveMeta());
		meta.setFilePointer(file);
		return meta;
	}
	
	@Override
	public JspMetadata archiveEntryToMeta(ZipEntryMetadata archiveEntry) {
		File file = archiveEntry.getFilePointer();
		LOG.debug("Processing: " + file.getAbsolutePath());

		JspMetadata meta = new JspMetadata();
		meta.setArchiveMeta(archiveEntry.getArchiveMeta());
		meta.setFilePointer(file);
		return meta;
	}
}
