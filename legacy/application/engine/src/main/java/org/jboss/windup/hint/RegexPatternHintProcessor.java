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

import java.util.regex.Pattern;

import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.AbstractDecoration.NotificationLevel;
import org.jboss.windup.metadata.decoration.effort.Effort;
import org.jboss.windup.metadata.decoration.effort.StoryPointEffort;
import org.jboss.windup.metadata.decoration.hint.MarkdownHint;


public class RegexPatternHintProcessor implements ResultProcessor {
	protected Pattern regexPattern;
	protected String hint;
	protected NotificationLevel notificationLevel;
	protected Effort effort;

	public void setNotificationLevel(String notificationLevel) {
		this.notificationLevel = NotificationLevel.valueOf(notificationLevel);
	}

	public void setEffort(int effort) {
		this.effort = new StoryPointEffort(effort);
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public String getHint() {
		return hint;
	}

	public void setRegexPattern(Pattern regexPattern) {
		this.regexPattern = regexPattern;
	}

	@Override
	public void process(AbstractDecoration result) {
		if (regexPattern.matcher(result.getPattern()).find()) {
			MarkdownHint sh = new MarkdownHint();
			sh.setMarkdown(hint);
			
			result.getHints().add(sh);
			if (notificationLevel != null) {
				result.setLevel(notificationLevel);
			}
			if (effort != null) {
				result.setEffort(effort);
			}
		}
	}
}
