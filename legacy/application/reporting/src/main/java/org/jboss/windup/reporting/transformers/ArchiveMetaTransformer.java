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

import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.html.ArchiveReport;
import org.jboss.windup.reporting.html.ResourceReport;

public class ArchiveMetaTransformer extends GenericMetaTransformer<FileMetadata> {

	@Override
	public ResourceReport toResourceReport(FileMetadata meta, File reportDirectory,
			ArchiveReport parent) {

		ArchiveReport report = new ArchiveReport();
		populateResourceData(meta, reportDirectory, report);
		report.setTitle(report.getRelativePathFromRoot());

		ArchiveMetadata am = (ArchiveMetadata)meta;
		report.setVendorResult(buildVendorResult(am));
		report.setRelativePathFromRoot(am.getRelativePath());
		return report;
	}
	
	@Override
	protected String buildSyntax() {
		return null;
	}

}
