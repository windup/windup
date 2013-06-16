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
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.metadata.type.ManifestMetadata;


public abstract class ManifestDecorator implements MetaDecorator<ManifestMetadata> {

	protected String extractValue(Manifest mf, List<String> priority) {
		String val = findValueInAttribute(mf.getMainAttributes(), priority);
		if (StringUtils.isNotBlank(val)) {
			return val;
		}
		// prepare the fallback attributes, ordered by name...
		List<String> attributeNames = new ArrayList<String>(mf.getEntries().keySet());
		Collections.sort(attributeNames);

		for (String attributeName : attributeNames) {
			val = findValueInAttribute(mf.getAttributes(attributeName), priority);

			if (StringUtils.isNotBlank(val)) {
				return val;
			}
		}

		return null;
	}

	protected String cleanseValue(String value) {
		value = StringUtils.trim(value);
		value = StringUtils.removeStart(value, "'");
		value = StringUtils.removeStart(value, "\"");
		value = StringUtils.removeEnd(value, "\"");
		value = StringUtils.removeEnd(value, "'");
		value = StringUtils.trim(value);

		return value;
	}

	private String findValueInAttribute(Attributes attribute, List<String> priority) {
		if (priority != null) {
			for (String key : priority) {
				String val = attribute.getValue(key);
				if (StringUtils.isNotBlank(val)) {
					return val;
				}
			}
		}
		return null;
	}

}
