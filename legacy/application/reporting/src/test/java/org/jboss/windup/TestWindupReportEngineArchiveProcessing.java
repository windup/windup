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

import java.io.IOException;

import org.jboss.windup.test.helpers.WindupTestCase;
import org.junit.Test;

/**
 * <p>
 * Test the {@link WindupMetaEngine} multi file processing API.
 * </p>
 */
public class TestWindupReportEngineArchiveProcessing extends WindupTestCase {
	@Test
	public void testReport_WASEAR() throws IOException {
		this.runArchiveReportTest(
				"archives/WAS-EAR.ear",
				"archives/testReport_WASEAR",
				"expectedReports/WAS-EAR.ear");
	}
	
	@Test
	public void testReport_WASEAR_noSpecifiedOutput() throws IOException {
		this.runArchiveReportTest(
				"archives/WAS-EAR.ear",
				null,
				"expectedReports/WAS-EAR.ear");
	}
	
	@Test
	public void testReport_PortalWAR() throws IOException {
		this.runArchiveReportTest(
				"archives/Portal-WAR.war",
				"archives/testReport_PortalWar",
				"expectedReports/Portal-WAR.war");
	}
}