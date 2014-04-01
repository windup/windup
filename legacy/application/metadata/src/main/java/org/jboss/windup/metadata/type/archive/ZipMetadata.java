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
package org.jboss.windup.metadata.type.archive;

import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.Summary;
import org.jboss.windup.metadata.decoration.AbstractDecoration.NotificationLevel;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;

public class ZipMetadata extends ArchiveMetadata {
	private static final Log LOG = LogFactory.getLog(ZipMetadata.class);
	
	protected ZipFile zip;
	
	public ZipFile getZipFile() {
		if (zip != null) {
			return zip;
		}
		// otherwise, hydrate the zip.
		if (this.filePointer != null) {
			try {
				zip = new ZipFile(this.filePointer);
			}
			catch (Exception e) {
				LOG.error("Bad Zip? " + filePointer.getAbsolutePath());
				LOG.info("Skipping file. Continuing Windup Processing...");

				Summary sr = new Summary();
				sr.setDescription("Bad Zip? Unable to parse.");
				sr.setLevel(NotificationLevel.CRITICAL);
				sr.setEffort(new UnknownEffort());
				this.getDecorations().add(sr);
				this.zip = null;
			}

		}

		return zip;
	}
}
