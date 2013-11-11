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
package org.jboss.windup.reporting;

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
import org.jboss.windup.WindupEngine;
import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.util.RPMToZipTransformer;
import org.springframework.context.ApplicationContext;


public class ReportEngine {
	private static final String[] RPM_EXTENSIONS = new String[] { ".rpm", ".rpm_" };
	private static final Log LOG = LogFactory.getLog(ReportEngine.class);

	private final Collection<Reporter> reporters;
	private final ApplicationContext context;
	private final WindupEngine windupEngine;
	private final WindupEnvironment settings;
	private final List<String> supportedExtensions;
	
	/**
	 * <p>
	 * Creates a new {@link ReportEngine} using the given
	 * {@link WindupEnvironment}. This new {@link ReportEngine} will use a newly
	 * created {@link WindupEngine}.
	 * </p>
	 * 
	 * @param settings
	 *            Windup settings to use for this {@link ReportEngine}
	 */
	public ReportEngine(WindupEnvironment settings) {
		this(settings, new WindupEngine(settings));
	}
	
	/**
	 * <p>
	 * Creates a {@link ReportEngine} using an already existing
	 * {@link WindupEngine}.
	 * </p>
	 * 
	 * <p>
	 * <b>IMPORTANT: keep in mind that {@link WindupEngine} is not inherently
	 * thread safe, so if you are using it for more then one task be sure to
	 * manage this risk yourself.
	 * </p>
	 * 
	 * @param settings
	 *            Windup settings to use for this {@link ReportEngine}
	 * @param engine
	 *            Windup engine to use for this {@link ReportEngine}
	 */
	public ReportEngine(WindupEnvironment settings, WindupEngine engine) {
		this.settings = settings;
		windupEngine = engine;
		context = windupEngine.getContext();
		reporters = (Collection<Reporter>)context.getBean("reporters");
		supportedExtensions = new ArrayList((Collection<String>) context.getBean("zipExtensions"));
	}
	
	/**
	 * Main processing method. 
	 * @param inputLocation
	 * @param outputLocation
	 * @throws IOException
	 */
	public void process(File inputLocation, File outputLocation) throws IOException {
		if (!inputLocation.exists()) {
			throw new FileNotFoundException("ArchiveMetadata not found: " + inputLocation);
		}
		
		if(settings.isSource()) {
			//validate input and output.
			if(!inputLocation.exists() || !inputLocation.isDirectory())
			{
				throw new IllegalArgumentException("Source input must be directory.");
			}			
			
			if(outputLocation == null) {
				String outputName = inputLocation.getName() +"-doc";
				String outputPathLoc = inputLocation.getParentFile().getAbsolutePath();
				outputPathLoc = outputPathLoc + File.separator + outputName;
				
				//create output path...
				outputLocation = new File(outputPathLoc);
				LOG.info("Creating output path: "+outputLocation.getAbsolutePath());
				LOG.info("  - To overwrite this in the future, use the -output parameter.");
			}
			
			if (!outputLocation.exists()) {
				FileUtils.forceMkdir(outputLocation);
			}
			
			ArchiveMetadata am = windupEngine.processSourceDirectory(inputLocation, outputLocation);
			generateReport(am, outputLocation);
		}
		//if this isn't a source run, then we should run it as archive mode.
		else {
			if (inputLocation.isDirectory()) {
				batchInputDirectory(inputLocation);
			}
			else {
				// single archive processing.
				if (outputLocation == null) {
					//generate output based on input.
					outputLocation = generateArchiveOutputLocation(inputLocation);
					ArchiveMetadata amd = windupEngine.processArchive(inputLocation, outputLocation);
					generateReport(amd, outputLocation);
				}
				else {
					ArchiveMetadata amd = windupEngine.processArchive(inputLocation, outputLocation);
					generateReport(amd, outputLocation);
				}
			}
		}
	}
	
	protected File generateArchiveOutputLocation(File input) throws IOException {
		String outputLoc = StringUtils.substringBeforeLast(input.getAbsolutePath(), ".");
		outputLoc += "-" + StringUtils.substringAfterLast(input.getAbsolutePath(), ".") + "-doc";
		
		File outputLocation = new File(outputLoc);
		FileUtils.forceMkdir(outputLocation);
		
		return outputLocation;
	}

	
	
	/**
	 * Processes a directory of zip archives, producing reports for each archive.  This is non-recursive.
	 * @param dirPath - path to "batch" zip directory
	 * @return
	 * @throws IOException
	 */
	public Collection<ArchiveMetadata> batchInputDirectory(File dirPath) throws IOException {
		Validate.notNull(dirPath, "Directory Path is required, but null.");
		Validate.isTrue(dirPath.isDirectory(), "Directory Path must be to directory.");
		
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

		List<ArchiveMetadata> archiveMetas = new LinkedList<ArchiveMetadata>();
		for (File archive : archives) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing archive: " + archive.getAbsolutePath());
			}
			LOG.info("ArchiveMetadata Path: " + archive.getAbsolutePath());
			
			File output = generateArchiveOutputLocation(archive);
			archiveMetas.add(windupEngine.processArchive(archive, output));
		}
		
		return archiveMetas;
	}
	
	
	protected void generateReport(ArchiveMetadata archive, File reportDirectory) {
	    if (archive.getName() == null) {
	        LOG.info("Processing reports for: " + archive.getFilePointer().toString());
	    } else {
	        LOG.info("Processing reports for: "+archive.getName());
	    }
		
		if(reporters != null) {
			for(Reporter reporter : reporters) {
				reporter.process(archive, reportDirectory);
			}
		}
		
		if(reporters == null || reporters.size() == 0){
			LOG.warn("No reporters are currently registered.");
		}
		
		LOG.info("Reporting complete.");
		
	}

}
