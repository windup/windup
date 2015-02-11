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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
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
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.java.service.TypeReferenceService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.Logging;

/**
 * Runs through the source code and checks "type" uses against the blacklisted class entries.
 * 
 * @author bradsdavis
 * @author jsightle
 * @author lincolnthree
 * @author mbriskar
 * @author ozizka
 */
public class VariableResolvingASTVisitor extends ASTVisitor
{
    private static final Logger LOG = Logging.get(VariableResolvingASTVisitor.class);

    private final GraphService<JavaAnnotationTypeReferenceModel> annotationTypeReferenceService;
    private final JavaClassService javaClassService;
    private final TypeReferenceService typeRefService;
    private final WindupJavaConfigurationService windupJavaCfgService;

    public VariableResolvingASTVisitor(GraphContext context)
    {
        this.annotationTypeReferenceService = new GraphService<>(context, JavaAnnotationTypeReferenceModel.class);
        this.javaClassService = new JavaClassService(context);
        this.typeRefService = new TypeReferenceService(context);
        this.windupJavaCfgService = new WindupJavaConfigurationService(context);
    }

    private CompilationUnit cu;
    private String fqcn;

    /**
     * Contains all wildcard imports (import com.example.*) lines from the source file.
     *
     * These are used for type resolution throughout the class.
     */
    private final List<String> wildcardImports = new ArrayList<>();

    /**
     * Indicates that we have already attempted to query the graph for this particular shortname. The shortname will exist here even if no results
     * were found.
     */
    private final Set<String> classNameLookedUp = new HashSet<>();

    /**
     * Contains a map of class short names (eg, MyClass) to qualified names (eg, com.example.MyClass)
     */
    private final Map<String, String> classNameToFQCN = new HashMap<>();

    /**
     * Maintains a set of all variable names that have been resolved
     */
    private final Set<String> names = new HashSet<String>();

    /**
     * Maintains a map of nameInstances to fully qualified class names.
     */
    private final Map<String, String> nameInstance = new HashMap<String, String>();

    private FileModel fileModel;

    public void init(CompilationUnit cu, FileModel fileModel)
    {
        this.cu = cu;
        this.fileModel = fileModel;
        this.wildcardImports.clear();
        this.classNameLookedUp.clear();
        this.classNameToFQCN.clear();
        this.names.clear();
        this.nameInstance.clear();

        PackageDeclaration packageDeclaration = cu.getPackage();
        String packageName = packageDeclaration == null ? "" : packageDeclaration.getName().getFullyQualifiedName();
        @SuppressWarnings("unchecked")
        List<TypeDeclaration> types = cu.types();
        if (!types.isEmpty())
        {
            TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
            String className = typeDeclaration.getName().getFullyQualifiedName();

            if (packageName.equals(""))
            {
                this.fqcn = className;
            }
            else
            {
                this.fqcn = packageName + "." + className;
            }

            // add all customer package type as references
            if (windupJavaCfgService.shouldScanPackage(packageName))
            {
                typeRefService.createTypeReference(fileModel, TypeReferenceLocation.TYPE, cu.getLineNumber(typeDeclaration.getStartPosition()),
                            cu.getColumnNumber(cu.getStartPosition()), cu.getLength(), this.fqcn);

                Type superclassType = typeDeclaration.getSuperclassType();
                ITypeBinding resolveBinding = null;
                if (superclassType != null)
                {
                    resolveBinding = superclassType.resolveBinding();
                }

                while (resolveBinding != null)
                {
                    if (superclassType.resolveBinding() != null)
                    {
                        typeRefService.createTypeReference(fileModel, TypeReferenceLocation.TYPE,
                                    cu.getLineNumber(typeDeclaration.getStartPosition()),
                                    cu.getColumnNumber(cu.getStartPosition()), cu.getLength(), resolveBinding.getQualifiedName());
                    }
                    resolveBinding = resolveBinding.getSuperclass();
                }
            }

            this.names.add("this");
            this.nameInstance.put("this", fqcn);
        }
    }

    private void processConstructor(ConstructorType interest, int lineNumber, int columnNumber, int length)
    {
        String text = interest.toString();
        if (TypeInterestFactory.matchesAny(text, TypeReferenceLocation.CONSTRUCTOR_CALL))
        {
            JavaTypeReferenceModel typeRef = typeRefService.createTypeReference(fileModel,
                        TypeReferenceLocation.CONSTRUCTOR_CALL,
                        lineNumber, columnNumber, length, text);

            LOG.finer("Candidate: " + typeRef);
        }
    }

    private void processMethod(MethodType interest, int lineNumber, int columnNumber, int length)
    {
        String text = interest.toString();
        if (TypeInterestFactory.matchesAny(text, TypeReferenceLocation.METHOD_CALL))
        {
            JavaTypeReferenceModel typeRef = typeRefService.createTypeReference(fileModel,
                        TypeReferenceLocation.METHOD_CALL,
                        lineNumber, columnNumber, length, text);

            LOG.finer("Candidate: " + typeRef);
        }
    }

    private void processImport(String interest, int lineNumber, int columnNumber, int length)
    {
        if (TypeInterestFactory.matchesAny(interest, TypeReferenceLocation.IMPORT))
        {

            JavaTypeReferenceModel typeRef = typeRefService.createTypeReference(fileModel,
                        TypeReferenceLocation.IMPORT,
                        lineNumber, columnNumber, length, interest.toString());

            LOG.finer("Candidate: " + typeRef);
        }
    }

    private JavaTypeReferenceModel processTypeBinding(ITypeBinding type, TypeReferenceLocation referenceLocation, int lineNumber, int columnNumber,
                int length)
    {
        if (type == null)
            return null;

        String sourceString = type.getQualifiedName();
        return processTypeAsString(sourceString, referenceLocation, lineNumber, columnNumber, length);
    }

    /**
     * The method determines if the type can be resolved and if not, will try to guess the qualified name using the information from the imports.
     */
    private JavaTypeReferenceModel processType(Type type, TypeReferenceLocation typeReferenceLocation, int lineNumber, int columnNumber, int length)
    {
        if (type == null)
            return null;
        ITypeBinding resolveBinding = type.resolveBinding();
        if (resolveBinding == null)
        {
            return processTypeAsString(resolveClassname(type.toString()), typeReferenceLocation, lineNumber,
                        columnNumber, length);
        }
        else
        {
            return processTypeBinding(type.resolveBinding(), typeReferenceLocation, lineNumber,
                        columnNumber, length);
        }

    }

    private JavaTypeReferenceModel processTypeAsString(String sourceString, TypeReferenceLocation referenceLocation, int lineNumber,
                int columnNumber,
                int length)
    {
        if (sourceString == null)
            return null;

        if (!TypeInterestFactory.matchesAny(sourceString, referenceLocation))
            return null;

        JavaTypeReferenceModel typeRef = typeRefService.createTypeReference(fileModel, referenceLocation,
                    lineNumber, columnNumber, length, sourceString);
        if (TypeReferenceLocation.ANNOTATION == referenceLocation)
        {
            typeRef = this.annotationTypeReferenceService.addTypeToModel(typeRef);
        }

        LOG.finer("Prefix: " + referenceLocation);
        LOG.finer("Candidate: " + typeRef);
        return typeRef;
    }

    @Override
    public boolean visit(MethodDeclaration node)
    {
        // get a method's return type.
        IMethodBinding resolveBinding = node.resolveBinding();
        ITypeBinding returnType = null;
        if (resolveBinding != null)
        {
            returnType = node.resolveBinding().getReturnType();
        }
        if (returnType != null)
        {
            processTypeBinding(returnType, TypeReferenceLocation.RETURN_TYPE, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength());
        }
        else
        {
            Type methodReturnType = node.getReturnType2();
            processType(methodReturnType, TypeReferenceLocation.RETURN_TYPE, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength());
        }

        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) node.parameters();
        if (parameters != null)
        {
            for (SingleVariableDeclaration type : parameters)
            {
                this.names.add(type.getName().toString());
                String typeName = type.getType().toString();
                typeName = resolveClassname(typeName);
                this.nameInstance.put(type.getName().toString(), typeName);
                processType(type.getType(), TypeReferenceLocation.METHOD_PARAMETER, cu.getLineNumber(node.getStartPosition()),
                            cu.getColumnNumber(node.getStartPosition()), node.getLength());
            }
        }

        @SuppressWarnings("unchecked")
        List<Type> throwsTypes = node.thrownExceptionTypes();
        if (throwsTypes != null)
        {
            for (Type type : throwsTypes)
            {
                processType(type,TypeReferenceLocation.THROWS_METHOD_DECLARATION,
                            cu.getLineNumber(node.getStartPosition()),
                            cu.getColumnNumber(type.getStartPosition()), type.getLength());
            }
        }

        return super.visit(node);
    }

    @Override
    public boolean visit(InstanceofExpression node)
    {
        Type type = node.getRightOperand();
        processTypeBinding(type.resolveBinding(), TypeReferenceLocation.INSTANCE_OF, cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(type.getStartPosition()), type.getLength());

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.ThrowStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            Type type = cic.getType();
            processType(type, TypeReferenceLocation.THROW_STATEMENT, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(cic.getStartPosition()), cic.getLength());
        }

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.CatchClause node)
    {
        Type catchType = node.getException().getType();
        processType(catchType, TypeReferenceLocation.CATCH_EXCEPTION_STATEMENT, cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(catchType.getStartPosition()), catchType.getLength());

        return super.visit(node);
    }

    @Override
    public boolean visit(ReturnStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            processTypeBinding(cic.getType().resolveBinding(), TypeReferenceLocation.CONSTRUCTOR_CALL, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(cic.getStartPosition()), cic.getLength());
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
            this.names.add(frag.getName().getIdentifier());
            this.nameInstance.put(frag.getName().toString(), nodeType.toString());

            processTypeBinding(node.getType().resolveBinding(), TypeReferenceLocation.FIELD_DECLARATION, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength());
        }
        return true;
    }

    /**
     * Adds parameters contained in the annotation into the annotation type reference
     * 
     * @param typeRef
     * @param node
     */
    private void addAnnotationValues(JavaAnnotationTypeReferenceModel typeRef, NormalAnnotation node)
    {
        @SuppressWarnings("unchecked")
        List<MemberValuePair> annotationValues = node.values();
        Map<String, String> annotationValueMap = new HashMap<>();
        for (MemberValuePair annotationValue : annotationValues)
        {
            String key = annotationValue.getName().toString();
            Expression valueExpression = annotationValue.getValue();
            String value;
            if (valueExpression instanceof StringLiteral)
                value = ((StringLiteral) valueExpression).getLiteralValue();
            else
                value = valueExpression.toString();
            annotationValueMap.put(key, value);
        }
        typeRef.setAnnotationValues(annotationValueMap);
    }

    @Override
    public boolean visit(MarkerAnnotation node)
    {
        ITypeBinding resolveTypeBinding = node.resolveTypeBinding();
        if (resolveTypeBinding != null)
        {
            processTypeBinding(resolveTypeBinding, TypeReferenceLocation.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(cu.getStartPosition()), cu.getLength());
        }
        else
        {
            String resolved = resolveClassname(node.getTypeName().toString());
            processTypeAsString(resolved, TypeReferenceLocation.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(cu.getStartPosition()), cu.getLength());
        }

        return super.visit(node);
    }

    @Override
    public boolean visit(NormalAnnotation node)
    {
        ITypeBinding resolveTypeBinding = node.resolveTypeBinding();
        JavaTypeReferenceModel typeRef;
        if (resolveTypeBinding != null)
        {
            typeRef = processTypeBinding(node.resolveTypeBinding(), TypeReferenceLocation.ANNOTATION,
                        cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength());
        }
        else
        {
            String name = node.getTypeName().toString();
            String resolved = resolveClassname(name);
            typeRef = processTypeAsString(resolved, TypeReferenceLocation.ANNOTATION,
                        cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength());
        }
        if (typeRef != null)
            // provide parameters of the annotation
            addAnnotationValues((JavaAnnotationTypeReferenceModel) typeRef, node);
        return super.visit(node);
    }

    @Override
    public boolean visit(SingleMemberAnnotation node)
    {
        // field annotation
        ITypeBinding resolveTypeBinding = node.resolveTypeBinding();
        if (resolveTypeBinding != null)
        {
            processTypeBinding(resolveTypeBinding, TypeReferenceLocation.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength());
        }
        else
        {
            String name = node.getTypeName().toString();
            String resolved = resolveClassname(name);
            processTypeAsString(resolved, TypeReferenceLocation.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength());
        }

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
                        ITypeBinding resolvedSuperInterface = ((SimpleType) clzInterface).resolveBinding();
                        Stack<ITypeBinding> stack = new Stack<ITypeBinding>();
                        stack.push(resolvedSuperInterface);
                        // register all the implemented interfaces (even superinterfaces)
                        while (!stack.isEmpty())
                        {
                            resolvedSuperInterface = stack.pop();
                            processTypeBinding(resolvedSuperInterface, TypeReferenceLocation.IMPLEMENTS_TYPE,
                                        cu.getLineNumber(node.getStartPosition()),
                                        cu.getColumnNumber(node.getStartPosition()), node.getLength());
                            if (resolvedSuperInterface != null)
                            {
                                ITypeBinding[] interfaces = resolvedSuperInterface.getInterfaces();
                                for (ITypeBinding oneInterface : interfaces)
                                {
                                    stack.push(oneInterface);
                                }
                            }
                        }
                    }
                    else
                    {
                        LOG.finer("" + clzInterface);
                    }
                }
            }
        }
        if (clzSuperClasses != null)
        {
            if (clzSuperClasses instanceof SimpleType)
            {
                ITypeBinding resolvedSuperClass = ((SimpleType) clzSuperClasses).resolveBinding();
                // register all the superClasses up to Object
                while (resolvedSuperClass != null && !resolvedSuperClass.getQualifiedName().equals("java.lang.Object"))
                {
                    processTypeBinding(resolvedSuperClass, TypeReferenceLocation.INHERITANCE, cu.getLineNumber(node.getStartPosition()),
                                cu.getColumnNumber(node.getStartPosition()), node.getLength());
                    resolvedSuperClass = resolvedSuperClass.getSuperclass();
                }
            }
            else
            {
                LOG.finer("" + clzSuperClasses);
            }
        }

        return super.visit(node);
    }

    /**
     * Declaration of the variable within a block
     */
    @Override
    public boolean visit(VariableDeclarationStatement node)
    {
        for (int i = 0; i < node.fragments().size(); ++i)
        {
            String nodeType = node.getType().toString();
            nodeType = resolveClassname(nodeType);
            VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(i);
            this.names.add(frag.getName().getIdentifier());
            this.nameInstance.put(frag.getName().toString(), nodeType.toString());
        }
        processType(node.getType(),TypeReferenceLocation.VARIABLE_DECLARATION,
                        cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength());
        return super.visit(node);
    }

    @Override
    public boolean visit(ImportDeclaration node)
    {
        String name = node.getName().toString();
        if (node.isOnDemand())
        {
            wildcardImports.add(name);

            Iterable<JavaClassModel> classModels = javaClassService.findByJavaPackage(name);
            for (JavaClassModel classModel : classModels)
            {
                processImport(classModel.getQualifiedName(), cu.getLineNumber(node.getName().getStartPosition()),
                            cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength());
            }
        }
        else
        {
            String clzName = StringUtils.substringAfterLast(name, ".");
            classNameLookedUp.add(clzName);
            classNameToFQCN.put(clzName, name);
            processImport(name, cu.getLineNumber(node.getName().getStartPosition()),
                        cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength());
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
        List<String> qualifiedInstances = new ArrayList<String>();
        List<String> argumentsQualified = new ArrayList<String>();
        // get qualified arguments of the method
        IMethodBinding resolveTypeBinding = node.resolveMethodBinding();
        if (resolveTypeBinding != null)
        {
            ITypeBinding[] arguments = resolveTypeBinding.getParameterTypes();

            for (ITypeBinding type : arguments)
            {
                argumentsQualified.add(type.getQualifiedName());
            }

            // find the interface declaring the method

            if (resolveTypeBinding != null && resolveTypeBinding.getDeclaringClass() != null)
            {
                ITypeBinding declaringClass = resolveTypeBinding.getDeclaringClass();
                qualifiedInstances.add(declaringClass.getQualifiedName());
                ITypeBinding[] interfaces = declaringClass.getInterfaces();
                // Now find all the implemented interfaces having the method called.
                for (ITypeBinding possibleInterface : interfaces)
                {
                    IMethodBinding[] declaredMethods = possibleInterface.getDeclaredMethods();
                    if (declaredMethods.length != 0)
                    {
                        for (IMethodBinding interfaceMethod : declaredMethods)
                        {
                            if (interfaceMethod.getName().equals(node.getName().toString()))
                            {

                                List<String> interfaceMethodArguments = new ArrayList<String>();
                                for (ITypeBinding type : interfaceMethod.getParameterTypes())
                                {
                                    interfaceMethodArguments.add(type.getQualifiedName());
                                }
                                if (interfaceMethodArguments.equals(argumentsQualified))
                                {
                                    qualifiedInstances.add(possibleInterface.getQualifiedName());
                                }
                            }
                        }
                    }

                }
            }

        }
        else
        {
            String nodeName = StringUtils.removeStart(node.toString(), "this.");
            String objRef = StringUtils.substringBefore(nodeName, "." + node.getName().toString());
            if (nameInstance.containsKey(objRef))
            {
                objRef = nameInstance.get(objRef);
            }
            objRef = resolveClassname(objRef);

            // not resolved binding
            List<Expression> arguments = node.arguments();
            for (Expression expression : arguments)
            {
                ITypeBinding argumentBinding = expression.resolveTypeBinding();
                if (argumentBinding != null)
                {
                    argumentsQualified.add(argumentBinding.getQualifiedName());
                }
                else
                {
                    // TODO: Is toString good option? Just a name of the argument will be saved
                    argumentsQualified.add(expression.toString());
                }
            }
            qualifiedInstances.add(objRef);
        }

        // register all found qualified names for this method invocation
        for (String qualifiedInstance : qualifiedInstances)
        {
            MethodType methodCall = new MethodType(qualifiedInstance, node.getName().toString(), argumentsQualified);
            processMethod(methodCall, cu.getLineNumber(node.getName().getStartPosition()),
                        cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength());
        }

        return super.visit(node);
    }

    @Override
    public boolean visit(PackageDeclaration node)
    {
        LOG.finer("Found package: " + node.getName().toString());
        return super.visit(node);
    }

    @Override
    public boolean visit(ClassInstanceCreation node)
    {
        IMethodBinding constructorBinding = node.resolveConstructorBinding();
        // ITypeBinding resolveTypeBinding = node.resolveTypeBinding();
        String qualifiedClass = "";
        List<String> constructorMethodQualifiedArguments = new ArrayList<String>();
        if (constructorBinding != null && constructorBinding.getDeclaringClass() != null)
        {
            ITypeBinding declaringClass = constructorBinding.getDeclaringClass();
            qualifiedClass = declaringClass.getQualifiedName();
            for (ITypeBinding type : constructorBinding.getParameterTypes())
            {
                constructorMethodQualifiedArguments.add(type.getQualifiedName());
            }
        }

        if (constructorMethodQualifiedArguments.isEmpty() && !node.arguments().isEmpty())
        {
            List<Expression> arguments = node.arguments();
            arguments.get(0).resolveTypeBinding();
            for (Expression type : arguments)
            {
                ITypeBinding argumentBinding = type.resolveTypeBinding();
                if (argumentBinding != null)
                {
                    constructorMethodQualifiedArguments.add(argumentBinding.getQualifiedName());
                }
                else
                {
                    List<String> guessedParam = methodParameterGuesser(Collections.singletonList(type));
                    constructorMethodQualifiedArguments.addAll(guessedParam);
                }

            }
        }

        // qualified class may not be resolved in case of anonymous classes
        if (qualifiedClass == null || qualifiedClass.equals(""))
        {
            qualifiedClass = node.getType().toString();
            qualifiedClass = resolveClassname(qualifiedClass);
        }

        ConstructorType resolvedConstructor = new ConstructorType(qualifiedClass, constructorMethodQualifiedArguments);
        processConstructor(resolvedConstructor, cu.getLineNumber(node.getType().getStartPosition()),
                    cu.getColumnNumber(node.getType().getStartPosition()), node.getType().getLength());

        return super.visit(node);
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

    private List<String> methodParameterGuesser(List<?> arguements)
    {
        List<String> resolvedParams = new ArrayList<String>(arguements.size());
        for (Object o : arguements)
        {
            if (o instanceof SimpleName)
            {
                String name = nameInstance.get(o.toString());
                if (name != null)
                {
                    resolvedParams.add(name);
                }
                else
                {
                    resolvedParams.add("Undefined");
                }
            }
            else if (o instanceof StringLiteral)
            {
                resolvedParams.add("java.lang.String");
            }
            else if (o instanceof FieldAccess)
            {
                String field = ((FieldAccess) o).getName().toString();
                if (names.contains(field))
                {
                    resolvedParams.add(nameInstance.get(field));
                }
                else
                {
                    resolvedParams.add("Undefined");
                }
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
                LOG.finer("Unable to determine type: " + o.getClass() + ReflectionToStringBuilder.toString(o));
                resolvedParams.add("Undefined");
            }
        }
        return resolvedParams;
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
                    Iterable<JavaClassModel> models = javaClassService.findAllByProperty(
                                JavaClassModel.QUALIFIED_NAME, candidateQualifiedName);
                    if (models.iterator().hasNext())
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

    private String qualifyType(String objRef)
    {
        // temporarily remove to resolve arrays
        objRef = StringUtils.removeEnd(objRef, "[]");
        if (nameInstance.containsKey(objRef))
        {
            objRef = nameInstance.get(objRef);
        }
        objRef = resolveClassname(objRef);
        return objRef;
    }

}
