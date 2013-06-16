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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.WindupEngine;
import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Hash;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.ReportEngine;
import org.junit.Assert;

/**
 * <p>
 * The parent class for all Windup test cases.
 * </p>
 */
public class WindupTestCase {
	/**
	 * The path to the parent directory where the resources used by this test
	 * should be stored
	 */
	private final String PARENT_DIR_PATH = new File(this.getClass().getResource(this.getClass().getSimpleName() + ".class").getFile()).getParent() + File.separator;

	/**
	 * <p>
	 * Runs a test on a single file processed by the Windup engine.
	 * </p>
	 * 
	 * @param fileName
	 *            name of the file to test, relative to the test class
	 * @param expectedFileDecorations
	 *            expected file decorations
	 * @param expectedArhciveDecorations
	 *            expected archive decorations
	 * 
	 * @throws IOException
	 *             can happen when reading files
	 */
	protected void runSingleFileMetaTest(String fileName, String[] expectedFileDecorations, String[] expectedArhciveDecorations) throws IOException {

		if (expectedFileDecorations == null) {
			expectedFileDecorations = new String[0];
		}

		if (expectedArhciveDecorations == null) {
			expectedArhciveDecorations = new String[0];
		}

		File inputFile = new File(this.getResourcePath(fileName));
		FileMetadata meta = this.getMetaEngine().processFile(inputFile);

		Assert.assertNotNull("Returned meta should not be null", meta);

		// search the file for the expected decorations
		for (String expectedFileDecoration : expectedFileDecorations) {
			boolean found = false;
			for (AbstractDecoration decoration : meta.getDecorations()) {
				if (decoration.getDescription().equals(expectedFileDecoration)) {
					found = true;
					break;
				}
			}

			if (!found) {
				Assert.fail("Did not find expected file decoration: '" + expectedFileDecoration + "'");
			}
		}
		Assert.assertEquals("The number of file decorations does not match expected", expectedFileDecorations.length, meta.getDecorations().size());

		// search the archive for the expected decorations
		for (String expectedArchiveDecoration : expectedArhciveDecorations) {
			boolean found = false;
			for (AbstractDecoration decoration : meta.getArchiveMeta().getDecorations()) {
				if (decoration.getDescription().equals(expectedArchiveDecoration)) {
					found = true;
					break;
				}
			}

			if (!found) {
				Assert.fail("Did not find expected archive decoration: '" + expectedArchiveDecoration + "'");
			}
		}
		Assert.assertEquals("The number of archvie decorations does not match expected", expectedArhciveDecorations.length, meta.getArchiveMeta().getDecorations().size());
	}

	/**
	 * <p>
	 * Run a test on a single archive.
	 * </p>
	 * 
	 * @param archivePath
	 *            path to the archive to test
	 * @param expectedArchiveDecorations
	 *            expected decorations on the archive
	 * @param expectedArchiveEntryDecorations
	 *            map of file paths to their expected decorations
	 * @param outputPath
	 *            Optional. The output directory to be used when generating the
	 *            meta
	 * 
	 * @throws IOException
	 *             can happen when reading files
	 */
	protected void runSourceDirectoryMetaTest(String archivePath, String[] expectedArchiveDecorations, Map<String, String[]> expectedArchiveEntryDecorations, String outputPath) throws IOException {

		File input = new File(this.getResourcePath(archivePath));
		File output = new File(this.getResourcePath(outputPath));
		ArchiveMetadata archiveMeta = this.getMetaEngine().processSourceDirectory(input, output);

		this.runArchiveMetaTest(archiveMeta, expectedArchiveDecorations, expectedArchiveEntryDecorations);
	}

	
	/**
	 * <p>
	 * Run a test on a single archive.
	 * </p>
	 * 
	 * @param archivePath
	 *            path to the archive to test
	 * @param expectedArchiveDecorations
	 *            expected decorations on the archive
	 * @param expectedArchiveEntryDecorations
	 *            map of file paths to their expected decorations
	 * @param outputPath
	 *            Optional. The output directory to be used when generating the
	 *            meta
	 * 
	 * @throws IOException
	 *             can happen when reading files
	 */
	protected void runArchiveMetaTest(String archivePath, String[] expectedArchiveDecorations, Map<String, String[]> expectedArchiveEntryDecorations, String outputPath) throws IOException {

		File input = new File(this.getResourcePath(archivePath));
		File output = new File(this.getResourcePath(outputPath));
		ArchiveMetadata archiveMeta = this.getArchiveMeta(input, output);

		this.runArchiveMetaTest(archiveMeta, expectedArchiveDecorations, expectedArchiveEntryDecorations);
	}

	/**
	 * <p>
	 * Runs an archive report test by comparing a generated report to an
	 * expected report.
	 * </p>
	 * 
	 * @param archivePath
	 *            archive to generate a report for
	 * @param reportPath
	 *            location to generate the report to
	 * @param expectedReportPath
	 *            location of the expected report contents
	 * 
	 * @throws IOException
	 *             this can happen when performing file operations
	 */
	protected void runArchiveReportTest(String archivePath, String reportPath, String expectedReportPath) throws IOException {
		// cleanup any existing output
		File output = null;
		String outputPath = null;
		
		if(reportPath != null) {
			outputPath = this.getResourcePath(reportPath);
			output = new File(outputPath);
			output.delete();
		}

		// generate report
		ReportEngine reportEngine = new ReportEngine(this.getEnvironment());
		File input = new File(this.getResourcePath(archivePath));
		reportEngine.process(input, output);

		//if not output specified the engine will append -doc
		if(outputPath == null) {
			outputPath = StringUtils.substringBeforeLast(input.getAbsolutePath(), ".");
			outputPath += "-" + StringUtils.substringAfterLast(input.getAbsolutePath(), ".") + "-doc";
		}
		
		// compare generated report to expected report
		FileHelpers.compareFolders(this.getResourcePath(expectedReportPath), outputPath);

		// cleanup generated report depending on where it is
		if(output != null) {
			output.delete();
		} else {
			(new File(outputPath)).delete();
		}
	}

	/**
	 * <p>
	 * Run a test on a directory of archives.
	 * </p>
	 * 
	 * @param parentPath
	 *            path to the directory containing the archives to test
	 * @param expectedArchiveDecorations
	 *            map of archive names to expected decorations on the archive
	 * @param expectedArchiveEntryDecorations
	 *            map of archive names to maps of file paths to their expected
	 *            decorations
	 * @param outputDir
	 *            Optional. The output directory to be used when generating the
	 *            meta
	 * 
	 * @throws IOException
	 *             can happen when reading files
	 */
	protected void runMultipleArchiveMetaTest(String parentPath, Map<String, String[]> expectedArchiveDecorations, Map<String, Map<String, String[]>> expectedArchiveEntryDecorations, String outputPath) throws IOException {

		if (expectedArchiveDecorations == null) {
			expectedArchiveDecorations = new HashMap<String, String[]>();
		}

		if (expectedArchiveEntryDecorations == null) {
			expectedArchiveEntryDecorations = new HashMap<String, Map<String, String[]>>();
		}

		File input = new File(this.getResourcePath(parentPath));
		File output = new File(this.getResourcePath(outputPath));
		Collection<ArchiveMetadata> parentArchiveMetas = this.getReportEngine().batchInputDirectory(input);
		Assert.assertNotNull("Returned meta should not be null", parentArchiveMetas);

		for (ArchiveMetadata childArchiveMeta : parentArchiveMetas) {
			String name = childArchiveMeta.getName();

			this.runArchiveMetaTest(childArchiveMeta, expectedArchiveDecorations.get(name), expectedArchiveEntryDecorations.get(name));
		}
	}


	/**
	 * @return a fully setup {@link WindupMetaEngine} for use during testing
	 */
	protected ReportEngine getReportEngine() {
		WindupEnvironment settings = getEnvironment();
		ReportEngine metaEngine = new ReportEngine(settings);

		return metaEngine;
	}

	
	/**
	 * @return a fully setup {@link WindupMetaEngine} for use during testing
	 */
	protected WindupEngine getMetaEngine() {
		WindupEnvironment settings = getEnvironment();
		WindupEngine metaEngine = new WindupEngine(settings);

		return metaEngine;
	}

	/**
	 * @return {@link WindupEnvironment} setup for testing
	 */
	protected WindupEnvironment getEnvironment() {
		WindupEnvironment settings = new WindupEnvironment();

		return settings;
	}

	/**
	 * @param resourceName
	 *            name of the resource relative to this test to get the path for
	 * 
	 * @return full path to the resource
	 */
	protected final String getResourcePath(String resourceName) {
		return PARENT_DIR_PATH + resourceName;
	}

	/**
	 * <p>
	 * Run a test on a single {@link ArchiveMetadata}
	 * </p>
	 * 
	 * @param archiveMeta
	 *            {@link ArchiveMetadata} to test
	 * @param expectedArchiveDecorations
	 *            expected decorations on the archive
	 * @param expectedArchiveEntryDecorations
	 *            map of file paths to their expected decorations
	 */
	private void runArchiveMetaTest(ArchiveMetadata archiveMeta, String[] expectedArchiveDecorations, Map<String, String[]> expectedArchiveEntryDecorations) {
		Assert.assertNotNull("ArchiveMetadata should not be null", archiveMeta);

		if (expectedArchiveDecorations == null) {
			expectedArchiveDecorations = new String[0];
		}

		if (expectedArchiveEntryDecorations == null) {
			expectedArchiveEntryDecorations = new HashMap<String, String[]>();
		}

		// search the archive for the expected decorations
		Collection<AbstractDecoration> archiveDecorations = new ArrayList<AbstractDecoration>(archiveMeta.getDecorations());
		for (String expectedArchiveDecoration : expectedArchiveDecorations) {
			boolean foundExpectedArchiveDecoration = false;

			// check entry decorators for expected decorator
			Iterator<AbstractDecoration> decorationsIter = archiveDecorations.iterator();
			while (decorationsIter.hasNext() && !foundExpectedArchiveDecoration) {
				AbstractDecoration decoration = decorationsIter.next();
				if (decoration.getDescription().equals(expectedArchiveDecoration)) {
					foundExpectedArchiveDecoration = true;

					// remove the matched decoration
					decorationsIter.remove();
				}
			}
			Assert.assertTrue("Did not find expected archive decoration: '" + expectedArchiveDecoration + "' in '" + archiveMeta.getName() + "'", foundExpectedArchiveDecoration);
		}
		
		//remove all "hash" decorators becuase they are always expected
		Iterator<AbstractDecoration> unexpecedDecorationsIter = archiveDecorations.iterator();
		while(unexpecedDecorationsIter.hasNext()) {
			if(unexpecedDecorationsIter.next() instanceof Hash) {
				unexpecedDecorationsIter.remove();
			}
		}
		
		// error if found any unexpected decorations on the archive
		if (!archiveDecorations.isEmpty()) {
			StringBuffer unexpectedArchiveDecorators = new StringBuffer();
			unexpectedArchiveDecorators.append("The archive '" + archiveMeta.getName() + "' has unexpected decorators:\n");
			for (AbstractDecoration unexpectedDecoration : archiveDecorations) {
				unexpectedArchiveDecorators.append("\t");
				unexpectedArchiveDecorators.append(unexpectedDecoration);
				unexpectedArchiveDecorators.append("\n");
			}

			Assert.fail(unexpectedArchiveDecorators.toString());
		}

		Set<FileMetadata> entries = new HashSet<FileMetadata>(archiveMeta.getEntries());
		Set<String> expectedEntryPaths = expectedArchiveEntryDecorations.keySet();
		for (String expectedEntryFilePath : expectedEntryPaths) {
			boolean foundEntry = false;

			// search entries for expected entry
			Iterator<FileMetadata> entriesIter = entries.iterator();
			while (entriesIter.hasNext()) {
				FileMetadata entry = entriesIter.next();

				// if entry matches expected entry path search for expected
				// decorators on that entry
				String relativeEntrypath = FilenameUtils.normalize(entry.getPathRelativeToArchive());
				if (relativeEntrypath.equals(FilenameUtils.normalize(expectedEntryFilePath))) {
					foundEntry = true;

					// remove this entry since it has matched
					entriesIter.remove();

					// for each expected entry decorator see if it exists
					String[] expectedEntryDecorators = expectedArchiveEntryDecorations.get(expectedEntryFilePath);
					Collection<AbstractDecoration> entryDecorations = new ArrayList<AbstractDecoration>(entry.getDecorations());
					for (String expectedEntryDecorator : expectedEntryDecorators) {
						boolean foundExpectedEntryDecorator = false;

						// check entry decorators for expected decorator
						Iterator<AbstractDecoration> decorationsIter = entryDecorations.iterator();
						while (decorationsIter.hasNext() && !foundExpectedEntryDecorator) {
							AbstractDecoration decoration = decorationsIter.next();
							if (decoration.getDescription().equals(expectedEntryDecorator)) {
								foundExpectedEntryDecorator = true;

								// remove the matched decoration
								decorationsIter.remove();
							}
						}
						Assert.assertTrue("Did not find expected archive entry decoration '" + expectedEntryDecorator + "' on '" + expectedEntryFilePath + "' in '" + archiveMeta.getName() + "'", foundExpectedEntryDecorator);
					}

					// error if found any unexpected decorations on the expected
					// entry
					if (!entryDecorations.isEmpty()) {
						StringBuffer unexpectedEntryDecorators = new StringBuffer();
						unexpectedEntryDecorators.append("The entry '" + expectedEntryFilePath + "' in '" + archiveMeta.getName() + "' contained unexpected entries:\n");
						for (AbstractDecoration unexpectedDecoration : entryDecorations) {
							unexpectedEntryDecorators.append("\t");
							unexpectedEntryDecorators.append(unexpectedDecoration.getDescription());
							unexpectedEntryDecorators.append("\n");
						}

						Assert.fail(unexpectedEntryDecorators.toString());
					}
				}
			}

			Assert.assertTrue("Did not find expected archive entry in '" + archiveMeta.getName() + "' for '" + expectedEntryFilePath + "'", foundEntry);
		}

		// error if found any unexpected entries
		if (!entries.isEmpty()) {
			StringBuffer unexpectedEntries = new StringBuffer();
			unexpectedEntries.append("The archive '" + archiveMeta.getName() + "' contained unexpected entries:\n");
			for (FileMetadata entry : entries) {
				unexpectedEntries.append("\t");
				unexpectedEntries.append(entry);
				unexpectedEntries.append("\n");
			}

			Assert.fail(unexpectedEntries.toString());
		}
	}

	/**
	 * <p>
	 * Uses {@link WindupMetaEngine} to generate {@link ArchiveMetadata} for the
	 * given input.
	 * </p>
	 * 
	 * @param input
	 *            {@link File} to generate {@link ArchiveMetadata} for
	 * @param outputDir
	 *            Optional. The output directory to be used when generating the
	 *            meta
	 * 
	 * @return {@link ArchiveMetadata} generated for the given {@link File}
	 * 
	 * @throws IOException
	 *             can happen when trying to read files
	 */
	private ArchiveMetadata getArchiveMeta(File input, File outputDir) throws IOException {
		WindupEngine engine = this.getMetaEngine();
		ArchiveMetadata meta = engine.processArchive(input, outputDir);

		return meta;
	}
}