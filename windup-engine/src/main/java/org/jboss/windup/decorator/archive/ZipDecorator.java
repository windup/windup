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
package org.jboss.windup.decorator.archive;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.interrogator.Interrogator;
import org.jboss.windup.interrogator.util.KnownArchiveProfiler;
import org.jboss.windup.metadata.type.ZipEntryMetadata;
import org.jboss.windup.metadata.type.archive.ZipMetadata;
import org.jboss.windup.util.FatalWindupException;


public class ZipDecorator implements MetaDecorator<ZipMetadata> {

	private static final Log LOG = LogFactory.getLog(ZipDecorator.class);

	protected KnownArchiveProfiler knownArchiveProfiler;
	protected List<Interrogator<?>> interrogators = new ArrayList<Interrogator<?>>();
	protected List<Interrogator<?>> versionInterrogators = new ArrayList<Interrogator<?>>();

	public void setVersionInterrogators(List<Interrogator<?>> versionInterrogators) {
		this.versionInterrogators = versionInterrogators;
	}

	public void setInterrogators(List<Interrogator<?>> interrogators) {
		this.interrogators = interrogators;
	}

	public void setKnownArchiveProfiler(KnownArchiveProfiler knownArchiveProfiler) {
		this.knownArchiveProfiler = knownArchiveProfiler;
	}

	@Override
	public void processMeta(ZipMetadata archive) {
		try {

			ZipEntry entry;
			List<Interrogator<?>> loadedInterrogators = null;
			// first, check to see whether the archive is a known vendor archive. this will allow us to only process version information and skip all other files.
			if (knownArchiveProfiler.isExclusivelyKnownArchive(archive)) {
				// only check for version information.
				loadedInterrogators = versionInterrogators;
			}
			else {
				loadedInterrogators = interrogators;
			}

			// first, check all entries against the known vendors list.
			Enumeration<?> e = archive.getZipFile().entries();
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();

				// create the archive entry meta file.
				ZipEntryMetadata archiveEntry = new ZipEntryMetadata();
				archiveEntry.setArchiveMeta(archive);
				archiveEntry.setZipEntry(entry);

				for (Interrogator<?> interrogator : loadedInterrogators) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Entry: " + entry.toString() + " -> Interrogator: " + interrogator.getClass());
					}

					interrogator.processArchiveEntry(archiveEntry);
				}
			}
		}
		catch (Exception e) {
			if(e instanceof FatalWindupException) {
				throw (FatalWindupException)e;
			}
			
			LOG.error("Exception processing archive: " + archive.getName(), e);
		}
	}

}
