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
import org.jboss.windup.resource.type.FileMeta;
import org.jboss.windup.resource.type.JspMeta;
import org.jboss.windup.resource.type.ZipEntryMeta;


/**
 * Interrogates JSP files. Extracts the JSP file, creates a JspMeta object, and passes it down the
 * decorator pipeline.
 * 
 * @author bdavis
 * 
 */
public class JspInterrogator extends ExtensionInterrogator<JspMeta> {

	private static final Log LOG = LogFactory.getLog(JspInterrogator.class);

	@Override
	public JspMeta fileEntryToMeta(FileMeta entry) {
		File file = entry.getFilePointer();
		LOG.debug("Processing: " + file.getAbsolutePath());

		JspMeta meta = new JspMeta();
		meta.setArchiveMeta(entry.getArchiveMeta());
		meta.setFilePointer(file);
		return meta;
	}
	
	@Override
	public JspMeta archiveEntryToMeta(ZipEntryMeta archiveEntry) {
		File file = archiveEntry.getFilePointer();
		LOG.debug("Processing: " + file.getAbsolutePath());

		JspMeta meta = new JspMeta();
		meta.setArchiveMeta(archiveEntry.getArchiveMeta());
		meta.setFilePointer(file);
		return meta;
	}
}
