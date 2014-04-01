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
import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.test.helpers.WindupTestCase;
import org.junit.Test;

/**
 * <p>
 * Test the {@link WindupMetaEngine} multi file processing API.
 * </p>
 */
public class TestWindupMetaEngineArchiveProcessing extends WindupTestCase {
	@Test
	public void testMeta_WASEAR_NoOutputDir() throws IOException {
		Map<String, String[]> entries = new HashMap<String, String[]>();
		
		entries.put("META-INF/deployment.xml",
			new String[] {
				"IBM Deployment Descriptor"
			}
		);
		
		this.runArchiveMetaTest("archives/WAS-EAR.ear", null, entries, null);
	}
	
	@Test
	public void testMeta_WASEAR_SpecifiedOutputDir() throws IOException {
		Map<String, String[]> entries = new HashMap<String, String[]>();
		
		entries.put("META-INF/deployment.xml",
			new String[] {
				"IBM Deployment Descriptor"
			}
		);
		
		this.runArchiveMetaTest("archives/WAS-EAR.ear", null, entries, "archives/test_WASEAR_SpecifiedOutputDir");
	}
	
	@Test
	public void testMeta_PortalWAR_NoOutputDir() throws IOException {
		Map<String, String[]> entries = new HashMap<String, String[]>();
		
		entries.put("WebContent/Portal.jsp",
			new String[] {
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-navigation",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-dynamicui",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-logic",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-core",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-fmt"
			}
		);
		
		this.runArchiveMetaTest("archives/Portal-WAR.war", null, entries, null);
	}
	
	@Test
	public void testMeta_PortalWAR_SpecifiedOutputDir() throws IOException {
		Map<String, String[]> entries = new HashMap<String, String[]>();
		
		entries.put("WebContent/Portal.jsp",
			new String[] {
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-navigation",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-dynamicui",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-logic",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-core",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-fmt"
			}
		);
		
		this.runArchiveMetaTest("archives/Portal-WAR.war", null, entries, "archives/test_PortalWAR_SpecifiedOutputDir");
	}
	
	@Test
	public void testMeta_MultipleArchives_NoOutputDir() throws IOException {
		Map<String, Map<String, String[]>> expected = new HashMap<String, Map<String,String[]>>();
		
		Map<String, String[]> portalWarEntries = new HashMap<String, String[]>();
		portalWarEntries.put("WebContent/Portal.jsp",
			new String[] {
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-navigation",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-dynamicui",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-logic",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-core",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-fmt"
			}
		);
		
		Map<String, String[]> wasEAREntries = new HashMap<String, String[]>();
		wasEAREntries.put("META-INF/deployment.xml",
				new String[] {
					"IBM Deployment Descriptor"
				}
			);
		
		expected.put("Portal-WAR.war", portalWarEntries);
		expected.put("WAS-EAR.ear", wasEAREntries);
		
		this.runMultipleArchiveMetaTest("archives", null, expected, null);
	}
	
	@Test
	public void testMeta_MultipleArchives_SpecifiedOutputDir() throws IOException {
		Map<String, Map<String, String[]>> expected = new HashMap<String, Map<String,String[]>>();
		
		Map<String, String[]> portalWarEntries = new HashMap<String, String[]>();
		portalWarEntries.put("WebContent/Portal.jsp",
			new String[] {
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-navigation",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-dynamicui",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-logic",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-core",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-fmt"
			}
		);
		
		Map<String, String[]> wasEAREntries = new HashMap<String, String[]>();
		wasEAREntries.put("META-INF/deployment.xml",
				new String[] {
					"IBM Deployment Descriptor"
				}
			);
		
		expected.put("Portal-WAR.war", portalWarEntries);
		expected.put("WAS-EAR.ear", wasEAREntries);
		
		this.runMultipleArchiveMetaTest("archives", null, expected, "archives/test_MultipleArchives_SpecifiedOutputDir");
	}
}