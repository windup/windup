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
package org.jboss.windup.resource.type;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.jboss.windup.resource.decoration.AbstractDecoration;


public abstract class ResourceMeta {
	protected Collection<AbstractDecoration> decorations = new LinkedList<AbstractDecoration>();

	protected File filePointer;

	public Collection<AbstractDecoration> getDecorations() {
		return decorations;
	}

	public void setDecorations(Set<AbstractDecoration> decorations) {
		this.decorations = decorations;
	}

	public File getFilePointer() {
		return filePointer;
	}

	public void setFilePointer(File filePointer) {
		this.filePointer = filePointer;
	}

}
