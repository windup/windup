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
package org.jboss.windup.reporting.data;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.windup.metadata.decoration.AbstractDecoration;


@XmlRootElement(name="resource", namespace="http://jboss.org/windup/resource")
public class ResourceData {

	private String syntax;
	private String relativePathToRoot;
	private String relativePathFromRoot;
	
	private Collection<AbstractDecoration> decorations = new HashSet<AbstractDecoration>();
	
	public String getSyntax() {
		return syntax;
	}
	
	public void setSyntax(String syntax) {
		this.syntax = syntax;
	}
	
	public String getRelativePathToRoot() {
		return relativePathToRoot;
	}

	public void setRelativePathToRoot(String relativePathToRoot) {
		this.relativePathToRoot = relativePathToRoot;
	}

	public String getRelativePathFromRoot() {
		return relativePathFromRoot;
	}

	public void setRelativePathFromRoot(String relativePathFromRoot) {
		this.relativePathFromRoot = relativePathFromRoot;
	}

	
	@XmlElementWrapper(name="decorations")
	@XmlElementRef
	public Collection<AbstractDecoration> getDecorations() {
		return decorations;
	}
	
	public void setDecorations(Collection<AbstractDecoration> decorations) {
		this.decorations = decorations;
	}
}
