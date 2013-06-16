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
package org.jboss.windup.interrogator.impl;

import org.jboss.windup.decorator.ChainingDecorator;
import org.jboss.windup.metadata.type.ResourceMetadata;

public class DecoratorPipeline<T extends ResourceMetadata> extends ChainingDecorator<T> {

	@Override
	public void processMeta(T meta) {
		chainDecorators(meta);
	}
	
}
