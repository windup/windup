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
package org.jboss.windup.reporting.data;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;
import org.jboss.windup.reporting.transformers.MetaResultTransformResolver;
import org.jboss.windup.reporting.transformers.MetaResultTransformer;


public class ResourceDataReporter implements Reporter {
	private static final Log LOG = LogFactory.getLog(ResourceDataReporter.class);
	private MetaResultTransformResolver resolver;
	
	private ResourceDataMarshaller marshaller;
	
	public ResourceDataReporter() {
		marshaller = new ResourceDataMarshaller();
	}
	
	public void setResolver(MetaResultTransformResolver resolver) {
		this.resolver = resolver;
	}
	
	@Override
	public void process(ArchiveMetadata archive, File reportDirectory) {
		//for each resource, create and persist a serialized data object.
	 
		for(ArchiveMetadata archiveMeta : archive.getNestedArchives()) {
			//recurse.
			this.process(archiveMeta, reportDirectory);
		}
		  
		for(FileMetadata fileMeta : archive.getEntries()) {
			//convert file resource to meta object and persist.
			MetaResultTransformer transformer = resolver.resolveTransformer(fileMeta.getClass());
			ResourceData data = transformer.toResourceData(fileMeta, reportDirectory);

			File metaOut = new File(fileMeta.getFilePointer().getAbsolutePath() + ".windup.meta");
			try {
				marshaller.marshal(metaOut, data);
			} catch (IOException e) {
				LOG.error("Exception writing meta: "+metaOut.getAbsolutePath(), e);	
			}			
		}
	}
}
