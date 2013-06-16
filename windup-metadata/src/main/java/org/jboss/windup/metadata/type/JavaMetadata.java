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

import java.util.Set;

/**
 * Extends the FileMetadata, keeps track of Java class specific information including class dependencies
 * and blacklisted dependencies.
 * 
 * @author bdavis
 * 
 */
public class JavaMetadata extends FileMetadata {
	private String qualifiedClassName;
	private boolean abstractClz;
	private boolean interfaceClz;
	private boolean publicClz;

	private Set<String> classDependencies;
	private Set<String> blackListedDependencies;

	public void setAbstractClz(boolean abstractClz) {
		this.abstractClz = abstractClz;
	}

	public void setInterfaceClz(boolean interfaceClz) {
		this.interfaceClz = interfaceClz;
	}

	public void setPublicClz(boolean publicClz) {
		this.publicClz = publicClz;
	}

	public boolean isAbstractClz() {
		return abstractClz;
	}

	public boolean isInterfaceClz() {
		return interfaceClz;
	}

	public boolean isPublicClz() {
		return publicClz;
	}

	public void setQualifiedClassName(String qualifiedClassName) {
		this.qualifiedClassName = qualifiedClassName;
	}

	public String getQualifiedClassName() {
		return qualifiedClassName;
	}

	public void setBlackListedDependencies(Set<String> blackListedDependencies) {
		this.blackListedDependencies = blackListedDependencies;
	}

	public Set<String> getBlackListedDependencies() {
		return blackListedDependencies;
	}

	public Set<String> getClassDependencies() {
		return classDependencies;
	}

	public void setClassDependencies(Set<String> classDependencies) {
		this.classDependencies = classDependencies;
	}

	@Override
	public String toString() {
		return "JavaMetadata [qualifiedClassName=" + qualifiedClassName + ", classDependencies=" + classDependencies + ", blackListedDependencies="
				+ blackListedDependencies + ", filePointer=" + filePointer + "]";
	}
}
