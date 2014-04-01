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
package org.jboss.windup.interrogator;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.archive.DirectoryMetadata;

public class FileInterrogationEngine {
	private static final Log LOG = LogFactory.getLog(FileInterrogationEngine.class);
	protected List<Interrogator> interrogators;
	
	public void setInterrogators(List<Interrogator> interrogators) {
		this.interrogators = interrogators;
	}
	
	public FileMetadata process(File targetFile) {
		FileMetadata tempMeta = new FileMetadata();
		tempMeta.setFilePointer(targetFile);
		
		DirectoryMetadata dirMeta = new DirectoryMetadata();
		dirMeta.setRelativePath(targetFile.getParentFile().getPath());
		tempMeta.setArchiveMeta(dirMeta);
		
		//run the interrogators
		if(targetFile.isFile()) {
			LOG.debug("Processing file: "+targetFile.getAbsolutePath());
			
			for(Interrogator<?> interrogator : interrogators) {
				interrogator.processFile(tempMeta);
			}
		}
		
		/* only one of the interrogators will match resulting in a single entry on the archive
		 * this bit of code finds that single entry and returns that as the result of processing
		 * the file.
		 * 
		 * WARNING:
		 * If the assumption that only one interrogator will ever match ever changes this bit
		 * of code will break */
		FileMetadata result = null;
		for(FileMetadata archiveEntryMeta : tempMeta.getArchiveMeta().getEntries()) {
			if(archiveEntryMeta.getFilePointer().equals(tempMeta.getFilePointer())) {
				result = archiveEntryMeta;
				break;
			}
		}

		return result;
	}
}
