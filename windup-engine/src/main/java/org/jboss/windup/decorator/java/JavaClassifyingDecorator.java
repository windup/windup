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
package org.jboss.windup.decorator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.gate.JavaPatternGateDecorator;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.type.FileMetadata;


public class JavaClassifyingDecorator extends JavaPatternGateDecorator {
	private static final Log LOG = LogFactory.getLog(JavaClassifyingDecorator.class);

	private String matchDescription;

	public void setMatchDescription(String matchDescription) {
		this.matchDescription = matchDescription;
	}

	public void setRegexPattern(Pattern regexPattern) {
		this.regexPattern = regexPattern;
	}

	@Override
	protected List<AbstractDecoration> matchResults(FileMetadata meta) {
		// only process classification.
		List<AbstractDecoration> matched = super.matchResults(meta);

		List<AbstractDecoration> results = new ArrayList<AbstractDecoration>(1);
		if (matched != null && matched.size() > 0) {
			// create classification, with the same pattern.
			Classification cr = new Classification();
			cr.setDescription(matchDescription);
			cr.setEffort(effort);
			cr.setPattern(matched.get(0).getPattern());

			meta.getDecorations().add(cr);
			results.add(cr);
		}

		return results;
	}
}
