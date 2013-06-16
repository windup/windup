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
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.decoration.effort.StoryPointEffort;
import org.jboss.windup.metadata.type.FileMetadata;

public class ClassificationDecorator implements MetaDecorator<FileMetadata> {
	protected String description;
	protected Integer effort;

	public void setEffort(int effort) {
		this.effort = effort;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void processMeta(FileMetadata file) {
		Classification gr = new Classification();
		gr.setDescription(description);

		if (effort != null) {
			StoryPointEffort hours = new StoryPointEffort();
			hours.setHours(effort);
			gr.setEffort(hours);
		}

		file.getDecorations().add(gr);
	}
}
