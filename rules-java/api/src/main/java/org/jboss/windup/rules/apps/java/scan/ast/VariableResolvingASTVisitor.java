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
        String sourceString = interest;
        if (TypeInterestFactory.matchesAny(sourceString, TypeReferenceLocation.IMPORT))
        {
            sourceString = resolveClassname(sourceString);

            JavaTypeReferenceModel typeRef = typeRefService.createTypeReference(fileModel,
                        TypeReferenceLocation.IMPORT,
                        lineNumber, columnNumber, length, interest.toString());

            LOG.finer("Candidate: " + typeRef);
        }
    }

    private void processType(Type type, TypeReferenceLocation referenceLocation)
    {
        if (type == null)
            return;

        String sourceString = type.toString();
        sourceString = resolveClassname(sourceString);
        if (TypeInterestFactory.matchesAny(sourceString, referenceLocation))
        {
            int lineNumber = cu.getLineNumber(type.getStartPosition());
            int columnNumber = cu.getColumnNumber(type.getStartPosition());
            int length = type.getLength();

            JavaTypeReferenceModel typeRef = typeRefService.createTypeReference(fileModel, referenceLocation,
                        lineNumber, columnNumber, length, sourceString);

            LOG.finer("Prefix: " + referenceLocation);
            if (type instanceof SimpleType)
            {
                SimpleType sType = (SimpleType) type;
                LOG.finer("The type name is: " + sType.getName().getFullyQualifiedName() + " and " + sourceString);

            }
            LOG.finer("Candidate: " + typeRef);
        }
    }

    private JavaTypeReferenceModel processName(Name name, TypeReferenceLocation referenceLocation, int lineNumber, int columnNumber, int length)
    {
        if (name == null)
            return null;

        String sourceString = resolveClassname(name.toString());
        if (!TypeInterestFactory.matchesAny(sourceString, referenceLocation))
            return null;

        sourceString = resolveClassname(sourceString);

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
        Type returnType = node.getReturnType2();
        if (returnType != null)
        {
            processType(returnType, TypeReferenceLocation.RETURN_TYPE);
        }

        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) node.parameters();
        if (parameters != null)
        {
            for (SingleVariableDeclaration type : parameters)
            {
                // make it fully qualified.
                String typeName = type.getType().toString();
                typeName = resolveClassname(typeName);
                // now add it as a local variable.
                this.names.add(type.getName().toString());
                this.nameInstance.put(type.getName().toString(), typeName);

                processType(type.getType(), TypeReferenceLocation.METHOD_PARAMETER);
            }
        }

        @SuppressWarnings("unchecked")
        List<Name> throwsTypes = node.thrownExceptions();
        if (throwsTypes != null)
        {
            for (Name name : throwsTypes)
            {
                processName(name, TypeReferenceLocation.THROWS_METHOD_DECLARATION,
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
        processType(type, TypeReferenceLocation.INSTANCE_OF);

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.ThrowStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            processType(cic.getType(), TypeReferenceLocation.THROW_STATEMENT);
        }

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.CatchClause node)
    {
        Type catchType = node.getException().getType();
        processType(catchType, TypeReferenceLocation.CATCH_EXCEPTION_STATEMENT);

        return super.visit(node);
    }

    @Override
    public boolean visit(ReturnStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            processType(cic.getType(), TypeReferenceLocation.CONSTRUCTOR_CALL);
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

            processType(node.getType(), TypeReferenceLocation.FIELD_DECLARATION);
        }
        return true;
    }

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
        processName(node.getTypeName(), TypeReferenceLocation.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(cu.getStartPosition()), cu.getLength());
        return super.visit(node);
    }

    @Override
    public boolean visit(NormalAnnotation node)
    {
        JavaTypeReferenceModel typeRef = processName(node.getTypeName(), TypeReferenceLocation.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(node.getStartPosition()), node.getLength());
        if (typeRef != null)
            addAnnotationValues((JavaAnnotationTypeReferenceModel) typeRef, node);
        return super.visit(node);
    }

    @Override
    public boolean visit(SingleMemberAnnotation node)
    {
        processName(node.getTypeName(), TypeReferenceLocation.ANNOTATION, cu.getLineNumber(node.getStartPosition()),
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
                        processType((SimpleType) clzInterface, TypeReferenceLocation.IMPLEMENTS_TYPE);
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
                processType((SimpleType) clzSuperClasses, TypeReferenceLocation.INHERITANCE);
            }
            else
            {
                LOG.finer("" + clzSuperClasses);
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
            this.names.add(frag.getName().getIdentifier());
            this.nameInstance.put(frag.getName().toString(), nodeType.toString());
        }
        processType(node.getType(), TypeReferenceLocation.VARIABLE_DECLARATION);

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
            processImport(node.getName().toString(), cu.getLineNumber(node.getName().getStartPosition()),
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

        String nodeName = StringUtils.removeStart(node.toString(), "this.");

        List<?> arguments = node.arguments();
        List<String> resolvedParams = methodParameterGuesser(arguments);

        String objRef = StringUtils.substringBefore(nodeName, "." + node.getName().toString());

        if (nameInstance.containsKey(objRef))
        {
            objRef = nameInstance.get(objRef);
        }

        objRef = resolveClassname(objRef);

        MethodType methodCall = new MethodType(objRef, node.getName().toString(), resolvedParams);
        processMethod(methodCall, cu.getLineNumber(node.getName().getStartPosition()),
                    cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength());

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
        String nodeType = node.getType().toString();
        nodeType = resolveClassname(nodeType);

        List<String> resolvedParams = this.methodParameterGuesser(node.arguments());

        ConstructorType resolvedConstructor = new ConstructorType(nodeType, resolvedParams);
        processConstructor(resolvedConstructor, cu.getLineNumber(node.getType().getStartPosition()),
                    cu.getColumnNumber(node.getType().getStartPosition()), node.getType().getLength());

        return super.visit(node);
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

}
