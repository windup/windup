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
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.resource.type.archive.ArchiveMeta;


public class ReportEngine implements Reporter {
	private static final Log LOG = LogFactory.getLog(ReportEngine.class);
	private Collection<Reporter> reporters;
	
	public void setReporters(Collection<Reporter> reporters) {
		this.reporters = reporters;
	}
	
	@Override
	public void process(ArchiveMeta archive, File reportDirectory) {
		
		LOG.info("Processing reports for: "+archive.getName());
		
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
