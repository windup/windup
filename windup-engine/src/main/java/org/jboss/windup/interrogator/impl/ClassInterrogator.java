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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javassist.ClassPool;
import javassist.CtClass;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.java.decompiler.DecompilerAdapter;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.JavaMetadata;
import org.jboss.windup.metadata.type.ZipEntryMetadata;
import org.jboss.windup.metadata.type.archive.ZipMetadata;
import org.jboss.windup.util.BlacklistPackageResolver;
import org.jboss.windup.util.CustomerPackageResolver;
import org.jboss.windup.util.FatalWindupException;


public class ClassInterrogator extends ExtensionInterrogator<JavaMetadata> {
	private static final Log LOG = LogFactory.getLog(ClassInterrogator.class);
	private DecompilerAdapter decompiler;

	private BlacklistPackageResolver blacklistPackageResolver;
	private CustomerPackageResolver customerPackageResolver;

	public void setBlacklistPackageResolver(BlacklistPackageResolver blacklistPackageResolver) {
		this.blacklistPackageResolver = blacklistPackageResolver;
	}
	
	public void setDecompiler(DecompilerAdapter decompiler) {
		this.decompiler = decompiler;
	}

	public void setCustomerPackageResolver(CustomerPackageResolver customerPackageResolver) {
		this.customerPackageResolver = customerPackageResolver;
	}

	@Override
	public JavaMetadata archiveEntryToMeta(ZipEntryMetadata archiveEntry) {
		String className = extractClassName(archiveEntry.getZipEntry().getName());
		if (LOG.isDebugEnabled()) {
			LOG.debug("Class: " + className + " " + customerPackageResolver.isCustomerPkg(className));
		}

		// first, is this a customer's package?
		if (!customerPackageResolver.isCustomerPkg(className)) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Class: " + className + " is not a customer package.");
			}
			return null;
		}

		// extract imports if it is a customer class.
		Set<String> clzImports = extractImports(archiveEntry);

		// second, is this generated?
		if (blacklistPackageResolver.containsGenerated(clzImports)) {
			LOG.trace("Class is generated.  Skip profiling.");
			return null;
		}

		// third does the class contain any blacklists?
		if (!blacklistPackageResolver.containsBlacklist(clzImports)) {
			LOG.trace("Class does not contain blacklists.");
			return null;
		}

		/*
		 * ok; it contains blacklists... we need to process it. get the Java for the class by either
		 * 1) finding it in the Zip archive... or
		 * 2) decompiling.
		 */
		return extractJavaFile(className, clzImports, archiveEntry);
	}

	private JavaMetadata extractJavaFile(String className, Set<String> clzImports, ZipEntryMetadata archiveEntry) {
		JavaMetadata javaMeta = new JavaMetadata();
		try {
			File clzFile;
			File javaFile;

			//TODO: make this work for directorymeta too.
			ZipMetadata zipMeta = (ZipMetadata)archiveEntry.getArchiveMeta();
			ZipFile zipFile = zipMeta.getZipFile();
			ZipEntry entry = archiveEntry.getZipEntry();
			// check to see whether the Java version is packaged with the archive...
			String javaZipEntry = StringUtils.removeEnd(entry.getName(), ".class") + ".java";

			if (zipFile.getEntry(javaZipEntry) != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found Java in archive: " + className);
				}
				ZipEntry javaEntry = zipFile.getEntry(javaZipEntry);
				archiveEntry.setZipEntry(javaEntry);
				javaFile = archiveEntry.getFilePointer();
			}
			else {
				clzFile = archiveEntry.getFilePointer();
				javaFile = new File(StringUtils.substringBeforeLast(clzFile.getAbsolutePath(), ".class") + ".java");
				File javaPathFile = new File(StringUtils.substringBeforeLast(clzFile.getAbsolutePath(), File.separator));

				if (LOG.isDebugEnabled()) {
					LOG.debug("Did not find class in archive. Decompiling class: " + className);
				}
				decompiler.decompile(className, clzFile, javaPathFile);

				if (LOG.isDebugEnabled()) {
					LOG.debug("Unzipped class: " + className);
				}
			}

			javaMeta.setFilePointer(javaFile);
			javaMeta.setArchiveMeta(archiveEntry.getArchiveMeta());
			javaMeta.setClassDependencies(clzImports);
			javaMeta.setQualifiedClassName(className);
			javaMeta.setBlackListedDependencies(blacklistPackageResolver.extractBlacklist(clzImports));

			return javaMeta;
		}
		catch (Exception e) {
			if(e instanceof FatalWindupException) {
				throw (FatalWindupException)e;
			}
			
			LOG.error(e);
			return null;
		}
	}

	protected Set<String> extractImports(ZipEntryMetadata archiveEntry) {
		try {
			// otherwise, load the class and get its imports.

			//TODO: make this work for directorymeta too.
			ZipMetadata zipMeta = (ZipMetadata)archiveEntry.getArchiveMeta();
			ZipFile zipFile = zipMeta.getZipFile();
			ZipEntry entry = archiveEntry.getZipEntry();
			CtClass ctClz = new ClassPool().makeClass(zipFile.getInputStream(entry));
			if (LOG.isDebugEnabled()) {
				for (String clz : ctClz.getClassFile2().getInterfaces()) {
					LOG.debug("Interfaces: " + clz);
				}
				LOG.debug("Super Class: " + ctClz.getClassFile2().getSuperclass());
			}
			Set<String> clzImports = new HashSet<String>(castList(String.class, ctClz.getRefClasses()));
			return clzImports;
		}
		catch (Exception e) {
			LOG.error(e);
			return null;
		}
	}

	public static <T> List<T> castList(Class<? extends T> aclass, Collection<?> c) {
		List<T> r = new ArrayList<T>(c.size());
		for (Object o : c) {
			r.add(aclass.cast(o));
		}
		return r;
	}

	protected String extractClassName(String entryName) {
		String className = StringUtils.replace(entryName, "\\", "/");
		className = StringUtils.removeStart(className, "/");
		className = StringUtils.replace(className, "/", ".");
		className = StringUtils.removeEnd(className, ".class");
		className = StringUtils.removeEnd(className, ".java");
		className = StringUtils.substringBefore(className, "$");

		// account for WAR classes.
		if (StringUtils.contains(className, "WEB-INF.classes.")) {
			className = StringUtils.substringAfter(className, "WEB-INF.classes.");
		}
		return className;
	}

	@Override
	public JavaMetadata fileEntryToMeta(FileMetadata entry) {
		JavaMetadata javaMeta = new JavaMetadata();
		javaMeta.setArchiveMeta(entry.getArchiveMeta());
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(entry.getFilePointer());
			javaMeta.setFilePointer(entry.getFilePointer());
			//javaMeta.setArchiveMeta(archiveEntry.getArchiveMeta());
			CtClass ctClazz = new ClassPool().makeClass(fis);

			String className = ctClazz.getName();
			if (!customerPackageResolver.isCustomerPkg(className)) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Class: " + className + " is not a customer package.");
				}
				return null;
			}
			
			Set<String> clzImports = new HashSet<String>(castList(String.class, ctClazz.getRefClasses()));

			// second, is this generated?
			if (blacklistPackageResolver.containsGenerated(clzImports)) {
				LOG.trace("Class is generated.  Skip profiling.");
				return null;
			}

			// third does the class contain any blacklists?
			if (!blacklistPackageResolver.containsBlacklist(clzImports)) {
				LOG.trace("Class does not contain blacklists.");
				return null;
			}
			
			javaMeta.setClassDependencies(clzImports);
			javaMeta.setQualifiedClassName(ctClazz.getName());
			javaMeta.setBlackListedDependencies(blacklistPackageResolver.extractBlacklist(clzImports));
			
			return javaMeta;
		}
		catch(Exception e) {
			LOG.error("Exception processing file: "+entry.getFilePointer().getAbsolutePath(), e);
		}
		finally { 
			org.apache.commons.io.IOUtils.closeQuietly(fis);
		}
		
		return null;
	}
}
