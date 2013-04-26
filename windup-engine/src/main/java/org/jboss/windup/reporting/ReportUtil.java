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

import org.apache.commons.lang.StringUtils;

public class ReportUtil {

	public static String calculateRelativePathToRoot(File reportDirectory, File htmlOutputPath) {
		String archiveOutput = reportDirectory.getAbsolutePath();
		String htmlOutput = htmlOutputPath.getAbsolutePath();

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
		String relPath = StringUtils.removeStart(relativeFile.getAbsolutePath(), reportDirectory.getAbsolutePath());
		relPath = StringUtils.replace(relPath, "\\", "/");
		relPath = StringUtils.removeStart(relPath, "/");
		return relPath;
	}
}
