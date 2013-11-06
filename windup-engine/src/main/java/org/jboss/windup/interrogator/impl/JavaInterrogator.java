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
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.JavaMetadata;
import org.jboss.windup.metadata.type.ZipEntryMetadata;
import org.jboss.windup.util.BlacklistPackageResolver;
import org.jboss.windup.util.CustomerPackageResolver;


/**
 * Runs through all of the archives, comparing against the blacklist. If the class matches, looks for the source.
 * If the source does not exist, this interrogator will decompile the Class file into a Java file
 * Creates a JavaMetadata object, and passes it down the decorator pipeline.
 * 
 * @author bdavis
 * 
 */
public class JavaInterrogator extends ExtensionInterrogator<JavaMetadata> {
	private static final Log LOG = LogFactory.getLog(JavaInterrogator.class);

	private BlacklistPackageResolver blacklistPackageResolver;
	private CustomerPackageResolver customerPackageResolver;

	public void setBlacklistPackageResolver(
			BlacklistPackageResolver blacklistPackageResolver) {
		this.blacklistPackageResolver = blacklistPackageResolver;
	}
	
	public void setCustomerPackageResolver(CustomerPackageResolver customerPackageResolver) {
		this.customerPackageResolver = customerPackageResolver;
	}

	@Override
	public JavaMetadata archiveEntryToMeta(ZipEntryMetadata archiveEntry) {
		File file = archiveEntry.getFilePointer();
		
		JavaMetadata meta = new JavaMetadata();
		meta.setArchiveMeta(archiveEntry.getArchiveMeta());
		meta.setFilePointer(file);
		populateMeta(meta);

		if(LOG.isDebugEnabled()) {
			LOG.debug("Processing: " + file.getAbsolutePath());
			LOG.debug("Processing: " + meta.getQualifiedClassName());
			
		}
		
		if (!customerPackageResolver.isCustomerPkg(meta.getQualifiedClassName())) {
			LOG.trace("Not customer type: "+meta.getQualifiedClassName());
			return null;
		}
		if (blacklistPackageResolver.containsGenerated(meta.getClassDependencies())) {
			LOG.trace("Class is generated.  Skip profiling.");
			return null;
		}

		// third does the class contain any blacklists?
		if (!blacklistPackageResolver.containsBlacklist(meta.getClassDependencies())) {
			LOG.trace("Class does not contain blacklists.");
			return null;
		}
		
		return meta;
	}

	@Override
	public JavaMetadata fileEntryToMeta(FileMetadata entry) {
		JavaMetadata meta = new JavaMetadata();
		meta.setFilePointer(entry.getFilePointer());
		meta.setArchiveMeta(entry.getArchiveMeta());
		
		populateMeta(meta);
		
		if (!customerPackageResolver.isCustomerPkg(meta.getQualifiedClassName())) {
			LOG.trace("Not customer package.");
			return null;
		}
		if (blacklistPackageResolver.containsGenerated(meta.getClassDependencies())) {
			LOG.trace("Class is generated.  Skip profiling.");
			return null;
		}

		// third does the class contain any blacklists?
		if (!blacklistPackageResolver.containsBlacklist(meta.getClassDependencies())) {
			LOG.trace("Class does not contain blacklists.");
			return null;
		}
		
		return meta;
	}
	
	public void populateMeta(JavaMetadata meta) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		try {
			parser.setSource(FileUtils.readFileToString(meta.getFilePointer()).toCharArray());
		}
		catch (IOException e) {
			LOG.error("Exception setting source for parser.", e);
			return;
		}
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		@SuppressWarnings("unchecked")
		List<ImportDeclaration> imports = cu.imports();
		Set<String> clzDependencies = new HashSet<String>();
		
		if(imports != null) {
			for(ImportDeclaration id : imports) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Import: "+id.getName());
				}
				clzDependencies.add(id.getName().getFullyQualifiedName());
			}
		}
		meta.setClassDependencies(clzDependencies);
		meta.setBlackListedDependencies(blacklistPackageResolver.extractBlacklist(clzDependencies));
		
		if(LOG.isDebugEnabled()) {
			for(String id : meta.getBlackListedDependencies()) {
				LOG.debug("Blacklist: "+id);
			}
		}
		
		@SuppressWarnings("unchecked")
		List<TypeDeclaration> types = cu.types();
		
		if(types != null) {
			for(TypeDeclaration type : types) {
				//if class is in package grab that
				PackageDeclaration packageDeclaration = cu.getPackage();
				String packageName = "";
				if(packageDeclaration != null) {
					packageName = cu.getPackage().getName().getFullyQualifiedName() + ".";
				}
				
				//generate full class name
				String fullPackage = packageName + type.getName().getFullyQualifiedName();
				meta.setQualifiedClassName(fullPackage);
				
				if(LOG.isDebugEnabled()) {
					LOG.debug("Full Package: "+fullPackage);
				}
				break;
			}
		}
		else {
			LOG.warn("Expected to find Java 'type' in fine: "+meta.getPathRelativeToArchive());
		}
	}
	

}
