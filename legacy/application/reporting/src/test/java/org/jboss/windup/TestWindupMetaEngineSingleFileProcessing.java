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

import org.jboss.windup.test.helpers.WindupTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * Test the {@link WindupMetaEngine} single file processing API.
 * </p>
 */
public class TestWindupMetaEngineSingleFileProcessing extends WindupTestCase {
	/**
	 * @see org.jboss.windup.test.helpers.WindupTestCase#getEnvironment()
	 */
	@Override
	protected WindupEnvironment getEnvironment() {
		WindupEnvironment settings = new WindupEnvironment();
		settings.setSource(true);
		
		return settings;
	}
	
	@Test
	public void testMeta_processSingleFile_FileDoesNotExist() throws IOException {
		try {
			WindupEngine engine = this.getMetaEngine();
			engine.processFile(new File("DoesNotExist.xml"));
			
			Assert.fail("Windup must thought the file existed when it should not have.");
		} catch (FileNotFoundException e) {
			//this is what we are looking for
		}
	}
	
	@Test
	public void testMeta_processSingleFile_Simple() throws IOException {
		this.runSingleFileMetaTest("files/Simple.xml", null, null);
	}
	
	@Test
	public void testMeta_processSingleFile_BadSyntax() throws IOException {
		this.runSingleFileMetaTest("files/BadSyntax.xml",
				new String[] {
					"Bad XML? Unable to parse."
				},
				null);
	}
	
	@Test
	public void testMeta_processSingleFile_IBMDeploymentDescriptor() throws IOException {
		this.runSingleFileMetaTest("files/deployment.xml",
				new String[] {
					"IBM Deployment Descriptor"
				},
				null);
	}
	
	@Test
	public void testMeta_processSingleFile_Portal() throws IOException {
		this.runSingleFileMetaTest("files/Portal.jsp",
				new String[] {
					"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-navigation",
					"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-dynamicui",
					"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-logic",
					"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-core",
					"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-fmt"
				},
				null);
	}
}