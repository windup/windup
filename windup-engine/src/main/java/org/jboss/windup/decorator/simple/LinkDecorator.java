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
import org.jboss.windup.metadata.decoration.Link;
import org.jboss.windup.metadata.type.FileMetadata;

public class LinkDecorator implements MetaDecorator<FileMetadata> {
	protected String link;
	protected String description;

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void processMeta(FileMetadata file) {
		Link lr = new Link();
		lr.setDescription(description);
		lr.setLink(link);

		file.getDecorations().add(lr);
	}
}
