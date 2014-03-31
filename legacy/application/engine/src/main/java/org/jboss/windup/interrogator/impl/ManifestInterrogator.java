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
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.decoration.effort.StoryPointEffort;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.ManifestMetadata;
import org.jboss.windup.metadata.type.ZipEntryMetadata;


public class ManifestInterrogator extends ExtensionInterrogator<ManifestMetadata> {
	private static final Log LOG = LogFactory.getLog(ManifestInterrogator.class);

	@Override
	public ManifestMetadata archiveEntryToMeta(ZipEntryMetadata archiveEntry) {
		File file = archiveEntry.getFilePointer();

		LOG.debug("Processing Manifest: " + file.getAbsolutePath().toString());

		if (file.length() > 1048576L * 1) {
			LOG.warn("Manifest larger than 1 MB: " + file.getAbsolutePath() + "; Skipping processing.");
			return null;
		}
		
		ManifestMetadata meta = new ManifestMetadata();
		meta.setArchiveMeta(archiveEntry.getArchiveMeta());
		meta.setFilePointer(file);

		Classification classification = new Classification();
		StoryPointEffort he = new StoryPointEffort();
		he.setHours(0);
		classification.setEffort(he);
		classification.setDescription("ArchiveMetadata Manifest");
		meta.getDecorations().add(classification);

		return meta;
	}

	@Override
	public ManifestMetadata fileEntryToMeta(FileMetadata entry) {
		File file = entry.getFilePointer();

		LOG.debug("Processing Manifest: " + file.getAbsolutePath().toString());

		if (file.length() > 1048576L * 1) {
			LOG.warn("Manifest larger than 1 MB: " + file.getAbsolutePath() + "; Skipping processing.");
			return null;
		}
		
		ManifestMetadata meta = new ManifestMetadata();
		//meta.setArchiveMeta(archiveEntry.getArchiveMeta());
		meta.setFilePointer(file);
		meta.setArchiveMeta(entry.getArchiveMeta());
		
		Classification classification = new Classification();
		StoryPointEffort he = new StoryPointEffort();
		he.setHours(0);
		classification.setEffort(he);
		classification.setDescription("ArchiveMetadata Manifest");
		meta.getDecorations().add(classification);

		return meta;
	}
}
