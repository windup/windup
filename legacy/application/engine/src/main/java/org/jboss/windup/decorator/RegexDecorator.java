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
package org.jboss.windup.decorator;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.Line;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.util.NewLineUtil;


public class RegexDecorator extends ChainingDecorator<FileMetadata> {
	private static final Log LOG = LogFactory.getLog(RegexDecorator.class);

	protected Pattern regexPattern;

	public void setRegexPattern(Pattern regexPattern) {
		this.regexPattern = regexPattern;
	}

	@Override
	public void processMeta(FileMetadata meta) {
		try {
			String contents = FileUtils.readFileToString(meta.getFilePointer());
			Matcher matcher = regexPattern.matcher(contents);
			while (matcher.find()) {
				String matched = matcher.group(1);

				Line lr = new Line();
				lr.setDescription("Blacklist Namespace: " + matched);
				lr.setLineNumber(NewLineUtil.countNewLine(contents, matcher.start()));
				lr.setPattern(matched);

				meta.getDecorations().add(lr);
			}
		}
		catch (IOException e) {
			LOG.error("Exception reading content for: " + meta.getFilePointer().getAbsolutePath(), e);
		}
	}
}
