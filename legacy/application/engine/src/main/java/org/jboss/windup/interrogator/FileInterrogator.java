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

import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.ResourceMetadata;

/**
 * <p>
 * Defines a file interrogator for a specific type of {@link ResourceMetadata} data.
 * </p>
 * 
 * @param <T> the type of {@link ResourceMetadata} this {@link FileInterrogator} is for
 */
public interface FileInterrogator<T extends ResourceMetadata> {
	public abstract void processFile(FileMetadata entry);
	public abstract T fileEntryToMeta(FileMetadata entry);
}
