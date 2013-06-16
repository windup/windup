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
package org.jboss.windup.hint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.AbstractDecoration;


public class RegexPatternWhitelistProcessor implements MatchingProcessor {
	private static final Log LOG = LogFactory.getLog(RegexPatternWhitelistProcessor.class);

	protected Pattern regexPattern;

	public void setRegexPattern(Pattern regexPattern) {
		this.regexPattern = regexPattern;
	}

	@Override
	public boolean process(AbstractDecoration result) {
		Matcher matcher = regexPattern.matcher(result.getPattern());
		while (matcher.find()) {
			LOG.debug("Matched: " + result.getPattern() + " as whitelist. ");
			return true;
		}

		return false;
	}
}
