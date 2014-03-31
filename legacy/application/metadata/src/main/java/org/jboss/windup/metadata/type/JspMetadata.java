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
package org.jboss.windup.metadata.type;

import java.util.HashSet;
import java.util.Set;

/**
 * Responsible for keeping track of a JSP file's included Tag Libraries and Class Imports.
 * 
 * @author bdavis
 * 
 */
public class JspMetadata extends FileMetadata {
	private Set<String> classDependencies = new HashSet<String>();
	private Set<String> taglibDependencies = new HashSet<String>();

	public Set<String> getClassDependencies() {
		return classDependencies;
	}

	public void setClassDependencies(Set<String> classDependencies) {
		this.classDependencies = classDependencies;
	}

	public Set<String> getTaglibDependencies() {
		return taglibDependencies;
	}

	public void setTaglibDependencies(Set<String> taglibDependencies) {
		this.taglibDependencies = taglibDependencies;
	}
}
