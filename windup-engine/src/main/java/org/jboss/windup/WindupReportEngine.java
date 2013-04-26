/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Ian Tewksbury - ian@itewk.com - Initial API and implementation
 */
package org.jboss.windup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.reporting.ReportEngine;
import org.jboss.windup.reporting.Reporter;
import org.jboss.windup.resource.type.archive.ArchiveMeta;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>
 * Main entry point for generating Windup Reports.
 * </p>
 */
public class WindupReportEngine {
	private static final Log LOG = LogFactory.getLog(WindupReportEngine.class);

	private WindupMetaEngine metaEngine;

	private ApplicationContext context;

	private Reporter reportEngine;

	/**
	 * <p>
	 * Creates a {@link WindupReportEngine} that creates a new
	 * {@link WindupMetaEngine} from the given {@link WindupEnvironment}
	 * settings.
	 * </p>
	 * 
	 * @param settings
	 *            {@link WindupEnvironment} used when creating meta information
	 *            used to generate reports with
	 */
	public WindupReportEngine(WindupEnvironment settings) {
		this.metaEngine = new WindupMetaEngine(settings);

		// sets environment variables needed for Spring configuration.
		List<String> springContexts = new LinkedList<String>();
		springContexts.add("jboss-windup-context.xml");
		this.context = new ClassPathXmlApplicationContext(springContexts.toArray(new String[springContexts.size()]));
		this.reportEngine = (ReportEngine) context.getBean("report-engine");
	}

	/**
	 * <p>
	 * Creates a {@link WindupReportEngine} that uses the given
	 * {@link WindupMetaEngine}
	 * </p>
	 * 
	 * @param metaEngine
	 *            {@link WindupMetaEngine} used to create the meta information
	 *            used to generate reports with
	 */
	public WindupReportEngine(WindupMetaEngine metaEngine) {
		this.metaEngine = metaEngine;

		// sets environment variables needed for Spring configuration.
		List<String> springContexts = new LinkedList<String>();
		springContexts.add("jboss-windup-context.xml");
		this.context = new ClassPathXmlApplicationContext(springContexts.toArray(new String[springContexts.size()]));
		this.reportEngine = (ReportEngine) context.getBean("report-engine");
	}

	/**
	 * <p>
	 * Generates a report for the given input to the given output
	 * </p>
	 * 
	 * @param input
	 *            generate a report from this input
	 * @param output
	 *            generate the report to this location
	 * 
	 * @throws IOException
	 *             this can happen when doing file stuff
	 */
	public void generateReport(File input, File output) throws IOException {
		if (!input.exists()) {
			throw new FileNotFoundException("Nothing exists at the given path: " + input);
		}

		/* if output specified use that
		 * else create directory at same level as input with -doc appended */
		File actualOutput;
		if (output != null) {
			actualOutput = output;
		} else {
			String outputPathLoc = input.getPath() +"-doc";
			
			//create output path...
			actualOutput = new File(outputPathLoc);
			
			if(LOG.isInfoEnabled()) {
				LOG.info("Creating output path: " + actualOutput.getAbsolutePath());
				LOG.info("  - To overwrite this in the future, use the -output parameter.");
			}
		}

		//generate the meta
		ArchiveMeta meta = this.metaEngine.getArchiveMeta(input, actualOutput);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Generate report for meta generated for '" + input + "' to '" + actualOutput + "'");
		}
		
		//generate the report
		reportEngine.process(meta, actualOutput);
	}
}