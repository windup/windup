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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.JavaLine;
import org.jboss.windup.metadata.decoration.SourceType;


public class JavaASTVariableResolvingVisitor extends ASTVisitor {
	private static final Log LOG = LogFactory.getLog(JavaASTVariableResolvingVisitor.class);

	private final Collection<AbstractDecoration> results;
	private final CompilationUnit cu;
	private final Set<String> classBlacklists;

	private final Map<String, String> classNameToFullyQualified = new HashMap<String, String>();

	Set<String> names = new HashSet<String>();
	Map<String, String> nameInstance = new HashMap<String, String>();

	public JavaASTVariableResolvingVisitor(CompilationUnit cu, Collection<AbstractDecoration> results, String qualifiedClassName, Set<String> knownDependencies, Set<String> blacklists) {
		this.results = results;
		this.cu = cu;
		this.classBlacklists = blacklists;

		for (String dependency : knownDependencies) {
			classNameToFullyQualified.put(StringUtils.substringAfterLast(dependency, "."), dependency);
		}

		this.names.add("this");
		this.nameInstance.put("this", qualifiedClassName);
		this.classNameToFullyQualified.put(StringUtils.substringAfterLast(qualifiedClassName, "."), qualifiedClassName);
	}

	public Collection<AbstractDecoration> getResults() {
		return results;
	}

	private void processInterest(String interest, int lineStart, String decoratorPrefix, SourceType sourceType) {
		int sourcePosition = lineStart;
		String sourceString = interest;
		if (!StringUtils.contains(sourceString, ".")) {
			if (classNameToFullyQualified.containsKey(sourceString)) {
				sourceString = classNameToFullyQualified.get(sourceString);
			}
		}

		if (lineContainsClassBlacklist(sourceString)) {
			JavaLine dr = new JavaLine();
			dr.setDescription(decoratorPrefix + " '" + sourceString + "' at line " + sourcePosition);
			dr.setLineNumber(sourcePosition);
			dr.setSourceType(sourceType);
			dr.setPattern(sourceString);

			this.results.add(dr);
		}
	}

	private void processType(Type type, String decoratorPrefix, SourceType sourceType) {
		if (type == null)
			return;

		int sourcePosition = cu.getLineNumber(type.getStartPosition());
		String sourceString = type.toString();
		if (!StringUtils.contains(sourceString, ".")) {
			if (classNameToFullyQualified.containsKey(sourceString)) {
				sourceString = classNameToFullyQualified.get(sourceString);
			}
		}

		if (lineContainsClassBlacklist(sourceString)) {
			JavaLine dr = new JavaLine();
			dr.setDescription(decoratorPrefix + " '" + sourceString + "' at line " + sourcePosition);
			dr.setLineNumber(sourcePosition);
			dr.setSourceType(sourceType);
			dr.setPattern(sourceString);
			// dr.setAstNode(type);
			results.add(dr);
		}
	}

	private void processName(Name name, String decoratorPrefix, int position) {
		if (name == null)
			return;

		int sourcePosition = position;
		String sourceString = name.toString();

		if (!StringUtils.contains(sourceString, ".")) {
			if (classNameToFullyQualified.containsKey(sourceString)) {
				sourceString = classNameToFullyQualified.get(sourceString);
			}
		}

		if (lineContainsClassBlacklist(sourceString)) {
			JavaLine dr = new JavaLine();
			dr.setDescription(decoratorPrefix + " '" + sourceString + "' at line " + sourcePosition);
			dr.setLineNumber(sourcePosition);
			dr.setSourceType(SourceType.TYPE);
			dr.setPattern(sourceString);
			// dr.setAstNode(name);
			results.add(dr);
		}
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		// get a method's return type.
		Type returnType = node.getReturnType2();
		if (returnType != null) {
			processType(returnType, "Return type", SourceType.TYPE);
		}

		List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) node.parameters();
		if (parameters != null) {
			for (SingleVariableDeclaration type : parameters) {
				// make it fully qualified.
				String typeName = type.getType().toString();
				if (classNameToFullyQualified.containsKey(typeName)) {
					typeName = classNameToFullyQualified.get(typeName);
				}
				// now add it as a local variable.
				this.names.add(type.getName().toString());
				this.nameInstance.put(type.getName().toString(), typeName);

				processType(type.getType(), "Method parameter", SourceType.TYPE);
			}
		}

		List<Name> throwsTypes = node.thrownExceptions();
		if (throwsTypes != null) {
			for (Name name : throwsTypes) {
				processName(name, "Throws", node.getStartPosition());
			}
		}

		return true;
	}

	public boolean visit(org.eclipse.jdt.core.dom.ThrowStatement node) {
		if (node.getExpression() instanceof ClassInstanceCreation) {
			ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
			processType(cic.getType(), "Throwing", SourceType.TYPE);
		}

		return true;
	}

	public boolean visit(org.eclipse.jdt.core.dom.CatchClause node) {
		Type catchType = node.getException().getType();
		processType(catchType, "Catching", SourceType.TYPE);

		return true;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		if (node.getExpression() instanceof ClassInstanceCreation) {
			ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
			processType(cic.getType(), "Declaring", SourceType.TYPE);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		Type superClasses = node.getSuperclassType();
		List<Type> superInterfaces = node.superInterfaceTypes();
		processType(superClasses, "Extending class", SourceType.INHERITANCE);

		if (superInterfaces != null) {
			for (Type inter : superInterfaces) {
				processType(inter, "Implementing interface", SourceType.INHERITANCE);
			}
		}

		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		String nodeType = node.getType().toString();
		if (classNameToFullyQualified.containsKey(nodeType)) {
			nodeType = classNameToFullyQualified.get(nodeType);
		}

		List<String> resolvedParams = this.methodParameterGuesser(node.arguments());

		String resolvedConstructorCall = nodeType + "(";
		for (int i = 0, j = resolvedParams.size(); i < j; i++) {
			resolvedConstructorCall += resolvedParams.get(i);

			if (i < j - 1) {
				resolvedConstructorCall += ", ";
			}
		}
		resolvedConstructorCall = resolvedConstructorCall + ")";
		LOG.trace("Resolved Constructor: " + resolvedConstructorCall);
		processInterest(resolvedConstructorCall, cu.getLineNumber(node.getStartPosition()), "Constructing type", SourceType.CONSTRUCT);

		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		for (int i = 0; i < node.fragments().size(); ++i) {
			String nodeType = node.getType().toString();
			if (!StringUtils.contains(nodeType, ".")) {
				if (classNameToFullyQualified.containsKey(nodeType)) {
					nodeType = classNameToFullyQualified.get(nodeType);
				}
			}
			VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(i);
			frag.resolveBinding();
			this.names.add(frag.getName().getIdentifier());
			this.nameInstance.put(frag.getName().toString(), nodeType.toString());

			processType(node.getType(), "Declaring type", SourceType.TYPE);
		}
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		processName(node.getTypeName(), "Annotation", cu.getLineNumber(node.getStartPosition()));
		return super.visit(node);
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		processName(node.getTypeName(), "Annotation", cu.getLineNumber(node.getStartPosition()));
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		processName(node.getTypeName(), "Annotation", cu.getLineNumber(node.getStartPosition()));
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		for (int i = 0; i < node.fragments().size(); ++i) {
			String nodeType = node.getType().toString();
			if (!StringUtils.contains(nodeType, ".")) {
				if (classNameToFullyQualified.containsKey(nodeType)) {
					nodeType = classNameToFullyQualified.get(nodeType);
				}
			}

			VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(i);
			this.names.add(frag.getName().getIdentifier());
			this.nameInstance.put(frag.getName().toString(), nodeType.toString());

			processType(node.getType(), "Declaring type", SourceType.TYPE);
		}

		return true;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		String name = node.getName().toString();
		if (!node.isOnDemand()) {
			String clzName = StringUtils.substringAfterLast(name, ".");
			classNameToFullyQualified.put(clzName, name);
			processInterest(node.getName().toString(), cu.getLineNumber(node.getStartPosition()), "Import of", SourceType.IMPORT);
		}
		else {
			if (lineContainsPackageBlacklist(name)) {
				for (String knownClz : classNameToFullyQualified.values()) {
					if (StringUtils.startsWith(knownClz, name)) {
						processInterest(knownClz, cu.getLineNumber(node.getStartPosition()), "Leads to import of", SourceType.IMPORT);
					}
				}
			}
		}

		return true;
	}

	public boolean visit(MethodInvocation node) {
		if (!StringUtils.contains(node.toString(), ".")) {
			// it must be a local method. ignore.
			return true;
		}

		String nodeName = StringUtils.removeStart(node.toString(), "this.");

		List arguements = node.arguments();
		List<String> resolvedParams = methodParameterGuesser(arguements);

		String objRef = StringUtils.substringBefore(nodeName, "." + node.getName().toString());

		if (nameInstance.containsKey(objRef)) {
			objRef = nameInstance.get(objRef);
		}

		if (classNameToFullyQualified.containsKey(objRef)) {
			objRef = classNameToFullyQualified.get(objRef);
		}
		String resolvedMethodCall = objRef + "." + node.getName().toString() + "(";

		for (int i = 0, j = resolvedParams.size(); i < j; i++) {
			resolvedMethodCall += resolvedParams.get(i);

			if (i < j - 1) {
				resolvedMethodCall += ", ";
			}
		}
		resolvedMethodCall = resolvedMethodCall + ")";
		LOG.trace("Resolved Method call: " + resolvedMethodCall);
		// Here is the call to blacklist
		processInterest(resolvedMethodCall, cu.getLineNumber(node.getStartPosition()), "Usage of", SourceType.METHOD);

		return true;
	}

	private List<String> methodParameterGuesser(List arguements) {
		List<String> resolvedParams = new ArrayList<String>(arguements.size());
		for (Object o : arguements) {
			if (o instanceof SimpleName) {
				String name = nameInstance.get(o.toString());
				if (name != null) {
					resolvedParams.add(name);
				}
				else {
					resolvedParams.add("Undefined");
				}
			}
			else if (o instanceof StringLiteral) {
				resolvedParams.add("java.lang.String");
			}
			else if (o instanceof FieldAccess) {
				String field = ((FieldAccess) o).getName().toString();

				if (names.contains(field)) {
					resolvedParams.add(nameInstance.get(field));
				}
				else {
					resolvedParams.add("Undefined");
				}
			}
			else if (o instanceof CastExpression) {
				String type = ((CastExpression) o).getType().toString();
				type = qualifyType(type);
				resolvedParams.add(type);
			}
			else if (o instanceof MethodInvocation) {
				String on = ((MethodInvocation) o).getName().toString();
				if (StringUtils.equals(on, "toString")) {
					if (((MethodInvocation) o).arguments().size() == 0) {
						resolvedParams.add("java.lang.String");
					}
				}
				else {
					resolvedParams.add("Undefined");
				}
			}
			else if (o instanceof NumberLiteral) {
				if (StringUtils.endsWith(o.toString(), "L")) {
					resolvedParams.add("long");
				}
				else if (StringUtils.endsWith(o.toString(), "f")) {
					resolvedParams.add("float");
				}
				else if (StringUtils.endsWith(o.toString(), "d")) {
					resolvedParams.add("double");
				}
				else {
					resolvedParams.add("int");
				}
			}
			else if (o instanceof BooleanLiteral) {
				resolvedParams.add("boolean");
			}
			else if (o instanceof ClassInstanceCreation) {
				String nodeType = ((ClassInstanceCreation) o).getType().toString();
				if (classNameToFullyQualified.containsKey(nodeType)) {
					nodeType = classNameToFullyQualified.get(nodeType.toString());
				}
				resolvedParams.add(nodeType);
			}
			else if (o instanceof org.eclipse.jdt.core.dom.CharacterLiteral) {
				resolvedParams.add("char");
			}
			else if (o instanceof InfixExpression) {
				String expression = o.toString();
				if (StringUtils.contains(expression, "\"")) {
					resolvedParams.add("java.lang.String");
				}
				else {
					resolvedParams.add("Undefined");
				}
			}
			else {
				LOG.debug("Unable to determine type: " + o.getClass() + ReflectionToStringBuilder.toString(o));
				resolvedParams.add("Undefined");
			}
		}

		return resolvedParams;
	}

	private boolean lineContainsPackageBlacklist(String line) {
		for (String black : this.classBlacklists) {
			black = StringUtils.substringBeforeLast(black, ".");

			if (StringUtils.startsWith(line, black)) {
				return true;
			}
		}
		return false;
	}

	private boolean lineContainsClassBlacklist(String line) {
		for (String black : this.classBlacklists) {
			if (StringUtils.startsWith(line, black)) {
				return true;
			}
		}
		return false;
	}

	private String qualifyType(String objRef) {
		// temporarily remove to resolve arrays
		objRef = StringUtils.removeEnd(objRef, "[]");
		if (nameInstance.containsKey(objRef)) {
			objRef = nameInstance.get(objRef);
		}

		if (classNameToFullyQualified.containsKey(objRef)) {
			objRef = classNameToFullyQualified.get(objRef);
		}

		return objRef;
	}
}
