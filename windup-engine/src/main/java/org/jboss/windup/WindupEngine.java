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
import java.io.IOException;

import org.jboss.windup.resource.type.FileMeta;

/**
 * <p>
 * This class has been replaced by the {@link WindupMetaEngine} and the {@link WindupReportEngine}.
 * At some time in the future it will go away.
 * </p>
 */
@Deprecated
public class WindupEngine {
	
	WindupMetaEngine metaEngine;
	
	WindupReportEngine reportEngine;

	@Deprecated
	public WindupEngine(WindupEnvironment settings) {
		this.metaEngine = new WindupMetaEngine(settings);
		this.reportEngine = new WindupReportEngine(this.metaEngine);
	}
	
	/**
	 * @see {@link WindupMetaEngine#getFileMeta(File)}
	 */
	@Deprecated
	public FileMeta process(String filePath) throws IOException {
		return this.metaEngine.getFileMeta(new File(filePath));
	}

	/**
	 * @see {@link WindupReportEngine#generateReport(File, File)}
	 */
	@Deprecated
	public void process(File inputLocation, File outputLocation) throws IOException {
		this.reportEngine.generateReport(inputLocation, outputLocation);
	}
}