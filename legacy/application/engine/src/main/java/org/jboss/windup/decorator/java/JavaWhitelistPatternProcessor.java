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

import org.jboss.windup.hint.RegexPatternWhitelistProcessor;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.JavaLine;
import org.jboss.windup.metadata.decoration.SourceType;

public class JavaWhitelistPatternProcessor extends RegexPatternWhitelistProcessor {
	protected SourceType sourceType;

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	@Override
	public boolean process(AbstractDecoration result) {
		if (result == null || !(result instanceof JavaLine)) {
			return false;
		}
		JavaLine javaLine = (JavaLine) result;

		// if the source type is not provided, apply to all source types.
		if (sourceType != null) {
			if (javaLine.getSourceType() != sourceType) {
				// not of interest.
				return false;
			}
		}

		return super.process(result);
	}
}
