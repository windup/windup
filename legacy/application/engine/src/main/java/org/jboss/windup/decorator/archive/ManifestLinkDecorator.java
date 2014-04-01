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
import org.jboss.windup.metadata.decoration.Link;
import org.jboss.windup.metadata.type.ManifestMetadata;


public class ManifestLinkDecorator extends ManifestDecorator {

	protected List<String> linkPriority;

	public void setLinkPriority(List<String> linkPriority) {
		this.linkPriority = linkPriority;
	}

	@Override
	public void processMeta(ManifestMetadata file) {
		// Look for default description information.
		String link = extractValue(file.getManifest(), linkPriority);

		if (link == null) {
			return;
		}

		// if it exists, cleanse it.
		link = cleanseValue(link);

		if (StringUtils.isNotBlank(link)) {
			Link vr = new Link();
			vr.setDescription("Project Site");
			vr.setLink(link);

			file.getArchiveMeta().getDecorations().add(vr);
			return;
		}
	}

}
