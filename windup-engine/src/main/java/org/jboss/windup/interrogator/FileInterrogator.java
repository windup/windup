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
package org.jboss.windup.interrogator;

import org.jboss.windup.resource.type.FileMeta;
import org.jboss.windup.resource.type.ResourceMeta;

/**
 * <p>
 * Defines a file interrogator for a specific type of {@link ResourceMeta} data.
 * </p>
 * 
 * @param <T> the type of {@link ResourceMeta} this {@link FileInterrogator} is for
 */
public interface FileInterrogator<T extends ResourceMeta> {
	public abstract void processFile(FileMeta entry);
	public abstract T fileEntryToMeta(FileMeta entry);
}
