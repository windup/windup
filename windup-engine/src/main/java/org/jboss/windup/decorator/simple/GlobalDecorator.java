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
import org.jboss.windup.resource.decoration.Global;
import org.jboss.windup.resource.type.FileMeta;

public class GlobalDecorator implements MetaDecorator<FileMeta> {
	protected String description;

	public void setDescription(String description) {
		this.description = description;
	}

	public void processMeta(FileMeta file) {
		Global gr = new Global();
		gr.setDescription(description);

		file.getDecorations().add(gr);
	}
}
