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

import org.jboss.windup.hint.RegexPatternHintProcessor;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.JavaLine;
import org.jboss.windup.metadata.decoration.SourceType;

public class JavaPatternHintProcessor extends RegexPatternHintProcessor {
	private SourceType sourceType;

	public void setSourceType(String sourceType) {
		this.sourceType = SourceType.valueOf(sourceType);
	}

	@Override
	public void process(AbstractDecoration result) {
		if (result == null || !(result instanceof JavaLine)) {
			// not of interest.
			return;
		}
		JavaLine javaLine = (JavaLine) result;

		// if the source type is not provided, apply to all source types.
		if (sourceType != null) {
			if (javaLine.getSourceType() != sourceType) {
				// not of interest.
				return;
			}
		}
		super.process(result);
	}
}
