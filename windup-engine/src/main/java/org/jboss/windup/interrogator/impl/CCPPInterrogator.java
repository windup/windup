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
package org.jboss.windup.interrogator.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.jboss.windup.resource.type.CCPPMeta;
import org.jboss.windup.resource.type.FileMeta;
import org.jboss.windup.resource.type.JavaMeta;
import org.jboss.windup.resource.type.ZipEntryMeta;
import org.jboss.windup.util.BlacklistPackageResolver;
import org.jboss.windup.util.CustomerPackageResolver;


/**
 * @author chale
 * 
 */
public class CCPPInterrogator extends ExtensionInterrogator<CCPPMeta> {
	private static final Log LOG = LogFactory.getLog(CCPPInterrogator.class);


	@Override
	public CCPPMeta archiveEntryToMeta(ZipEntryMeta archiveEntry) {
		File file = archiveEntry.getFilePointer();
		
		CCPPMeta meta = new CCPPMeta();
		meta.setArchiveMeta(archiveEntry.getArchiveMeta());
		meta.setFilePointer(file);
		populateMeta(meta);

		if(LOG.isDebugEnabled()) {
			LOG.debug("Processing: " + file.getAbsolutePath());			
		}
			
		return meta;
	}

	@Override
	public CCPPMeta fileEntryToMeta(FileMeta entry) {
		CCPPMeta meta = new CCPPMeta();
		meta.setFilePointer(entry.getFilePointer());
		meta.setArchiveMeta(entry.getArchiveMeta());
		
		populateMeta(meta);
		
		return meta;
	}
	
	public void populateMeta(CCPPMeta meta) {
		//What to do here, if anything?
	}
	

}
