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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.effort.Effort;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;
import org.jboss.windup.metadata.type.ResourceMetadata;


public abstract class ChainingDecorator<T extends ResourceMetadata> implements MetaDecorator<T> {
	private static final Log LOG = LogFactory.getLog(ChainingDecorator.class);

	protected List<MetaDecorator<T>> decorators;
	protected Effort effort = new UnknownEffort();

	public void setEffort(Effort effort) {
		this.effort = effort;
	}

	public void setDecorators(List<MetaDecorator<T>> decorators) {
		this.decorators = decorators;
	}

	public void addDecorators(List<MetaDecorator<T>> decorators) {
		this.decorators.addAll(decorators);
	}

	protected void chainDecorators(T meta) {
		if (decorators != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Decorators not null.  Running chained decorators: " + decorators.size());
			}
			for (MetaDecorator<T> decorator : decorators) {
				decorator.processMeta(meta);
			}
		}
	}
}
