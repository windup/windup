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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.type.archive.ZipMetadata;

public class ZipEntryMetadata extends FileMetadata {
	private static final int BUFFER = 2048;
	private static final Log LOG = LogFactory.getLog(ZipEntryMetadata.class);
	
	private ZipEntry zipEntry;

	@Override
	public File getFilePointer() {
		if(!(this.getArchiveMeta() instanceof ZipMetadata)) {
			throw new IllegalStateException("ZipEntryMetadata must be child of Zip.");
		}
		
		ZipMetadata parent = (ZipMetadata)this.getArchiveMeta();
		
		if (this.filePointer == null) {
			try {
				File archiveOutputDirectory = this.getArchiveMeta().getArchiveOutputDirectory();
				this.filePointer = unzipEntry(zipEntry, parent.getZipFile(), archiveOutputDirectory);
			}
			catch (Exception e) {
				LOG.error("Exception unzipping entry: " + this.archiveMeta.getFilePointer().getAbsolutePath(), e);
			}
		}

		return this.filePointer;
	}

	public ZipEntry getZipEntry() {
		return zipEntry;
	}

	public void setZipEntry(ZipEntry zipEntry) {
		this.filePointer = null; // unset so we can rehydrate.
		this.zipEntry = zipEntry;
	}

	/**
	 * Unzips a given ZipEntry to a base output direction.
	 * 
	 * @param entry
	 *            ZipEntry to unzip
	 * @param zipfile
	 *            ZipFile to extract ZipEntry from
	 * @param archiveOutputDirectory
	 *            base directory to extract ZipEntry.
	 * @return Unzipped file.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected File unzipEntry(ZipEntry entry, ZipFile zipfile, File archiveOutputDirectory) throws IOException, FileNotFoundException {
		BufferedOutputStream dest;
		BufferedInputStream is;
		String pathOutput = null;
		if (StringUtils.contains(entry.toString(), "/")) {
			pathOutput = StringUtils.substringBeforeLast(entry.toString(), "/");
		}
		else {
			pathOutput = File.separator;
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("PathOutput: " + pathOutput);
			LOG.trace("PathEntry: " + entry);
			LOG.trace("ArchiveOutput: " + archiveOutputDirectory.getAbsolutePath());
		}

		File entryPathOutput = new File(archiveOutputDirectory.getAbsolutePath() + File.separator + pathOutput);
		File entryOutput = new File(archiveOutputDirectory.getAbsolutePath() + File.separator + entry);

		if (!entryOutput.exists()) {
			FileUtils.forceMkdir(entryPathOutput);
			is = new BufferedInputStream(zipfile.getInputStream(entry));
			LOG.debug("Unzipping: " + entryOutput.getAbsolutePath());

			int count;
			byte data[] = new byte[BUFFER];
			FileOutputStream fos = new FileOutputStream(entryOutput);
			dest = new BufferedOutputStream(fos, BUFFER);

			while ((count = is.read(data, 0, BUFFER)) != -1) {
				dest.write(data, 0, count);
			}

			dest.flush();
			dest.close();
			is.close();
		}

		return entryOutput;
	}
}
