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
package org.jboss.windup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.interrogator.DirectoryInterrogationEngine;
import org.jboss.windup.interrogator.FileInterrogationEngine;
import org.jboss.windup.interrogator.ZipInterrogationEngine;
import org.jboss.windup.resource.type.FileMeta;
import org.jboss.windup.resource.type.archive.ArchiveMeta;
import org.jboss.windup.resource.type.archive.DirectoryMeta;
import org.jboss.windup.util.LogController;
import org.jboss.windup.util.RPMToZipTransformer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>
 * Main entry point for generating Windup Meta information.
 * </p>
 */
public class WindupMetaEngine {
	private static final String[] RPM_EXTENSIONS = new String[] { ".rpm", ".rpm_" };
	private static final Log LOG = LogFactory.getLog(WindupMetaEngine.class);

	private ApplicationContext context;
	private List<String> supportedExtensions;

	private ZipInterrogationEngine interrogationEngine;
	private DirectoryInterrogationEngine directoryInterrogationEngine;
	private FileInterrogationEngine fileInterrogationEngine;

	private WindupEnvironment settings;

	public WindupMetaEngine(WindupEnvironment settings) {
		setupEnvironment(settings);
		this.settings = settings;

		// sets environment variables needed for Spring configuration.
		List<String> springContexts = new LinkedList<String>();

		springContexts.add("jboss-windup-context.xml");
		this.context = new ClassPathXmlApplicationContext(springContexts.toArray(new String[springContexts.size()]));

		interrogationEngine = (ZipInterrogationEngine) context.getBean("archive-interrogation-engine");
		directoryInterrogationEngine = (DirectoryInterrogationEngine) context.getBean("directory-interrogation-engine");
		fileInterrogationEngine = (FileInterrogationEngine) context.getBean("file-interrogation-engine");
		supportedExtensions = new ArrayList<String>((Collection<String>) context.getBean("zipExtensions"));
	}

	/**
	 * <p>
	 * Gets the meta information for a single given file.
	 * </p>
	 * 
	 * @param file
	 *            {@link File} to get meta information for
	 * 
	 * @return {@link FileMeta} for the given {@link File}
	 * 
	 * @throws IOException
	 *             this can happen when doing file operations
	 */
	public FileMeta getFileMeta(File file) throws IOException {
		FileMeta meta = null;

		if (!settings.isSource()) {
			throw new RuntimeException("Windup Engine must be set to source mode to process single files.");
		}

		if (!file.exists()) {
			throw new FileNotFoundException("File does not exist: " + file);
		}

		if (!file.isFile()) {
			throw new FileNotFoundException("Given path does not reference a file: " + file);
		}

		meta = fileInterrogationEngine.process(file);

		return meta;
	}

	/**
	 * <p>
	 * Gets the meta information for a given archive or directory of archives.
	 * </p>
	 * 
	 * @param input
	 *            archive or directory of archives to get the meta information
	 *            for
	 * 
	 * @return {@link ArchiveMeta} for the given archive or directory of
	 *         archives
	 * 
	 * @throws IOException
	 *             this can happen when doing file operations
	 */
	public ArchiveMeta getArchiveMeta(File input) throws IOException {
		return this.getArchiveMeta(input, null);
	}

	/**
	 * <p>
	 * Gets the meta information for a given archive or directory of archives
	 * saving the output to a given directory.
	 * </p>
	 * 
	 * @param input
	 *            Archive or directory of archives to get the meta information
	 *            for
	 * @param output
	 *            Optional. Directory to save any information generated while
	 *            generating the meta information
	 * 
	 * @return {@link ArchiveMeta} for the given archive or directory of
	 *         archives
	 * 
	 * @throws IOException
	 *             this can happen when doing file operations
	 */
	public ArchiveMeta getArchiveMeta(File input, File output) throws IOException {
		if (!input.exists()) {
			throw new FileNotFoundException("Nothing exists at the given path: " + input);
		}

		ArchiveMeta result = null;
		if (settings.isSource()) {
			// validate input and output.
			if (!input.exists() || !input.isDirectory()) {
				throw new IllegalArgumentException("Source input must be directory.");
			}
			result = processSourceDirectory(input, output);
		} else if (input.isDirectory()) {
			result = this.processDirectory(input, output);
		} else {
			result = this.processArchive(input, output, null);
		}

		return result;
	}

	/**
	 * <p>
	 * Generate {@link ArchiveMeta} by processing a directory of source files.
	 * </p>
	 * 
	 * @param sourceDir
	 *            directory of source files to get {@link ArchiveMeta} for
	 * @param outputDir
	 *            Optional. Directory to save any information generated while
	 *            generating the meta information
	 * 
	 * @return {@link ArchiveMeta} for the processed source directory
	 * 
	 * @throws IOException
	 *             This can happen when doing file operations
	 */
	private ArchiveMeta processSourceDirectory(File sourceDir, File outputDir) throws IOException {
		Validate.notNull(sourceDir, "Directory input path must be provided.");
		Validate.isTrue(sourceDir.isDirectory(), "Input must be a directory.");

		if (StringUtils.isNotBlank(settings.getLogLevel())) {
			LogController.setLogLevel(settings.getLogLevel());
		}

		ArchiveMeta result = directoryInterrogationEngine.process(outputDir, sourceDir);

		return result;
	}

	/**
	 * <p>
	 * Generate {@link ArchiveMeta} by processing a directory of archives.
	 * </p>
	 * 
	 * @param dirPath
	 *            Path to directory of archives to generate {@link ArchiveMeta}
	 *            for
	 * @param outputDir
	 *            Optional. Directory to save any information generated while
	 *            generating the meta information
	 * 
	 * @return {@link ArchiveMeta} generated from the processed archives in the
	 *         given directory
	 * 
	 * @throws IOException
	 *             This can happen when performing file operations
	 */
	private ArchiveMeta processDirectory(File dirPath, File outputDir) throws IOException {
		LOG.info("Processing directory: " + dirPath.getAbsolutePath());
		List<File> archives = new LinkedList<File>(Arrays.asList(dirPath.listFiles((FilenameFilter) new SuffixFileFilter(supportedExtensions))));

		File[] rpms = dirPath.listFiles((FilenameFilter) new SuffixFileFilter(RPM_EXTENSIONS));
		if (rpms.length > 0) {
			for (File rpm : rpms) {
				try {
					File zip = RPMToZipTransformer.convertRpmToZip(rpm);
					archives.add(zip);
				} catch (Exception e) {
					LOG.warn("Conversion of RPM: " + rpm.getAbsolutePath() + " to ZIP failed.");
				}

			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.info("Found " + archives.size() + " Archives");
		}

		// create a parent meta to add all the sub archive metas too
		DirectoryMeta parentDirMeta = new DirectoryMeta();
		parentDirMeta.setFilePointer(dirPath);
		parentDirMeta.setRelativePath("/");

		// process each child meta
		for (File archive : archives) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing archive: " + archive.getAbsolutePath());
			}

			if (LOG.isInfoEnabled()) {
				LOG.info("ArchiveMeta Path: " + archive.getAbsolutePath());
			}

			this.processArchive(archive, outputDir, parentDirMeta);
		}

		return parentDirMeta;
	}

	/**
	 * <p>
	 * Generate {@link ArchiveMeta} by processing a single archive.
	 * </p>
	 * 
	 * @param archive
	 *            Archive to process
	 * @param outputDir
	 *            Optional. Directory to save any information generated while
	 *            generating the meta information
	 * @param parentMeta
	 *            Optional. Parent meta for the {@link ArchiveMeta} generated by
	 *            processing the given archive
	 * 
	 * @return {@link ArchiveMeta} generated from processing the given archive.
	 *         Optionally has a parent of the the optionally given parent meta.
	 * 
	 * @throws IOException
	 *             This can happen when performing file operations
	 */
	private ArchiveMeta processArchive(File archive, File outputDir, ArchiveMeta parentMeta) throws IOException {
		ArchiveMeta result = null;

		if (!archive.exists()) {
			throw new FileNotFoundException("ArchiveMeta not found.");
		}

		for (String ext : RPM_EXTENSIONS) {
			if (StringUtils.endsWithIgnoreCase(archive.getAbsolutePath(), ext)) {
				try {
					archive = RPMToZipTransformer.convertRpmToZip(archive);
				} catch (Exception e) {
					LOG.error("Exception converting RPM: " + archive.getName() + " to ZIP.", e);
					return result;
				}
			}
		}

		if (StringUtils.isNotBlank(settings.getLogLevel())) {
			LogController.setLogLevel(settings.getLogLevel());
		}

		result = interrogationEngine.process(outputDir, archive, parentMeta);

		return result;
	}

	/**
	 * <p>
	 * Sets up the Java System properties based on the given
	 * {@link WindupEnvironment}.
	 * </p>
	 * 
	 * @param settings
	 *            {@link WindupEnvironment} used to setup the Java System
	 *            properties
	 */
	private static void setupEnvironment(WindupEnvironment settings) {
		// validate settings...
		if (StringUtils.isNotBlank(settings.getPackageSignature())) {
			System.setProperty("package.signature", settings.getPackageSignature());
		}
		else {
			LOG.warn("WARNING: Consider specifying javaPkgs.  Otherwise, the Java code will not be inspected.");
		}
		
		if (StringUtils.isNotBlank(settings.getExcludeSignature())) {
			System.setProperty("exclude.signature", settings.getExcludeSignature());
		} 

		if (StringUtils.isNotBlank(settings.getTargetPlatform())) {
			System.setProperty("target.platform", settings.getTargetPlatform());
		}

		if (StringUtils.isNotBlank(settings.getFetchRemote())) {
			System.setProperty("fetch.remote", settings.getFetchRemote());
		} else {
			LOG.warn("INFO: Will not try and fetch remote versions for unknown JARs.  Consider using: '-fetchRemote true' command line for more detailed reporting.  Requires internet connection.");
			System.setProperty("fetch.remote", "false");
		}
	}
}