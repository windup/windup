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
public class TestWindupMetaEngineSourceDirectoryProcessing extends WindupTestCase {
	
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
	public void testMeta_SourceDirectory() throws IOException {
		Map<String, String[]> entries = new HashMap<String, String[]>();
		
		entries.put("BadSyntax.xml",
			new String[] {
				"Bad XML? Unable to parse."
			}
		);
		entries.put("deployment.xml",
			new String[] {
				"IBM Deployment Descriptor"
			}
		);
		entries.put("Simple.xml", new String[] {});
		entries.put("Portal.jsp",
			new String[] {
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-navigation",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-dynamicui",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-logic",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-core",
				"Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-fmt"
			}
		);
		
		this.runSourceDirectoryMetaTest("files/", null, entries, null);
	}
}