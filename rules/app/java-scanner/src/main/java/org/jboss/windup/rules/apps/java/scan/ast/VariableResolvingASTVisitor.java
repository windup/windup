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
package org.jboss.windup.rules.apps.java.scan.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.scan.dao.JavaClassDao;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs through the source code and checks "type" uses against the blacklisted class entries.
 * 
 * @author bradsdavis
 * 
 */
public class VariableResolvingASTVisitor extends ASTVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(VariableResolvingASTVisitor.class);

    @Inject
    private Event<JavaScannerASTEvent> javaScannerASTEvent;

    @Inject
    private GraphContext graphContext;

    @Inject
    private JavaClassDao javaClassDao;

    private CompilationUnit cu;

    /**
     * Contains all wildcard imports (import com.example.*) lines from the source file.
     * 
     * These are used for type resolution throughout the class.
     */
    private final List<String> wildcardImports = new ArrayList<>();

    /**
     * Indicates that we have already attempted to query the graph for this particular shortname. The shortname will
     * exist here even if no results were found.
     */
    private final Set<String> classNameLookedUp = new HashSet<>();
    /**
     * Contains a map of class short names (eg, MyClass) to qualified names (eg, com.example.MyClass)
     */
    private final Map<String, String> classNameToFQCN = new HashMap<>();

    private FileModel fileModel;

    public void init(CompilationUnit cu, FileModel fileModel)
    {
        this.cu = cu;
        this.fileModel = fileModel;
    }

    private void fireJavaScannerEvent(ClassCandidate classCandidate)
    {
        JavaScannerASTEvent event = new JavaScannerASTEvent(graphContext, fileModel, classCandidate);
        javaScannerASTEvent.fire(event);
    }

    private void processConstructor(ConstructorType interest, int lineStart, int startPosition, int length,
                JavaSourceType sourceType)
    {
        ClassCandidate dr = new ClassCandidate(ClassCandidateType.CONSTRUCTOR_CALL, lineStart, startPosition, length,
                    interest.toString());
        fireJavaScannerEvent(dr);

        LOG.info("Candidate: " + dr);
    }

    private void processMethod(MethodType interest, int lineStart, int startPosition, int length,
                JavaSourceType sourceType)
    {
        ClassCandidate dr = new ClassCandidate(ClassCandidateType.METHOD_CALL, lineStart, startPosition, length,
                    interest.toString());
        fireJavaScannerEvent(dr);

        LOG.info("Candidate: " + dr);
    }

    private void processInterest(String interest, int lineStart, int startPosition, int length,
                JavaSourceType sourceType)
    {

        String sourceString = interest;
        sourceString = resolveClassname(sourceString);

        ClassCandidate dr = new ClassCandidate(ClassCandidateType.IMPORT, lineStart, startPosition, length,
                    sourceString);
        fireJavaScannerEvent(dr);

        LOG.info("Candidate: " + dr);
    }

    private void processType(Type type, ClassCandidateType classCandidateType)
    {
        if (type == null)
            return;

        int sourcePosition = cu.getLineNumber(type.getStartPosition());
        int startPosition = cu.getColumnNumber(type.getStartPosition());
        int length = type.getLength();

        String sourceString = type.toString();
        sourceString = resolveClassname(sourceString);

        ClassCandidate dr = new ClassCandidate(classCandidateType, sourcePosition, startPosition, length, sourceString);
        fireJavaScannerEvent(dr);

        LOG.info("Prefix: " + classCandidateType);
        if (type instanceof SimpleType)
        {
            SimpleType sType = (SimpleType) type;
            LOG.info("The type name is: " + sType.getName().getFullyQualifiedName() + " and " + sourceString);

        }
        LOG.info("Candidate: " + dr);
    }

    private void processName(Name name, ClassCandidateType type, int lineNumber, int startPosition, int length)
    {
        if (name == null)
            return;

        String sourceString = name.toString();
        sourceString = resolveClassname(sourceString);

        ClassCandidate dr = new ClassCandidate(type, lineNumber, startPosition, length, sourceString);
        fireJavaScannerEvent(dr);

        LOG.info("Prefix: " + type);
        LOG.info("Candidate: " + dr);
    }

    @Override
    public boolean visit(MethodDeclaration node)
    {
        // get a method's return type.
        Type returnType = node.getReturnType2();
        if (returnType != null)
        {
            processType(returnType, ClassCandidateType.RETURN_TYPE);
        }

        List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) node.parameters();
        if (parameters != null)
        {
            for (SingleVariableDeclaration type : parameters)
            {
                // make it fully qualified.
                String typeName = type.getType().toString();
                typeName = resolveClassname(typeName);
                // now add it as a local variable.
                // this.names.add(type.getName().toString());
                // this.nameInstance.put(type.getName().toString(), typeName);

                processType(type.getType(), ClassCandidateType.METHOD_PARAMETER);
            }
        }

        List<Name> throwsTypes = node.thrownExceptions();
        if (throwsTypes != null)
        {
            for (Name name : throwsTypes)
            {
                processName(name, ClassCandidateType.THROWS_METHOD_DECLARATION,
                            cu.getLineNumber(node.getStartPosition()),
                            cu.getColumnNumber(name.getStartPosition()), name.getLength());
            }
        }

        return super.visit(node);
    }

    @Override
    public boolean visit(InstanceofExpression node)
    {
        Type type = node.getRightOperand();
        processType(type, ClassCandidateType.INSTANCE_OF);

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.ThrowStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            processType(cic.getType(), ClassCandidateType.THROW_STATEMENT);
        }

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.CatchClause node)
    {
        Type catchType = node.getException().getType();
        processType(catchType, ClassCandidateType.CATCH_EXCEPTION_STATEMENT);

        return super.visit(node);
    }

    @Override
    public boolean visit(ReturnStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            processType(cic.getType(), ClassCandidateType.CONSTRUCTOR_CALL);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(FieldDeclaration node)
    {
        for (int i = 0; i < node.fragments().size(); ++i)
        {
            String nodeType = node.getType().toString();
            nodeType = resolveClassname(nodeType);

            VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(i);
            frag.resolveBinding();
            // this.names.add(frag.getName().getIdentifier());
            // this.nameInstance.put(frag.getName().toString(), nodeType.toString());

            processType(node.getType(), ClassCandidateType.FIELD_DECLARATION);
        }
        return true;
    }

    @Override
    public boolean visit(MarkerAnnotation node)
    {
        processName(node.getTypeName(), ClassCandidateType.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(cu.getStartPosition()), cu.getLength());
        return super.visit(node);
    }

    @Override
    public boolean visit(NormalAnnotation node)
    {
        processName(node.getTypeName(), ClassCandidateType.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(node.getStartPosition()), node.getLength());
        return super.visit(node);
    }

    @Override
    public boolean visit(SingleMemberAnnotation node)
    {
        processName(node.getTypeName(), ClassCandidateType.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(node.getStartPosition()), node.getLength());
        return super.visit(node);
    }

    public boolean visit(TypeDeclaration node)
    {
        Object clzInterfaces = node.getStructuralProperty(TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
        Object clzSuperClasses = node.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY);

        if (clzInterfaces != null)
        {
            if (List.class.isAssignableFrom(clzInterfaces.getClass()))
            {
                List<?> clzInterfacesList = (List<?>) clzInterfaces;
                for (Object clzInterface : clzInterfacesList)
                {
                    if (clzInterface instanceof SimpleType)
                    {
                        processType((SimpleType) clzInterface, ClassCandidateType.IMPLEMENTS_TYPE);
                    }
                    else
                    {
                        LOG.warn("" + clzInterface);
                    }
                }
            }
        }
        if (clzSuperClasses != null)
        {
            if (clzSuperClasses instanceof SimpleType)
            {
                processType((SimpleType) clzSuperClasses, ClassCandidateType.EXTENDS_TYPE);
            }
            else
            {
                LOG.warn("" + clzSuperClasses);
            }
        }

        return super.visit(node);
    }

    @Override
    public boolean visit(VariableDeclarationStatement node)
    {
        for (int i = 0; i < node.fragments().size(); ++i)
        {
            String nodeType = node.getType().toString();
            nodeType = resolveClassname(nodeType);

            VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(i);
            // this.names.add(frag.getName().getIdentifier());
            // this.nameInstance.put(frag.getName().toString(), nodeType.toString());
        }
        processType(node.getType(), ClassCandidateType.VARIABLE_DECLARATION);

        return super.visit(node);
    }

    @Override
    public boolean visit(ImportDeclaration node)
    {
        String name = node.getName().toString();
        if (node.isOnDemand())
        {
            wildcardImports.add(name);

            Iterable<JavaClassModel> classModels = javaClassDao.findByJavaPackage(name);
            for (JavaClassModel classModel : classModels)
            {
                processInterest(classModel.getQualifiedName(), cu.getLineNumber(node.getName().getStartPosition()),
                            cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(),
                            JavaSourceType.IMPORT);
            }
        }
        else
        {
            String clzName = StringUtils.substringAfterLast(name, ".");
            classNameLookedUp.add(clzName);
            classNameToFQCN.put(clzName, name);
            processInterest(node.getName().toString(), cu.getLineNumber(node.getName().getStartPosition()),
                        cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(),
                        JavaSourceType.IMPORT);
        }

        return super.visit(node);
    }

    /***
     * Takes the MethodInvocation, and attempts to resolve the types of objects passed into the method invocation.
     */
    public boolean visit(MethodInvocation node)
    {
        if (!StringUtils.contains(node.toString(), "."))
        {
            // it must be a local method. ignore.
            return true;
        }

        String nodeName = StringUtils.removeStart(node.toString(), "this.");

        List arguements = node.arguments();
        List<String> resolvedParams = methodParameterGuesser(arguements);

        String objRef = StringUtils.substringBefore(nodeName, "." + node.getName().toString());

        // if (nameInstance.containsKey(objRef))
        // {
        // objRef = nameInstance.get(objRef);
        // }

        objRef = resolveClassname(objRef);

        MethodType methodCall = new MethodType(objRef, node.getName().toString(), resolvedParams);
        processMethod(methodCall, cu.getLineNumber(node.getName().getStartPosition()),
                    cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(),
                    JavaSourceType.METHOD);

        return super.visit(node);
    }

    @Override
    public boolean visit(PackageDeclaration node)
    {
        LOG.info("Found package: " + node.getName().toString());
        return super.visit(node);
    }

    @Override
    public boolean visit(ClassInstanceCreation node)
    {
        String nodeType = node.getType().toString();
        nodeType = resolveClassname(nodeType);

        List<String> resolvedParams = this.methodParameterGuesser(node.arguments());

        ConstructorType resolvedConstructor = new ConstructorType(nodeType, resolvedParams);
        processConstructor(resolvedConstructor, cu.getLineNumber(node.getType().getStartPosition()),
                    cu.getColumnNumber(node.getType().getStartPosition()), node.getType().getLength(),
                    JavaSourceType.CONSTRUCT);

        return super.visit(node);
    }

    private List<String> methodParameterGuesser(List arguements)
    {
        List<String> resolvedParams = new ArrayList<String>(arguements.size());
        for (Object o : arguements)
        {
            if (o instanceof SimpleName)
            {
                // String name = nameInstance.get(o.toString());
                // if (name != null)
                // {
                // resolvedParams.add(name);
                // }
                // else
                // {
                // resolvedParams.add("Undefined");
                // }
            }
            else if (o instanceof StringLiteral)
            {
                resolvedParams.add("java.lang.String");
            }
            else if (o instanceof FieldAccess)
            {
                String field = ((FieldAccess) o).getName().toString();

                // if (names.contains(field))
                // {
                // resolvedParams.add(nameInstance.get(field));
                // }
                // else
                // {
                // resolvedParams.add("Undefined");
                // }
            }
            else if (o instanceof CastExpression)
            {
                String type = ((CastExpression) o).getType().toString();
                type = qualifyType(type);
                resolvedParams.add(type);
            }
            else if (o instanceof MethodInvocation)
            {
                String on = ((MethodInvocation) o).getName().toString();
                if (StringUtils.equals(on, "toString"))
                {
                    if (((MethodInvocation) o).arguments().size() == 0)
                    {
                        resolvedParams.add("java.lang.String");
                    }
                }
                else
                {
                    resolvedParams.add("Undefined");
                }
            }
            else if (o instanceof NumberLiteral)
            {
                if (StringUtils.endsWith(o.toString(), "L"))
                {
                    resolvedParams.add("long");
                }
                else if (StringUtils.endsWith(o.toString(), "f"))
                {
                    resolvedParams.add("float");
                }
                else if (StringUtils.endsWith(o.toString(), "d"))
                {
                    resolvedParams.add("double");
                }
                else
                {
                    resolvedParams.add("int");
                }
            }
            else if (o instanceof BooleanLiteral)
            {
                resolvedParams.add("boolean");
            }
            else if (o instanceof ClassInstanceCreation)
            {
                String nodeType = ((ClassInstanceCreation) o).getType().toString();
                nodeType = resolveClassname(nodeType);
                resolvedParams.add(nodeType);
            }
            else if (o instanceof org.eclipse.jdt.core.dom.CharacterLiteral)
            {
                resolvedParams.add("char");
            }
            else if (o instanceof InfixExpression)
            {
                String expression = o.toString();
                if (StringUtils.contains(expression, "\""))
                {
                    resolvedParams.add("java.lang.String");
                }
                else
                {
                    resolvedParams.add("Undefined");
                }
            }
            else
            {
                LOG.info("Unable to determine type: " + o.getClass() + ReflectionToStringBuilder.toString(o));
                resolvedParams.add("Undefined");
            }
        }

        return resolvedParams;
    }

    private String qualifyType(String objRef)
    {
        // temporarily remove to resolve arrays
        objRef = StringUtils.removeEnd(objRef, "[]");
        // if (nameInstance.containsKey(objRef))
        // {
        // objRef = nameInstance.get(objRef);
        // }

        objRef = resolveClassname(objRef);

        return objRef;
    }

    public static class MethodType
    {
        private final String qualifiedName;
        private final String methodName;
        private final List<String> qualifiedParameters;

        public MethodType(String qualifiedName, String methodName, List<String> qualifiedParameters)
        {
            this.qualifiedName = qualifiedName;
            this.methodName = methodName;

            if (qualifiedParameters != null)
            {
                this.qualifiedParameters = qualifiedParameters;
            }
            else
            {
                this.qualifiedParameters = new LinkedList<String>();
            }
        }

        public String getMethodName()
        {
            return methodName;
        }

        public String getQualifiedName()
        {
            return qualifiedName;
        }

        public List<String> getQualifiedParameters()
        {
            return qualifiedParameters;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(qualifiedName + "." + methodName);
            builder.append("(");

            for (int i = 0, j = qualifiedParameters.size(); i < j; i++)
            {
                if (i > 0)
                {
                    builder.append(", ");
                }
                String param = qualifiedParameters.get(i);
                builder.append(param);
            }
            builder.append(")");

            return builder.toString();
        }
    }

    public static class ConstructorType
    {
        private final String qualifiedName;
        private final List<String> qualifiedParameters;

        public ConstructorType(String qualifiedName, List<String> qualifiedParameters)
        {
            this.qualifiedName = qualifiedName;
            if (qualifiedParameters != null)
            {
                this.qualifiedParameters = qualifiedParameters;
            }
            else
            {
                this.qualifiedParameters = new LinkedList<String>();
            }

        }

        public String getQualifiedName()
        {
            return qualifiedName;
        }

        public List<String> getQualifiedParameters()
        {
            return qualifiedParameters;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(qualifiedName);
            builder.append("(");

            for (int i = 0, j = qualifiedParameters.size(); i < j; i++)
            {
                if (i > 0)
                {
                    builder.append(", ");
                }
                String param = qualifiedParameters.get(i);
                builder.append(param);
            }
            builder.append(")");

            return builder.toString();
        }
    }

    private String resolveClassname(String sourceClassname)
    {
        // If the type contains a "." assume that it is fully qualified.
        // FIXME - This is a carryover from the original Windup code, and I don't think
        // that this assumption is valid.
        if (!StringUtils.contains(sourceClassname, "."))
        {
            // Check if we have already looked this one up
            if (classNameLookedUp.contains(sourceClassname))
            {
                // if yes, then just use the looked up name from the map
                String qualifiedName = classNameToFQCN.get(sourceClassname);
                if (qualifiedName != null)
                {
                    return qualifiedName;
                }
                else
                {
                    // otherwise, just return the provided name (unchanged)
                    return sourceClassname;
                }
            }
            else
            {
                // if this name has not been resolved before, go ahead and resolve it from the graph (if possible)
                classNameLookedUp.add(sourceClassname);

                // search every wildcard import for this name
                for (String wildcardImport : wildcardImports)
                {
                    String candidateQualifiedName = wildcardImport + "." + sourceClassname;

                    JavaClassModel jcm = javaClassDao.getJavaClass(candidateQualifiedName);
                    if (jcm != null)
                    {
                        // we found it... put it in the map and return the result
                        classNameToFQCN.put(sourceClassname, candidateQualifiedName);
                        return candidateQualifiedName;
                    }
                }
                // nothing was found, so just return the original value
                return sourceClassname;
            }
        }
        else
        {
            return sourceClassname;
        }
    }

}
