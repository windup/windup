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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.resource.type.TemporarySourceFile;
import org.jboss.windup.resource.type.archive.ArchiveMeta;
import org.jboss.windup.resource.type.archive.DirectoryMeta;
import org.jboss.windup.util.RecursiveDirectoryMetaFactory;

public class DirectoryInterrogationEngine {
	private static final Log LOG = LogFactory.getLog(DirectoryInterrogationEngine.class);
	protected List<Interrogator> interrogators;
	
	public void setInterrogators(List<Interrogator> interrogators) {
		this.interrogators = interrogators;
	}
	
	public DirectoryMeta process(File outputDirectory, File targetDirectory) {
		//this will recurse all files from this directory; all but hidden directories.
		
		RecursiveDirectoryMetaFactory rdmf = new RecursiveDirectoryMetaFactory(targetDirectory);
		List<DirectoryMeta> directories = new LinkedList<DirectoryMeta>();
		
		DirectoryMeta root = rdmf.recursivelyExtract();
		unfoldRecursion(root, directories);


		int i = 1;
		int j = directories.size();
		
		for(DirectoryMeta directoryMeta : directories) {
			LOG.info("Interrogating (" + i + " of " + j + "): " + directoryMeta.getRelativePath());
			Collection<File> files = FileUtils.listFiles(directoryMeta.getFilePointer(), TrueFileFilter.INSTANCE, null);
			
			if(LOG.isDebugEnabled()) {
				LOG.debug("  Processing "+files.size()+" files within directory.");
			}
			
			if(outputDirectory != null) {
				String dirOutput = FilenameUtils.normalize(
						FilenameUtils.separatorsToSystem(outputDirectory.getAbsolutePath() + File.separatorChar + directoryMeta.getRelativePath()));
				
				directoryMeta.setArchiveOutputDirectory(new File(dirOutput));
			}
			for(Interrogator<?> interrogator : interrogators) {
				for(File file : files) {
					if(file.isFile()) {
						LOG.debug("Processing file: "+file.getAbsolutePath());
						TemporarySourceFile fileMeta = new TemporarySourceFile(file);
						fileMeta.setArchiveMeta(directoryMeta);
						LOG.debug("Set archive as: "+directoryMeta);
						interrogator.processFile(fileMeta);
					}
				}
			}
			i++;
		}
		return root;
	}
	
	protected void unfoldRecursion(DirectoryMeta base, Collection<DirectoryMeta> archiveMetas) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Directory: "+base.getName()+" Children: "+base.getNestedArchives().size()+" Path: "+base.getFilePointer().getAbsolutePath());
		}
		for (ArchiveMeta meta : base.getNestedArchives()) {
			DirectoryMeta child = (DirectoryMeta)meta;
			
			unfoldRecursion(child, archiveMetas);
		}
		archiveMetas.add(base);
	}
	
}
