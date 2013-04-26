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
import org.jboss.windup.resource.decoration.Classification;
import org.jboss.windup.resource.decoration.effort.StoryPointEffort;
import org.jboss.windup.resource.type.FileMeta;
import org.jboss.windup.resource.type.ManifestMeta;
import org.jboss.windup.resource.type.ZipEntryMeta;


public class ManifestInterrogator extends ExtensionInterrogator<ManifestMeta> {
	private static final Log LOG = LogFactory.getLog(ManifestInterrogator.class);

	@Override
	public ManifestMeta archiveEntryToMeta(ZipEntryMeta archiveEntry) {
		File file = archiveEntry.getFilePointer();

		LOG.debug("Processing Manifest: " + file.getAbsolutePath().toString());

		if (file.length() > 1048576L * 1) {
			LOG.warn("Manifest larger than 1 MB: " + file.getAbsolutePath() + "; Skipping processing.");
			return null;
		}
		
		ManifestMeta meta = new ManifestMeta();
		meta.setArchiveMeta(archiveEntry.getArchiveMeta());
		meta.setFilePointer(file);

		Classification classification = new Classification();
		StoryPointEffort he = new StoryPointEffort();
		he.setHours(0);
		classification.setEffort(he);
		classification.setDescription("ArchiveMeta Manifest");
		meta.getDecorations().add(classification);

		return meta;
	}

	@Override
	public ManifestMeta fileEntryToMeta(FileMeta entry) {
		File file = entry.getFilePointer();

		LOG.debug("Processing Manifest: " + file.getAbsolutePath().toString());

		if (file.length() > 1048576L * 1) {
			LOG.warn("Manifest larger than 1 MB: " + file.getAbsolutePath() + "; Skipping processing.");
			return null;
		}
		
		ManifestMeta meta = new ManifestMeta();
		//meta.setArchiveMeta(archiveEntry.getArchiveMeta());
		meta.setFilePointer(file);
		meta.setArchiveMeta(entry.getArchiveMeta());
		
		Classification classification = new Classification();
		StoryPointEffort he = new StoryPointEffort();
		he.setHours(0);
		classification.setEffort(he);
		classification.setDescription("ArchiveMeta Manifest");
		meta.getDecorations().add(classification);

		return meta;
	}
}
