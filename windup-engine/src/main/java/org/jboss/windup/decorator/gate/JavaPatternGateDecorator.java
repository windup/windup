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
package org.jboss.windup.decorator.gate;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.JavaLine;
import org.jboss.windup.metadata.decoration.SourceType;
import org.jboss.windup.metadata.type.FileMetadata;


public class JavaPatternGateDecorator extends RegexPatternGateProcessor {
	protected Pattern regexPattern;
	private SourceType sourceType;

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	public void setRegexPattern(Pattern regexPattern) {
		this.regexPattern = regexPattern;
	}

	@Override
	protected List<AbstractDecoration> matchResults(FileMetadata meta) {
		List<AbstractDecoration> results = new LinkedList<AbstractDecoration>();

		for (AbstractDecoration dr : meta.getDecorations()) {
			if (dr instanceof JavaLine) {
				JavaLine result = (JavaLine) dr;

				if (sourceType != null) {
					if (result.getSourceType() != sourceType) {
						// not of interest.
						continue;
					}
				}

				if (regexPattern.matcher(dr.getPattern()).find()) {
					results.add(dr);
				}
			}
		}
		return results;
	}
}
