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
package org.jboss.windup.test.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

/**
 * @author Ian
 *
 */
public class FileHelpers {
	/**
	 * Ignore all lines that start with this prefix when comparing two files
	 */
	private static final String IGNORE_PREFIX = "%COMPARE_IGNORE%";
	
	/**
	 * Treat any line that start with this string as a regex
	 */
	private static final String REGEX_PREFIX = "%COMPARE_REGEX%";
	
	/**
	 * <p>Compares the folder structure and contents of two folders including the contents
	 * of the files in those folders and the names of all of the files and folders.</p>
	 * 
	 * @param expectedPath expected folder
	 * @param actualPath actual folder
	 * 
	 * @throws IOException can happen when reading files
	 */
	public static void compareFolders(String expectedPath, String actualPath) throws IOException {
		File expectedFolder = new File(expectedPath);
		
		List<File> expectedFiles = new LinkedList<File>(Arrays.asList(expectedFolder.listFiles()));
		
		while(!expectedFiles.isEmpty()) {
			File expectedFile = expectedFiles.remove(0);
			
			//determine if actual resource exists for current expected file
			String relativePath = getPathRelativeToo(expectedFolder, expectedFile);
			File actualFile = new File(actualPath + relativePath);
			
			//ignore . files
			if(!actualFile.getName().startsWith(".")) {
				if(actualFile.exists()) {
				
					/* if actual resource exists and is a directory add files nested
					 * 		under expected folder to expected files list
					 * else if file check to see if actual file matches expected file */
					if(expectedFile.isDirectory()) {
						expectedFiles.addAll(Arrays.asList(expectedFile.listFiles()));
					} else {
						compareFile(expectedFile, actualFile);
					}
				} else {
					Assert.fail("Expected file not found in actual folder: " + actualFile.getPath());
				}
			}
		}
	}
	
	/**
	 * <p>Gets the path of a given file relative to a given parent.</p>
	 * 
	 * @param parentFolder get the path of the given file relative to this parent
	 * @param file get the path of this file relative to the given parent
	 * 
	 * @return path of a given file relative to a given parent
	 */
	private static String getPathRelativeToo(File parentFolder, File file) {
		String path = file.getPath();
		
		String parentPath = parentFolder.getPath();
		if(path.startsWith(parentPath)) {
			path = path.substring(parentPath.length(), path.length());
		}
		
		return path;
	}
	
	/**
	 * <p>Compares two files contents.</p>
	 * 
	 * @param expectedFile expected file
	 * @param actualFile actual file
	 * 
	 * @throws IOException can happen when reading files
	 */
	private static void compareFile(File expectedFile, File actualFile) throws IOException {
		List<String> expectedLines = FileUtils.readLines(expectedFile);
		List<String> actualLines = FileUtils.readLines(actualFile);
		
		for(int i = 0; i < expectedLines.size(); ++i) {
			String expectedLine = expectedLines.get(i);
			String actualLine = actualLines.get(i);
			
			//ignore any line that starts with the ignore prefix
			if(!expectedLine.startsWith(IGNORE_PREFIX)) {
				
				/* if line is a regex match then match using regex
				 * else do simple equals comparison */
				if(expectedLine.startsWith(REGEX_PREFIX)) {
					expectedLine = expectedLine.replaceFirst(REGEX_PREFIX, "");
					Assert.assertTrue("Actual file line " + (i+1) + " does not match expected regular expression:\n" + actualLine + "\n" + expectedLine, Pattern.matches(expectedLine, actualLine));
				} else {
					Assert.assertEquals("Actual file line " + (i+1) + " does not match expected: " + actualFile.getPath(),
							expectedLine, actualLine);
				}
			}
		}
	}
}
