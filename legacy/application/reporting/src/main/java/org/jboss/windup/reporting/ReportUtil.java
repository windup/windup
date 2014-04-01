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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReportUtil {

	private static final Log LOG = LogFactory.getLog(ReportUtil.class);
	
	public static String calculateRelativePathToRoot(File reportDirectory, File htmlOutputPath) {
		Validate.notNull(reportDirectory, "Report directory is null, but a required field.");
		Validate.notNull(htmlOutputPath, "HTML output directory is null, but a required field.");
		
		String archiveOutput = FilenameUtils.normalize(reportDirectory.getAbsolutePath());
		String htmlOutput = FilenameUtils.normalize(htmlOutputPath.getAbsolutePath());
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("archiveOutput: "+archiveOutput);
			LOG.debug("htmlOutput: "+htmlOutput);
		}

		String relative = StringUtils.removeStart(htmlOutput, archiveOutput);
		relative = StringUtils.replace(relative, "\\", "/");

		int dirCount = (StringUtils.countMatches(relative, "/") - 1);
		String relPath = "";
		for (int i = 0; i < dirCount; i++) {
			relPath += "../";
		}
		return relPath;
	}
	
	public static String calculateRelativePathFromRoot(File reportDirectory, File relativeFile) {
		String relPath = StringUtils.removeStart(
				FilenameUtils.normalize(relativeFile.getAbsolutePath()),
				FilenameUtils.normalize(reportDirectory.getAbsolutePath()));
		relPath = StringUtils.replace(relPath, "\\", "/");
		relPath = StringUtils.removeStart(relPath, "/");
		return relPath;
	}
}
