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
package org.jboss.windup.decorator.archive;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.metadata.decoration.Summary;
import org.jboss.windup.metadata.type.ManifestMetadata;


public class ManifestDescriptionDecorator extends ManifestDecorator {

	protected List<String> descriptionPriority;

	public void setDescriptionPriority(List<String> descriptionPriority) {
		this.descriptionPriority = descriptionPriority;
	}

	@Override
	public void processMeta(ManifestMetadata file) {
		// Look for default description information.
		String description = extractValue(file.getManifest(), descriptionPriority);

		if (description == null) {
			return;
		}

		// if it exists, cleanse it.
		description = cleanseValue(description);

		if (StringUtils.isNotBlank(description)) {
			Summary vr = new Summary();
			vr.setDescription(description);

			file.getArchiveMeta().getDecorations().add(vr);
			return;
		}
	}

}
