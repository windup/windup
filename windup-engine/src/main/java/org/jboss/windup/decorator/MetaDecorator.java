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
package org.jboss.windup.decorator;

import org.jboss.windup.metadata.type.ResourceMetadata;

/*
 * Decorators are responsible for taking a FileMetadata or subclass and adding Decorations
 * 
 * @author bdavis
 * 
 * @param <T>
 */
public interface MetaDecorator<T extends ResourceMetadata> {
	public abstract void processMeta(T meta);
}
