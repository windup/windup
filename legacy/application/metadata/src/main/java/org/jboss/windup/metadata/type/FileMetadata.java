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

import org.jboss.windup.metadata.type.archive.ArchiveMetadata;


/**
 * Keeps reference to a file, and collects meta data decorations from each Decorator.
 * 
 * @author bdavis
 * 
 */
public class FileMetadata extends ResourceMetadata {
	protected ArchiveMetadata archiveMeta;

	public void setArchiveMeta(ArchiveMetadata archiveMeta) {
		this.archiveMeta = archiveMeta;
	}

	public ArchiveMetadata getArchiveMeta() {
		return archiveMeta;
	}
	
	/**
	 * @return path relative to the parent archive
	 */
	public String getPathRelativeToArchive() {
		String relativePath = null;
		
		/* if archive has an output directory need to use that path to calculate relative path
		 * else use path to archive itself */
		File archiveOutputDir = this.getArchiveMeta().getArchiveOutputDirectory();
		File parentArchivieDir;
		if(archiveOutputDir != null) {
			parentArchivieDir = archiveOutputDir;
		} else {
			parentArchivieDir = this.getArchiveMeta().getFilePointer();
		}
		
		//get the relative path, +1 to remove preceding /
		relativePath = (this.getFilePointer().getPath()).substring(parentArchivieDir.getPath().length() + 1);
		
		return relativePath;
	}
}