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
import org.jboss.windup.resource.decoration.Link;
import org.jboss.windup.resource.type.FileMeta;

public class LinkDecorator implements MetaDecorator<FileMeta> {
	protected String link;
	protected String description;

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void processMeta(FileMeta file) {
		Link lr = new Link();
		lr.setDescription(description);
		lr.setLink(link);

		file.getDecorations().add(lr);
	}
}
