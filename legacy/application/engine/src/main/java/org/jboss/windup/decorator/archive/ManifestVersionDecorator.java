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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.archetype.version.PomVersion;
import org.jboss.windup.metadata.decoration.archetype.version.Version;
import org.jboss.windup.metadata.type.ManifestMetadata;


/**
 * Matches the fields provided in the attributeNameToRegex property.
 * If all provided properties match, it will take the matchName and matchVersion and create a Version,
 * attaching it to the manifest's archive.
 * 
 * @author bradsdavis
 * 
 */
public class ManifestVersionDecorator extends ManifestDecorator {

	private static final Log LOG = LogFactory.getLog(ManifestVersionDecorator.class);

	protected List<String> versionPriority;
	protected List<String> namePriority;

	public void setVersionPriority(List<String> versionPriority) {
		this.versionPriority = versionPriority;
	}

	public void setNamePriority(List<String> namePriority) {
		this.namePriority = namePriority;
	}

	@Override
	public void processMeta(ManifestMetadata file) {
		// check to see if a version already exists. Only process if no version exists..
		for (AbstractDecoration dr : file.getArchiveMeta().getDecorations()) {
			if (dr instanceof PomVersion) {
				LOG.debug("Already has version result: " + dr.toString());
				return;
			}
		}

		// Otherwise look for default version information.
		String name = null;
		String version = extractValue(file.getManifest(), versionPriority);
		version = cleanseValue(version);

		if (version != null) {
			name = extractValue(file.getManifest(), namePriority);
			name = cleanseValue(name);

			// otherwise, guess the name from the archive name...

			// first, look for "-[0-9]" ane substring before that.
			if (StringUtils.isBlank(name)) {
				name = extractNameFromArchiveName(file.getArchiveMeta().getName());
			}
		}

		if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(version)) {
			Version vr = new Version();
			vr.setName(name);
			vr.setVersion(version);

			file.getArchiveMeta().getDecorations().add(vr);
			return;
		}
	}

	private String extractNameFromArchiveName(String archiveName) {
		String name = archiveName;
		name = StringUtils.substringBefore(name, ".");

		// now, if there are numbers... strip the numbers from the end.
		StringBuilder nameBuilder = new StringBuilder();
		String[] nameArray = StringUtils.split(name, "-");
		for (String nameFrag : nameArray) {
			if (StringUtils.isNotBlank(nameFrag)) {
				// check to see if the fragment starts with alpha.
				if (!StringUtils.isNumeric(StringUtils.substring(nameFrag, 0, 1))) {
					nameBuilder.append(nameFrag).append("-");
				}
			}
		}
		name = nameBuilder.toString();
		if (StringUtils.endsWith(name, "-")) {
			name = StringUtils.substringBeforeLast(name, "-");
		}

		return name;
	}
}
