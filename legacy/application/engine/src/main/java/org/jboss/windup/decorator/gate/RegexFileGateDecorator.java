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

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.metadata.type.FileMetadata;


/**
 * Used to filter the decorators down a decorator chain.
 * 
 * An example would be if you want to decorate web.xml only as a WAR Application Descriptor; front the XPath decorator with this decorator.
 * 
 * @author bdavis@redhat.com
 * 
 */
public class RegexFileGateDecorator extends GateDecorator<FileMetadata> {
	protected Pattern regexPattern;
	protected boolean fullPath;

	public void setRegexPattern(Pattern regexPattern) {
		this.regexPattern = regexPattern;
	}

	public void setFullPath(boolean fullPath) {
		this.fullPath = fullPath;
	}

	@Override
	protected boolean continueProcessing(FileMetadata meta) {
		String fileNameText = null;
		if (fullPath) {
			fileNameText = meta.getFilePointer().getAbsolutePath();
			fileNameText = StringUtils.replace(fileNameText, "\\", "/");
		}
		else {
			fileNameText = meta.getFilePointer().getName();
		}

		return regexPattern.matcher(fileNameText).find();
	}

}
