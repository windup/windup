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

import org.jboss.windup.metadata.type.ResourceMetadata;
import org.jboss.windup.metadata.type.ZipEntryMetadata;

/**
 * An Interrogator is responsible for extracting an element of interest from the ArchiveMetadata.
 * It then executes it's decorators, and is responsible for returning 0 ... n InterrogationReports
 * for the items of interest extracted from the ArchiveMetadata.
 * 
 * @author bdavis
 * 
 * @param <T>
 */
public interface ZipEntryInterrogator<T extends ResourceMetadata> {

	/**
	 * Processes a ZipEntry; if the ZipEntry meets the criteria of the interrogator, the Interrogator can return 0 ... N InterrogationReports,
	 * which render to the WindupReport overview.
	 * 
	 * @param zipFile
	 * @param entry
	 * @param classDependencies
	 * @param reportDirectory
	 * @param archiveOutputDirectory
	 * @return 0 ... n InterrogationReports, which are rendered to the WindupReport's overview report.
	 */
	public abstract void processArchiveEntry(ZipEntryMetadata archiveEntry);
	public abstract T archiveEntryToMeta(ZipEntryMetadata archiveEntry); 
}
