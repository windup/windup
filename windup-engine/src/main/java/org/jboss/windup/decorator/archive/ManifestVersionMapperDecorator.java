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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class ManifestVersionMapperDecorator extends ManifestVersionDecorator {

	private static final Log LOG = LogFactory.getLog(ManifestVersionDecorator.class);

	private String matchName;
	private String matchVersion;

	public void setMatchVersion(String matchVersion) {
		this.matchVersion = matchVersion;
	}

	public void setMatchName(String matchName) {
		this.matchName = matchName;
	}

	private Map<String, Pattern> attributeNameToRegex = new HashMap<String, Pattern>(0);

	public void setAttributeNameToRegex(Map<String, Pattern> attributeNameToRegex) {
		this.attributeNameToRegex = attributeNameToRegex;
	}

	@Override
	public void processMeta(ManifestMetadata file) {
		if (matchesAll(file.getManifest())) {
			Version vr = new Version();

			if (StringUtils.isNotBlank(matchName)) {
				vr.setName(matchName);
			}
			else {
				String name = extractValue(file.getManifest(), namePriority);
				name = cleanseValue(name);

				vr.setName(name);
			}

			if (StringUtils.isNotBlank(matchVersion)) {
				vr.setVersion(matchVersion);
			}
			else {
				String version = extractValue(file.getManifest(), versionPriority);
				version = cleanseValue(version);

				vr.setVersion(version);
			}
			file.getArchiveMeta().getDecorations().add(vr);
		}
	}

	protected boolean matchesAll(Manifest mf) {
		boolean matched = false;

		// check the main attributes....
		matched = matchAttributes(mf.getMainAttributes());

		// if this doesn't match the main attributes, go through attribute groups trying to match.
		if (!matched) {
			// prepare the fallback attributes, ordered by name...
			List<String> attributeNames = new ArrayList<String>(mf.getEntries().keySet());
			Collections.sort(attributeNames);

			for (String attributeName : attributeNames) {
				matched = matchAttributes(mf.getAttributes(attributeName));

				if (matched) {
					return true;
				}
			}
		}

		return matched;
	}

	private boolean matchAttributes(Attributes attributes) {
		boolean matched = true;
		for (String key : attributeNameToRegex.keySet()) {

			// if it looks for the attribute in the attributes set
			// and it does not exist, return false.
			String attrVal = attributes.getValue(key);
			if (attrVal == null) {
				return false;
			}
			else {
				// else if it found the attribute, check if it matches a given regex pattern.
				matched = processPattern(attributeNameToRegex.get(key), attrVal);

				if (LOG.isTraceEnabled()) {
					LOG.trace("Key[" + key + "] Found: " + attrVal + " trying to match: " + attributeNameToRegex.get(key) + " :: " + matched);
				}
			}

			// if they become false at any point, return false.
			if (!matched) {
				return false;
			}
		}
		return matched;
	}

	protected boolean processPattern(Pattern pattern, String val) {
		if (pattern != null && val != null) {
			Matcher matcher = pattern.matcher(val);
			return matcher.find();
		}
		return false;
	}

}
