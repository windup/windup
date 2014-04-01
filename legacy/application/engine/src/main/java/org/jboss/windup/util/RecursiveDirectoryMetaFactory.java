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
package org.jboss.windup.util;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.type.archive.DirectoryMetadata;

public class RecursiveDirectoryMetaFactory {

	private static final Log LOG = LogFactory.getLog(RecursiveDirectoryMetaFactory.class);
	
	private final File start;
	
	public RecursiveDirectoryMetaFactory(File dir) {
		this.start = dir;
	}
	
	public DirectoryMetadata recursivelyExtract() {
		DirectoryMetadata dirMeta = new DirectoryMetadata();
		dirMeta.setFilePointer(start);
		dirMeta.setName(start.getName());
		dirMeta.setRelativePath("windup");
		populateChildren(dirMeta);
		
		return dirMeta;
	}
	
	protected void populateChildren(DirectoryMetadata parent) {
		File[] files = parent.getFilePointer().listFiles();
		if(LOG.isDebugEnabled()) {
			LOG.debug(files.length + " Children of "+parent.getFilePointer().getAbsolutePath());
		}
		
		for(File file : files) {
			if(file.isDirectory()) {
				if(file.isHidden()) {
					LOG.debug("Skipping hidden directory: "+file.getAbsolutePath());
					continue;
				}
				
				DirectoryMetadata dirMeta = new DirectoryMetadata();
				parent.getNestedArchives().add(dirMeta);
				dirMeta.setArchiveMeta(parent);
				dirMeta.setFilePointer(file);
				dirMeta.setRelativePath(generateRelativePath(file));
				dirMeta.setName(file.getName());
				
				if(LOG.isTraceEnabled()) {
					LOG.trace("Added child: "+dirMeta.getRelativePath());
				}
				populateChildren(dirMeta);
			}
		}
	}
	
	protected String generateRelativePath(File dir) 
	{
		String absPath = start.getParentFile().getAbsolutePath();
		String relative = StringUtils.removeStart(dir.getAbsolutePath(), absPath);
		relative = StringUtils.replace(relative, "\\", "/");
		return relative;
	}
}
