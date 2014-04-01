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
package org.jboss.windup.reporting.transformers;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.type.ResourceMetadata;
import org.jboss.windup.reporting.data.ResourceData;


public abstract class GenericMetaTransformer<T extends ResourceMetadata> extends MetaResultTransformer<T> {
	private static final Log LOG = LogFactory.getLog(GenericMetaTransformer.class);

	public ResourceData toMetaResult(T meta, File reportDirectory) {
		ResourceData data = super.toResourceData(meta, reportDirectory);
		return data;
	}
}
