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
package org.jboss.windup.resource.type;

import org.eclipse.cdt.core.parser.FileContent;

/**
 * Extends the FileMeta for keeping track of C and C++ files.
 * 
 * @author chale
 * 
 */
public class CCPPMeta extends FileMeta {
	public FileContent getFileContent() {
		return FileContent.createForExternalFileLocation(getFilePointer().getAbsolutePath());
	}
}
