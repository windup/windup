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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.jboss.windup.metadata.decoration.AbstractDecoration;


public class JavaASTAnnotationVisitor extends ASTVisitor {

	private static final Log LOG = LogFactory.getLog(JavaASTAnnotationVisitor.class);
	
	private final Collection<AbstractDecoration> results;
	private final CompilationUnit cu;

	private final Map<String, String> classNameToFullyQualified = new HashMap<String, String>();

	
	public JavaASTAnnotationVisitor(CompilationUnit cu, Collection<AbstractDecoration> results, Set<String> knownDependencies) {
		this.results = results;
		this.cu = cu;
		
		for (String dependency : knownDependencies) {
			classNameToFullyQualified.put(StringUtils.substringAfterLast(dependency, "."), dependency);
		}
	}
	
	@Override
	public boolean visit(MarkerAnnotation node) {
		return super.visit(node);
	}
	
	protected String qualify(Name objects) {
		if(objects instanceof org.eclipse.jdt.core.dom.QualifiedName) {
			return qualify((QualifiedName)objects);
		}
		else if(objects instanceof org.eclipse.jdt.core.dom.SimpleName) {
			return qualify((SimpleName)objects);
		}
		else {
			return objects.toString();
		}
	}

	protected String qualify(QualifiedName name) {
		Name qualifier = name.getQualifier();
		String q = name.getQualifier().toString();
		if(qualifier instanceof org.eclipse.jdt.core.dom.QualifiedName) {
			q = qualify((QualifiedName)qualifier);
		}
		else if(qualifier  instanceof org.eclipse.jdt.core.dom.SimpleName) {
			q = qualify((SimpleName)qualifier);
		}
		String n = qualify((SimpleName)name.getName());
		
		if(StringUtils.isNotBlank(q)) {
			n = q + "." + n;
		}
		return n;
	}

	protected String qualify(SimpleName name) {
		return qualify(name.toString());
	}
	
	protected String qualify(String className) {
		if(classNameToFullyQualified.containsKey(className)) {
			className = classNameToFullyQualified.get(className);
		}
		
		return className;
	}
	
	protected String extract(org.eclipse.jdt.core.dom.Expression value) {
		if(value instanceof Name) {
			return extract((Name)value);
		}
		else if(value instanceof ArrayInitializer) {
			return extract((ArrayInitializer)value);
		}
		else if(value instanceof NullLiteral) {
			return extract((NullLiteral)value);
		}
		else if(value instanceof NumberLiteral) {
			return extract((NumberLiteral)value);
		}
		else if(value instanceof StringLiteral) {
			return extract((StringLiteral)value);
		}
		else if(value instanceof BooleanLiteral) {
			return extract((BooleanLiteral)value);
		}
		else if(value instanceof TypeLiteral) {
			return extract((TypeLiteral)value);
		}
		return "Unknown: "+ReflectionToStringBuilder.toString(value);
	}
	
	protected String extract(Name value) {
		return qualify(value);
	}
	
	protected String extract(ArrayInitializer value) {
		List objects = value.expressions();
		
		for(Object object : objects) {
			LOG.info("Array Object: "+extract((Expression)object));
		}
		
		return value.toString();
	}
	
	protected String extract(TypeLiteral value) {
		return qualify(value.toString());
	}
	
	protected String extract(NullLiteral value) {
		return value.toString();
	}
	
	protected String extract(NumberLiteral value) {
		return value.toString();
	}
	
	protected String extract(StringLiteral value) {
		String val = value.toString();
		val = StringUtils.removeStart(val, "\"");
		val = StringUtils.removeEnd(val, "\"");
		
		return val;
	}
	
	protected String extract(BooleanLiteral value) {
		return value.toString();
	}
	

	@Override
	public boolean visit(NormalAnnotation node) {
		
		List objects = node.values();
		String name = qualify(node.getTypeName());
		
		LOG.info("Node Type: "+name);
		if(objects != null) {
			for(Object object : objects) {
				if(object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair)object;
					String val = extract(pair.getValue());
					LOG.info("Name: "+pair.getName().toString() + ", Value: "+val);
				}
				else {
					LOG.info("Unknown: Value: "+ReflectionToStringBuilder.toString(object));
				}
				
			}
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		Expression objects = node.getValue();
		String name = qualify(node.getTypeName());
		
		LOG.info("Node Type: "+name);
		if(objects != null) {			
			extract(objects);
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		String importVal = node.getName().toString();
		classNameToFullyQualified.put(StringUtils.substringAfterLast(importVal, "."), importVal);
		return super.visit(node);
	}
}
