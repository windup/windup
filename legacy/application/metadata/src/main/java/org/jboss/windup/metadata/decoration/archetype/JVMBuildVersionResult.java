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
package org.jboss.windup.metadata.decoration.archetype;

import org.jboss.windup.metadata.decoration.AbstractDecoration;

public class JVMBuildVersionResult extends AbstractDecoration {

	private String jdkBuildVersion;

	public String getJdkBuildVersion() {
		return jdkBuildVersion;
	}

	public void setJdkBuildVersion(String jdkBuildVersion) {
		this.jdkBuildVersion = jdkBuildVersion;
	}
}
