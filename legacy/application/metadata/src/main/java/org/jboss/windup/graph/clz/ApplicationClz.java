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
package org.jboss.windup.graph.clz;

import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.metadata.type.archive.ArchiveMetadata;

public class ApplicationClz extends GraphableClz {

	private ArchiveMetadata archive;
	private Set<GraphableClz> dependsOn = new HashSet<GraphableClz>();
	private Set<String> unresolvedDependencies = new HashSet<String>();
	
	public ApplicationClz(ArchiveMetadata archive, String className, Set<String> unresolvedDependencies) {
		super(className);
		this.archive = archive;
		
		if(unresolvedDependencies != null) {
			this.unresolvedDependencies = unresolvedDependencies;
		}
	}

	public ArchiveMetadata getArchive() {
		return archive;
	}
	
	public Set<String> getUnresolvedDependencies() {
		return unresolvedDependencies;
	}
	
	public Set<GraphableClz> getDependsOn() {
		return dependsOn;
	}
}
