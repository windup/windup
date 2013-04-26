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
package org.jboss.windup.resource.type;

import java.io.File;

import org.apache.log4j.helpers.RelativeTimeDateFormat;
import org.jboss.windup.resource.type.archive.ArchiveMeta;


/**
 * Keeps reference to a file, and collects meta data decorations from each Decorator.
 * 
 * @author bdavis
 * 
 */
public class FileMeta extends ResourceMeta {
	protected ArchiveMeta archiveMeta;

	public void setArchiveMeta(ArchiveMeta archiveMeta) {
		this.archiveMeta = archiveMeta;
	}

	public ArchiveMeta getArchiveMeta() {
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