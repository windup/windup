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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.interrogator.DirectoryInterrogationEngine;
import org.jboss.windup.interrogator.FileInterrogationEngine;
import org.jboss.windup.interrogator.ZipInterrogationEngine;
import org.jboss.windup.reporting.ReportEngine;
import org.jboss.windup.reporting.Reporter;
import org.jboss.windup.resource.type.FileMeta;
import org.jboss.windup.resource.type.archive.ArchiveMeta;
import org.jboss.windup.util.LogController;
import org.jboss.windup.util.RPMToZipTransformer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class WindupEngine {
	private static final String[] RPM_EXTENSIONS = new String[] { ".rpm", ".rpm_" };
	private static final Log LOG = LogFactory.getLog(WindupEngine.class);

	private ApplicationContext context;
	private List<String> supportedExtensions;

	private ZipInterrogationEngine interrogationEngine;
	private DirectoryInterrogationEngine directoryInterrogationEngine;
	private FileInterrogationEngine fileInterrogationEngine;
	
	private Reporter reportEngine;
	private WindupEnvironment settings;

	public WindupEngine(WindupEnvironment settings) {
		setupEnvironment(settings);
		this.settings = settings;

		// sets environment variables needed for Spring configuration.
		List<String> springContexts = new LinkedList<String>();

		springContexts.add("jboss-windup-context.xml");
		this.context = new ClassPathXmlApplicationContext(springContexts.toArray(new String[springContexts.size()]));

		
		interrogationEngine = (ZipInterrogationEngine) context.getBean("archive-interrogation-engine");
		directoryInterrogationEngine = (DirectoryInterrogationEngine) context.getBean("directory-interrogation-engine");
		fileInterrogationEngine = (FileInterrogationEngine) context.getBean("file-interrogation-engine");
		reportEngine = (ReportEngine) context.getBean("report-engine");
		supportedExtensions = new ArrayList((Collection<String>) context.getBean("zipExtensions"));
	}

	private void setupEnvironment(WindupEnvironment settings) {
		// validate settings...
		if (StringUtils.isNotBlank(settings.getPackageSignature())) {
			System.setProperty("package.signature", settings.getPackageSignature());
		}
		if (StringUtils.isNotBlank(settings.getExcludeSignature())) {
			System.setProperty("exclude.signature", settings.getExcludeSignature());
		}
		else {
			LOG.warn("WARNING: Consider specifying javaPkgs.  Otherwise, the Java code will not be inspected.");
		}

		if (StringUtils.isNotBlank(settings.getTargetPlatform())) {
			System.setProperty("target.platform", settings.getTargetPlatform());
		}

		if (StringUtils.isNotBlank(settings.getFetchRemote())) {
			System.setProperty("fetch.remote", settings.getFetchRemote());
		}
		else {
			LOG.warn("INFO: Will not try and fetch remote versions for unknown JARs.  Consider using: '-fetchRemote true' command line for more detailed reporting.  Requires internet connection.");
			System.setProperty("fetch.remote", "false");
		}

	}
	
	/**
	 * <p>
	 * Process a single file.
	 * </p>
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	public FileMeta processFile(File file) throws IOException {
		FileMeta meta = null;
		
		if(!settings.isSource()) {
			throw new RuntimeException("Windup Engine must be set to source mode to process single files.");
		}
		
		if (!file.exists()) {
			throw new FileNotFoundException("file does not exist: " + file);
		}
		
		if(!file.isFile()) {
			throw new FileNotFoundException("given file path does not reference a file: " + file.getAbsolutePath());
		}
		
		meta = fileInterrogationEngine.process(file);
		
		return meta;
	}
	
	/**
	 * <p>
	 * Process a single file.
	 * </p>
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	public FileMeta processFile(String filePath) throws IOException {
		return processFile(new File(filePath));
	}

	public void process(File inputLocation, File outputLocation) throws IOException {
		if (!inputLocation.exists()) {
			throw new FileNotFoundException("ArchiveMeta not found: " + inputLocation);
		}
		
		if(settings.isSource()) {
			//validate input and output.
			if(!inputLocation.exists() || !inputLocation.isDirectory())
			{
				throw new IllegalArgumentException("Source input must be directory.");
			}			
			processSourceDirectory(inputLocation, outputLocation);
		}
		
		
		if (inputLocation.isDirectory()) {
			// must be batch mode!
			if (outputLocation != null) {
				LOG.warn("Ignoring output parameter as input is directory.");
			}
			processDirectory(inputLocation);
		}
		else {
			// single archive processing.
			if (outputLocation == null) {
				processArchive(inputLocation);
			}
			else {
				processArchive(inputLocation, outputLocation);
			}
		}
	}
	
	public ArchiveMeta processSourceDirectory(File dirPath, File outputPath) throws IOException {
		Validate.notNull(dirPath, "Directory input path must be provided.");
		Validate.isTrue(dirPath.isDirectory(), "Input must be a directory.");
		
		File logFile = null;
		
		if(outputPath == null) {
			String outputName = dirPath.getName() +"-doc";
			String outputPathLoc = dirPath.getParentFile().getAbsolutePath();
			outputPathLoc = outputPathLoc + File.separator + outputName;
			
			//create output path...
			outputPath = new File(outputPathLoc);
			LOG.info("Creating output path: "+outputPath.getAbsolutePath());
			LOG.info("  - To overwrite this in the future, use the -output parameter.");
		}
		
		if (!outputPath.exists()) {
			FileUtils.forceMkdir(outputPath);
		}
		
		if (StringUtils.isNotBlank(settings.getLogLevel())) {
			LogController.setLogLevel(settings.getLogLevel());
		}

		
		
		try {
			if (settings.isCaptureLog()) {
				logFile = new File(outputPath.getAbsolutePath() + File.separator + "windup.log");
				LogController.addFileAppender(logFile);
			}
			ArchiveMeta meta = directoryInterrogationEngine.process(outputPath, dirPath);
			reportEngine.process(meta, outputPath);
			
			return meta;
		}
		finally {
			if (logFile != null && logFile.exists()) {
				LogController.removeFileAppender(logFile);
			}
		}
	}

	public Collection<ArchiveMeta> processDirectory(File dirPath) throws IOException {
		LOG.info("Processing directory: " + dirPath.getAbsolutePath());
		List<File> archives = new LinkedList<File>(Arrays.asList(dirPath.listFiles((FilenameFilter) new SuffixFileFilter(supportedExtensions))));

		File[] rpms = dirPath.listFiles((FilenameFilter) new SuffixFileFilter(RPM_EXTENSIONS));
		if (rpms.length > 0) {
			for (File rpm : rpms) {
				try {
					File zip = RPMToZipTransformer.convertRpmToZip(rpm);
					archives.add(zip);
				}
				catch (Exception e) {
					LOG.warn("Conversion of RPM: " + rpm.getAbsolutePath() + " to ZIP failed.");
				}

			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.info("Found " + archives.size() + " Archives");
		}

		List<ArchiveMeta> archiveMetas = new LinkedList<ArchiveMeta>();
		for (File archive : archives) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing archive: " + archive.getAbsolutePath());
			}
			LOG.info("ArchiveMeta Path: " + archive.getAbsolutePath());
			archiveMetas.add(processArchive(archive));
		}
		
		return archiveMetas;
	}

	public ArchiveMeta processArchive(File archivePath) throws IOException {
		String outputLoc = StringUtils.substringBeforeLast(archivePath.getAbsolutePath(), ".");
		outputLoc += "-" + StringUtils.substringAfterLast(archivePath.getAbsolutePath(), ".") + "-doc";

		if (LOG.isDebugEnabled()) {
			LOG.debug("Setting output location based on input: " + outputLoc);
		}

		return processArchive(archivePath, new File(outputLoc));
	}

	public ArchiveMeta processArchive(File archivePath, File outputPath) throws IOException {
		if (!archivePath.exists()) {
			throw new FileNotFoundException("ArchiveMeta not found.");
		}

		for (String ext : RPM_EXTENSIONS) {
			if (StringUtils.endsWithIgnoreCase(archivePath.getAbsolutePath(), ext)) {
				try {
					archivePath = RPMToZipTransformer.convertRpmToZip(archivePath);
				}
				catch (Exception e) {
					LOG.error("Exception converting RPM: " + archivePath.getName() + " to ZIP.", e);
					return null;
				}
			}
		}

		File logFile = null;
		if (!outputPath.exists()) {
			FileUtils.forceMkdir(outputPath);
		}

		if (StringUtils.isNotBlank(settings.getLogLevel())) {
			LogController.setLogLevel(settings.getLogLevel());
		}

		try {
			if (settings.isCaptureLog()) {
				logFile = new File(outputPath.getAbsolutePath() + File.separator + "windup.log");
				LogController.addFileAppender(logFile);
			} 
			ArchiveMeta meta = interrogationEngine.process(outputPath, archivePath);
			reportEngine.process(meta, outputPath);
			
			return meta;
		}
		finally {
			if (logFile != null && logFile.exists()) {
				LogController.removeFileAppender(logFile);
			}
		}
	}
}