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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TempSourceMetadata extends FileMetadata {
	private static final Log LOG = LogFactory.getLog(TempSourceMetadata.class);
	
	private final File source;
	
	public TempSourceMetadata(File source) {
		this.source = source;
	}
	
	@Override
	public File getFilePointer() {
		File archiveOutputDirectory = this.getArchiveMeta().getArchiveOutputDirectory();
		
		/* if archive output specified copy file there and return that,
		 * else just return source */
		File targetFile = null;
		if(archiveOutputDirectory != null) {
			if(LOG.isDebugEnabled()) {
				LOG.debug("Copy source file '" +source.getName() + "' to target '" + archiveOutputDirectory.getAbsolutePath() + "'");
			}
			
			//copy the source to the target.
			String fileName = source.getName();
			String fullName = archiveOutputDirectory.getAbsolutePath() + File.separatorChar + fileName;
			targetFile = new File(fullName);
			
			try {
				FileUtils.copyFile(source, targetFile);
			} catch (IOException e) {
				LOG.error("Exception copying file.", e);
			}
		} else {
			if(LOG.isDebugEnabled()) {
				LOG.debug("No output directroy specified, returning source file: " + source);
			}
			
			targetFile = source;
		}
		
		return targetFile;
	}
}
