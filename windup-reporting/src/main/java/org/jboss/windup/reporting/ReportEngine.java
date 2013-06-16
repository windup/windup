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
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.WindupEngine;
import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.springframework.context.ApplicationContext;


public class ReportEngine {
	private static final Log LOG = LogFactory.getLog(ReportEngine.class);

	private final Collection<Reporter> reporters;
	private final ApplicationContext context;
	private final WindupEngine windupEngine;
	private final WindupEnvironment settings;
	
	public ReportEngine(WindupEnvironment settings) {
		this.settings = settings;
		windupEngine = new WindupEngine(settings);
		context = windupEngine.getContext();
		reporters = (Collection<Reporter>)context.getBean("reporters");
	}
	
	/**
	 * Main processing method.  This 
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
			ArchiveMetadata am = windupEngine.processSourceDirectory(inputLocation, outputLocation);
			generateReport(am, outputLocation);
		}
		if (inputLocation.isDirectory()) {
			// must be batch mode!
			if (outputLocation != null) {
				LOG.warn("Ignoring output parameter as input is directory.");
			}
			Collection<ArchiveMetadata> amds = windupEngine.processDirectory(inputLocation);
			
			for(ArchiveMetadata amd : amds) {
				generateReport(amd, amd.getArchiveOutputDirectory());
			}
		}
		else {
			// single archive processing.
			if (outputLocation == null) {
				//generate output based on input.
				String outputLoc = StringUtils.substringBeforeLast(inputLocation.getAbsolutePath(), ".");
				outputLoc += "-" + StringUtils.substringAfterLast(inputLocation.getAbsolutePath(), ".") + "-doc";
				
				outputLocation = new File(outputLoc);
				
				ArchiveMetadata amd = windupEngine.processArchive(inputLocation);
				generateReport(amd, outputLocation);
			}
			else {
				ArchiveMetadata amd = windupEngine.processArchive(inputLocation, outputLocation);
				generateReport(amd, outputLocation);
			}
		}
	}
	
	
	
	protected void generateReport(ArchiveMetadata archive, File reportDirectory) {
		
	    if (archive.getName() == null) {
	        LOG.info("Processing reports for: " + archive.getFilePointer().toString());
	    } else {
	        LOG.info("Processing reports for: "+archive.getName());
	    }
		
		if(reporters != null) {
			for(Reporter reporter : reporters) {
				LOG.info("Report Directory: "+ reportDirectory);
				LOG.info("Archive Directory: "+ archive.getArchiveOutputDirectory());
				LOG.info("Archive: "+ archive);
				LOG.info("Reporter: "+ reporter);
				
				reporter.process(archive, reportDirectory);
			}
		}
		
		if(reporters == null || reporters.size() == 0){
			LOG.warn("No reporters are currently registered.");
		}
		
		LOG.info("Reporting complete.");
		
	}

}
