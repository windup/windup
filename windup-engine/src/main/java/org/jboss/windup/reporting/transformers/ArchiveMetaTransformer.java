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

import org.jboss.windup.reporting.html.ArchiveReport;
import org.jboss.windup.reporting.html.ResourceReport;
import org.jboss.windup.resource.type.FileMeta;
import org.jboss.windup.resource.type.archive.ArchiveMeta;

public class ArchiveMetaTransformer extends GenericMetaTransformer<FileMeta> {

	@Override
	public ResourceReport toResourceReport(FileMeta meta, File reportDirectory,
			ArchiveReport parent) {

		ArchiveReport report = new ArchiveReport();
		populateResourceData(meta, reportDirectory, report);
		report.setTitle(report.getRelativePathFromRoot());

		ArchiveMeta am = (ArchiveMeta)meta;
		report.setVendorResult(buildVendorResult(am));
		report.setRelativePathFromRoot(am.getRelativePath());
		return report;
	}
	
	@Override
	protected String buildSyntax() {
		return null;
	}

}
