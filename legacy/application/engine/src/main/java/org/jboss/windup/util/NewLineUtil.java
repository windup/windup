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
package org.jboss.windup.util;

import org.apache.commons.lang.StringUtils;

public class NewLineUtil {
	private NewLineUtil() {// seal

	}

	public static int countNewLine(String html, int charPosition) {
		String subString = StringUtils.substring(html, 0, charPosition + 1);
		String[] lines = subString.split("\r\n|\r|\n");
		return lines.length;
	}
}
