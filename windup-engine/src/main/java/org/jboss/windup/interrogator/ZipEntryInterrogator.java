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

import org.jboss.windup.resource.type.ResourceMeta;
import org.jboss.windup.resource.type.ZipEntryMeta;

/**
 * An Interrogator is responsible for extracting an element of interest from the ArchiveMeta.
 * It then executes it's decorators, and is responsible for returning 0 ... n InterrogationReports
 * for the items of interest extracted from the ArchiveMeta.
 * 
 * @author bdavis
 * 
 * @param <T>
 */
public interface ZipEntryInterrogator<T extends ResourceMeta> {

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
	public abstract void processArchiveEntry(ZipEntryMeta archiveEntry);
	public abstract T archiveEntryToMeta(ZipEntryMeta archiveEntry); 
}
