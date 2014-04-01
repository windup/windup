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

import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.interrogator.impl.DecoratorPipeline;
import org.jboss.windup.metadata.type.ResourceMetadata;


public abstract class Interrogator<T extends ResourceMetadata> implements ZipEntryInterrogator<T>, FileInterrogator<T>, MetaDecorator<T> {

	protected DecoratorPipeline<T> decoratorPipeline;
	
	public void setDecoratorPipeline(DecoratorPipeline<T> decoratorPipeline) {
		this.decoratorPipeline = decoratorPipeline;
	}
	
}
