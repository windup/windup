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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.interrogator.Interrogator;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.ZipEntryMetadata;


/**
 * Generic extension interrogator. Expects to create a report from the resource; therefore, it is
 * to be used with a text based file, such as property files.
 * 
 * @author bdavis
 * 
 */
public abstract class ExtensionInterrogator<T extends FileMetadata> extends Interrogator<T> {
	private static final Log LOG = LogFactory.getLog(ExtensionInterrogator.class);

	protected Set<Pattern> extensions;

	public void setExtensions(Set<String> extensions) {
		this.extensions = compilePatternSet(extensions);
	}

	@Override
	public void processMeta(T fileMeta) {
		File file = fileMeta.getFilePointer();
		if (LOG.isTraceEnabled()) {
			LOG.trace("Processing Extension: " + file.getAbsolutePath());
		}
		decoratorPipeline.processMeta(fileMeta);

		if (isOfInterest(fileMeta)) {
			// add it to it's parent's results.
			fileMeta.getArchiveMeta().getEntries().add(fileMeta);
		}
	}

	@Override
	public void processArchiveEntry(ZipEntryMetadata archiveEntry) {
		String entryName = archiveEntry.getZipEntry().getName();
		
		if(matchesExtension(entryName)) {
			T meta = archiveEntryToMeta(archiveEntry);

			if (meta != null) {
				processMeta(meta);
				return;
			}
		}
	}
	
 
	@Override
	public void processFile(FileMetadata entry) {
		String path = StringUtils.replace(entry.getFilePointer().getAbsolutePath(), "\\", "/");
		if(matchesExtension(path)) {
			T meta = fileEntryToMeta(entry);
			
			if (meta != null) {
				processMeta(meta);
				return;
			}
		}
	} 
	
	public boolean isOfInterest(T fileMeta) {
		// if we found a blacklist, it is of interest.
		return fileMeta.getDecorations().size() > 0;
	}
	
	protected boolean matchesExtension(String path) {
		for (Pattern extensionPattern : extensions) {
			if (extensionPattern.matcher(path).find()) {
				LOG.debug("Matched on extension: "+extensionPattern.pattern());
				return true;
			}
		}
		
		return false;
	}

	protected Set<Pattern> compilePatternSet(Set<String> patternStringSet) {
		if (patternStringSet == null)
			return null;

		Set<Pattern> target = new HashSet<Pattern>(patternStringSet.size());
		for (String patternString : patternStringSet) {
			if (!StringUtils.endsWith(patternString, "$")) {
				patternString = patternString + "$";
			}
			target.add(Pattern.compile(patternString));
		}
		return target;
	}
}
