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
package org.jboss.windup.decorator.gate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.ChainingDecorator;
import org.jboss.windup.metadata.type.FileMetadata;


public abstract class GateDecorator<T extends FileMetadata> extends ChainingDecorator<T> {
	private static final Log LOG = LogFactory.getLog(GateDecorator.class);
	private boolean negate;

	public void setNegate(boolean negate) {
		this.negate = negate;
	}

	protected abstract boolean continueProcessing(T meta);

	@Override
	public void processMeta(T meta) {
		if (continueProcessing(meta) == !negate) {
			LOG.debug("Chaining decorators...");
			// push it along the pipeline; otherwise, return without pushing it to children.
			chainDecorators(meta);
		}
	}
}
