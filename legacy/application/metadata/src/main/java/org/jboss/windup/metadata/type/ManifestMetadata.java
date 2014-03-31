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
package org.jboss.windup.metadata.type;

import java.io.FileInputStream;
import java.lang.ref.SoftReference;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.Summary;
import org.jboss.windup.metadata.decoration.AbstractDecoration.NotificationLevel;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;


public class ManifestMetadata extends FileMetadata {
	private static final Log LOG = LogFactory.getLog(ManifestMetadata.class);
	protected SoftReference<Manifest> manifest;

	public Manifest getManifest() {
		if (manifest == null) {
			// create it.
			try {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Reading manifest: " + filePointer.getAbsolutePath());
				}
				Manifest mTmp = new Manifest(new FileInputStream(filePointer));
				manifest = new SoftReference<Manifest>(mTmp);
				LOG.debug("Reading manifest complete.");
			}
			catch (Exception e) {
				LOG.error("Bad Manifest? " + filePointer.getAbsolutePath());
				LOG.info("Skipping file. Continuing Windup Processing...");

				Summary sr = new Summary();
				sr.setDescription("Bad Manifest? Unable to parse.");
				sr.setLevel(NotificationLevel.CRITICAL);
				sr.setEffort(new UnknownEffort());
				this.getDecorations().add(sr);
				this.manifest = null;
			}
		}
		if (manifest == null) {
			return null;
		}
		return manifest.get();
	}
}
