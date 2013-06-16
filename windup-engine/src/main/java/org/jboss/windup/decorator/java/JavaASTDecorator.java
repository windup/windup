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
package org.jboss.windup.decorator.java;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.jboss.windup.decorator.ChainingDecorator;
import org.jboss.windup.metadata.type.JavaMetadata;


public class JavaASTDecorator extends ChainingDecorator<JavaMetadata> {
	private static final Log LOG = LogFactory.getLog(JavaASTDecorator.class);

	protected Set<String> javaLangDependencies;

	public void setJavaLangDependencies(Set<String> javaLangDependencies) {
		this.javaLangDependencies = javaLangDependencies;
	}

	@Override
	public void processMeta(final JavaMetadata meta) {
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

		Set<String> allDependencies = new HashSet<String>(meta.getClassDependencies().size());
		allDependencies.addAll(javaLangDependencies);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new JavaASTVariableResolvingVisitor(cu, meta.getDecorations(), meta.getQualifiedClassName(), allDependencies, meta.getBlackListedDependencies()));
		//TODO: finish. cu.accept(new JavaASTAnnotationVisitor(cu, meta.getDecorations(), allDependencies));
		
		for (Object o : cu.types()) {
			TypeDeclaration td = (TypeDeclaration) o;
			String qualifiedTemp = cu.getPackage().getName() + "." + td.getName().toString();

			if (StringUtils.equals(qualifiedTemp, meta.getQualifiedClassName())) {
				LOG.debug("Matched: " + qualifiedTemp);
			}
			else {
				LOG.warn(qualifiedTemp + " did not match " + meta.getQualifiedClassName());
				continue;
			}

			meta.setInterfaceClz(td.isInterface());
			for (Object m : td.modifiers()) {
				if (!(m instanceof Modifier)) {
					// skip if it isn't a modifier type.
					LOG.debug("Instance: " + ReflectionToStringBuilder.toString(m));
					continue;
				}
				Modifier mod = (Modifier) m;
				if (StringUtils.equals("public", mod.getKeyword().toString())) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Setting class: " + qualifiedTemp + " as public.");
					}
					meta.setPublicClz(true);
				}
				if (StringUtils.equals("abstract", mod.getKeyword().toString())) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Setting class: " + qualifiedTemp + " as abstract.");
					}
					meta.setAbstractClz(true);
				}
			}
		}

		// at this point, we should have all of the interested parties.
		super.chainDecorators(meta);
	}
}
