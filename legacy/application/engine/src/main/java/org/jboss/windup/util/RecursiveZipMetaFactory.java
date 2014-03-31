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
package org.jboss.windup.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.type.archive.ZipMetadata;


/**
 *  Custom implementation of zip archive extraction.
 */
public class RecursiveZipMetaFactory {
	private static final Log LOG = LogFactory.getLog(RecursiveZipMetaFactory.class);

	private static final int BUFFER = 2048;

    /** The directory to extract to. */
	private File startLocation;

	public RecursiveZipMetaFactory(File startLocation, Set<String> extensions) {
		UUID uuidKey = UUID.randomUUID();
		safeExtractKey = "_" + StringUtils.substring(StringUtils.remove(uuidKey.toString(), "-"), 0, 6);

		this.startLocation = new File(startLocation.getAbsolutePath() + File.separator + "jboss_windup" + safeExtractKey);
		this.kae = extensions;
	}

	private final String safeExtractKey;
	private Set<String> kae = new HashSet<String>();

	public void releaseTempFiles() {
		LOG.debug("Cleaning up: " + this.startLocation.getAbsolutePath());
		FileUtils.deleteQuietly(this.startLocation);
	}

	public ZipMetadata recursivelyExtract(ZipFile zip) {

		if (LOG.isDebugEnabled()) {
			LOG.debug(zip.getName() + ": " + this.startLocation.getAbsolutePath());
		}
		ZipMetadata archive = generateArchive(null, new File(zip.getName()));
		recursivelyExtract(archive, zip, this.startLocation);

		return archive;
	}

	protected void recursivelyExtract(ZipMetadata parent, ZipFile zip, File outputDirectory) {
		String fileName = StringUtils.substringAfterLast(zip.getName(), File.separator);
		File subOutputDir = new File(outputDirectory.getAbsolutePath() + File.separator + fileName);
		ZipEntry entry;

		Enumeration<?> e = zip.entries();
		while (e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			// must be a candidate file.
			if (!entry.isDirectory()) {
				if (archiveEndInEntryOfInterest(entry.getName())) {
					try {
						File extracted = unzipEntry(parent, entry, zip, subOutputDir);
						ZipFile zf = new ZipFile(extracted);

						// we should know it is a valid zip here..
						ZipMetadata arch = generateArchive(parent, extracted);
						LOG.info("Prepared ZipMetadata: " + arch.getRelativePath());
						recursivelyExtract(arch, zf, new File(StringUtils.substringBeforeLast(zf.getName(), File.separator)));
					}
					catch (FileNotFoundException e1) {
						LOG.warn("Skipping invalid zip entry: " + entry);
					}
					catch (IOException e1) {
						LOG.warn("Skipping invalid zip entry: " + entry);
					}
				}
			}
		}
		try {
			zip.close();
		}
		catch (IOException e1) {
			LOG.error("Exception closing zip.", e1);
		}
	}

	protected File unzipEntry(ZipMetadata parent, ZipEntry entry, ZipFile zipfile, File archiveOutputDirectory) {
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		String pathOutput = null;
		if (StringUtils.contains(entry.toString(), "/")) {
			pathOutput = StringUtils.substringBeforeLast(entry.toString(), "/");
		}
		else {
			pathOutput = File.separator;
		}
		File entryPathOutput = new File(archiveOutputDirectory.getAbsolutePath() + safeExtractKey + File.separator + pathOutput);
		File entryOutput = new File(archiveOutputDirectory.getAbsolutePath() + safeExtractKey + File.separator + entry);
		if (!entryOutput.exists()) {
			try {
				FileUtils.forceMkdir(entryPathOutput);
				is = new BufferedInputStream(zipfile.getInputStream(entry));
				if (LOG.isDebugEnabled()) {
					LOG.debug("Unzipping: " + entryOutput.getAbsolutePath());
				}

				int count;
				byte data[] = new byte[BUFFER];
				FileOutputStream fos = new FileOutputStream(entryOutput);
				dest = new BufferedOutputStream(fos, BUFFER);

				while ((count = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();

				return entryOutput;
			}
			catch (IOException e) {
				LOG.error(e);
			}
			finally {
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(dest);
			}
		}
		else {
			LOG.warn("Entry: " + entry.toString() + " is a duplicate.  Returning the first entry.");
			return entryOutput;
		}

		return null;

	}

	private boolean archiveEndInEntryOfInterest(String entryName) {
		for (String extension : kae) {
			if (StringUtils.endsWith(entryName, extension)) {
				return true;
			}
		}
		return false;
	}

	private ZipMetadata generateArchive(ZipMetadata parent, File entryOutput) {
		String relativePath = StringUtils.removeStart(entryOutput.getAbsolutePath(), this.startLocation.getAbsolutePath().toString());

		if (LOG.isTraceEnabled()) {
			LOG.trace("RE Relative Path: " + relativePath);
			LOG.trace("SafeKey: " + safeExtractKey);
		}

		relativePath = StringUtils.replace(relativePath, "\\", "/");
		relativePath = StringUtils.removeStart(relativePath, "/");

		if (StringUtils.contains(relativePath, this.safeExtractKey)) {
			// all subarchives of the target archive will get copied to a location with the safeExtractKey.
			relativePath = StringUtils.remove(relativePath, this.safeExtractKey);
		}
		else {
			// otherwise, we know it is the original file...
			relativePath = StringUtils.substringAfterLast(relativePath, "/");
		}

		String archiveName = relativePath;
		if (StringUtils.contains(archiveName, "/")) {
			archiveName = StringUtils.substringAfterLast(relativePath, "/");
		}

		ZipMetadata archive = new ZipMetadata();
		archive.setName(archiveName);
		archive.setFilePointer(entryOutput);
		archive.setRelativePath(relativePath);

		if (parent != null) {
			parent.getNestedArchives().add(archive);
		}
		
		archive.setArchiveMeta(parent);

		if (LOG.isTraceEnabled()) {
			LOG.trace("Created archive: " + archive.toString());
		}
		return archive;
	}
}