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
package org.jboss.windup.decorator.simple;

import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.metadata.decoration.Global;
import org.jboss.windup.metadata.decoration.effort.Effort;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;
import org.jboss.windup.metadata.type.FileMetadata;

public class GlobalDecorator implements MetaDecorator<FileMetadata> {
	protected String description;
	protected Effort effort = new UnknownEffort();

	public void setEffort(Effort effort) {
		this.effort = effort;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void processMeta(FileMetadata file) {
		Global gr = new Global();
		gr.setDescription(description);
		gr.setEffort(effort);
		
		file.getDecorations().add(gr);
	}
}
