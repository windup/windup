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

import org.jboss.windup.hint.ResultProcessor;
import org.jboss.windup.resource.decoration.AbstractDecoration;
import org.jboss.windup.resource.type.FileMeta;


public class RegexPatternGateProcessor extends GateDecorator<FileMeta> {
	protected Pattern regexPattern;
	protected List<ResultProcessor> hints = new LinkedList<ResultProcessor>();

	public void setHints(List<ResultProcessor> hints) {
		this.hints = hints;
	}

	public void setRegexPattern(Pattern regexPattern) {
		this.regexPattern = regexPattern;
	}

	protected List<AbstractDecoration> matchResults(FileMeta meta) {
		List<AbstractDecoration> results = new LinkedList<AbstractDecoration>();

		for (AbstractDecoration rst : meta.getDecorations()) {
			if (regexPattern.matcher(rst.getPattern()).find()) {
				results.add(rst);
			}
		}
		return results;
	}

	@Override
	protected boolean continueProcessing(FileMeta meta) {
		List<AbstractDecoration> results = matchResults(meta);
		if (results != null && results.size() > 0) {
			if (hints != null) {
				for (ResultProcessor hintProcessor : hints) {
					for (AbstractDecoration result : results) {
						hintProcessor.process(result);
					}
				}
			}
			return true;
		}

		return false;
	}
}
