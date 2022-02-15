package org.jboss.windup.ast.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.ast.java.data.annotations.AnnotationArrayValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationClassReference;
import org.jboss.windup.ast.java.data.annotations.AnnotationLiteralValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationValue;

/**
 * Provides the ability to parse a Java source file and return a {@link List} of {@link ClassReference} objects containing the fully qualified names
 * of all of the contained references. <b>Note: A new instance of this visitor should be constructed for each {@link CompilationUnit}</b>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ReferenceResolvingVisitor extends ASTVisitor
{
    private static final Logger LOG = Logger.getLogger(ReferenceResolvingVisitor.class.getName());

    private final WildcardImportResolver wildcardImportResolver;
    private final String path;
    private final CompilationUnit compilationUnit;
    private final List<ClassReference> classReferences = new ArrayList<>();
    private final ReferenceResolvingVisitorState state;
    private String packageName;
    private String className;

    public ReferenceResolvingVisitor(WildcardImportResolver importResolver, CompilationUnit compilationUnit, String path)
    {
        this.state = new ReferenceResolvingVisitorState();
        this.wildcardImportResolver = importResolver;
        this.compilationUnit = compilationUnit;
        this.path = path;

        resolveCurrentTypeNames(compilationUnit);
    }

    private void resolveCurrentTypeNames(CompilationUnit compilationUnit)
    {
        PackageDeclaration packageDeclaration = compilationUnit.getPackage();
        this.packageName = packageDeclaration == null ? "" : packageDeclaration.getName().getFullyQualifiedName();
        @SuppressWarnings("unchecked")
        List<TypeDeclaration> types = compilationUnit.types();

        @SuppressWarnings("unchecked")
        List<ImportDeclaration> importDeclarations = (List<ImportDeclaration>) compilationUnit.imports();

        for (ImportDeclaration importDeclaration : importDeclarations)
        {
            processImport(importDeclaration);
        }

        String fqcn = null;
        if (!types.isEmpty())
        {
            AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) types.get(0);
            this.className = typeDeclaration.getName().getFullyQualifiedName();

            if (packageName.isEmpty())
            {
                fqcn = className;
            }
            else
            {
                fqcn = packageName + "." + className;
            }

            String typeLine = typeDeclaration.toString();
            if (typeDeclaration.getJavadoc() != null)
            {
                typeLine = typeLine.substring(typeDeclaration.getJavadoc().toString().length());
            }
            ClassReference mainTypeClassReference = new ClassReference(fqcn, packageName, className, null, ResolutionStatus.RESOLVED, TypeReferenceLocation.TYPE,
                    compilationUnit.getLineNumber(typeDeclaration.getStartPosition()),
                    compilationUnit.getColumnNumber(compilationUnit.getStartPosition()),
                    compilationUnit.getLength(), extractDefinitionLine(typeLine));
            classReferences.add(mainTypeClassReference);
            processModifiers(mainTypeClassReference, typeDeclaration.modifiers());

            if (typeDeclaration instanceof TypeDeclaration)
            {
                Type superclassType = ((TypeDeclaration) typeDeclaration).getSuperclassType();
                ITypeBinding resolveBinding = null;
                if (superclassType != null)
                {
                    resolveBinding = superclassType.resolveBinding();
                }

                if (resolveBinding == null && superclassType != null)
                {
                    ResolveClassnameResult resolvedResult = resolveClassname(superclassType.toString());
                    PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedName(resolvedResult.result);
                    String superPackageName = packageAndClassName.packageName;
                    String superClassName = packageAndClassName.className;

                    ResolutionStatus superResolutionStatus = resolvedResult.found ? ResolutionStatus.RECOVERED : ResolutionStatus.UNRESOLVED;

                    classReferences.add(new ClassReference(resolvedResult.result, superPackageName, superClassName, null,
                                superResolutionStatus,
                                TypeReferenceLocation.TYPE, compilationUnit.getLineNumber(typeDeclaration.getStartPosition()),
                                compilationUnit.getColumnNumber(compilationUnit.getStartPosition()),
                                compilationUnit.getLength(),
                                extractDefinitionLine(typeDeclaration.toString())));
                }

                while (resolveBinding != null)
                {
                    if (superclassType.resolveBinding() != null)
                    {
                        String superPackageName = resolveBinding.getPackage().getName();
                        String superClassName = resolveBinding.getName();

                        classReferences.add(new ClassReference(resolveBinding.getQualifiedName(), superPackageName, superClassName, null,
                                    ResolutionStatus.RESOLVED,
                                    TypeReferenceLocation.TYPE, compilationUnit.getLineNumber(typeDeclaration.getStartPosition()),
                                    compilationUnit.getColumnNumber(compilationUnit.getStartPosition()),
                                    compilationUnit.getLength(),
                                    extractDefinitionLine(typeDeclaration.toString())));
                    }
                    resolveBinding = resolveBinding.getSuperclass();
                }
            }
        }

        state.getNames().add("this");
        state.getNameInstance().put("this", fqcn);
    }

    private String extractDefinitionLine(String typeDeclaration)
    {
        String typeLine = "";
        String[] lines = typeDeclaration.split(System.lineSeparator());
        for (String line : lines)
        {
            typeLine = line;
            if (line.contains("{"))
            {
                break;
            }
        }
        return typeLine;
    }

    public List<ClassReference> getJavaClassReferences()
    {
        return this.classReferences;
    }

    private ClassReference processConstructor(ConstructorType interest, ResolutionStatus resolutionStatus, int lineNumber,
                int columnNumber, int length, String line)
    {
        String text = interest.toString();
        ClassReference reference = new ClassReference(text, this.packageName, this.className, "<init>", resolutionStatus,
                TypeReferenceLocation.CONSTRUCTOR_CALL, lineNumber, columnNumber, length,
                line);
        this.classReferences.add(reference);
        return reference;
    }

    private ClassReference processMethod(MethodType interest, ResolutionStatus resolutionStatus, TypeReferenceLocation location, int lineNumber,
                int columnNumber, int length, String line, String returnType)
    {
        String text = interest.toString();
        ClassReference reference = new ClassReference(text, interest.packageName, interest.className, interest.methodName, resolutionStatus, location,
                lineNumber, columnNumber, length, line, returnType);
        this.classReferences.add(reference);
        return reference;
    }

    private void processImport(String interest, ResolutionStatus resolutionStatus, int lineNumber, int columnNumber, int length, String line)
    {
        final PackageAndClassName packageAndClass;
        if (!interest.contains("."))
        {
            packageAndClass = new PackageAndClassName(interest, null);
        }
        else
        {
            packageAndClass = PackageAndClassName.parseFromQualifiedName(interest);
        }

        this.classReferences
                    .add(new ClassReference(interest, packageAndClass.packageName, packageAndClass.className, null, resolutionStatus,
                                TypeReferenceLocation.IMPORT, lineNumber,
                                columnNumber,
                                length, line));
    }

    private ClassReference processTypeBinding(ITypeBinding type, ResolutionStatus resolutionStatus,
                TypeReferenceLocation referenceLocation, int lineNumber, int columnNumber,
                int length, String line)
    {
        if (type == null)
            return null;
        final String sourceString = getQualifiedName(type);
        final String packageName;
        if (type.getPackage() != null)
            packageName = type.getPackage().getName();
        else
            packageName = PackageAndClassName.parseFromQualifiedName(sourceString).packageName;

        final String className = type.getName();
        return processTypeAsString(sourceString, packageName, className, resolutionStatus, referenceLocation, lineNumber, columnNumber, length, line);
    }

    private ClassReference processTypeAsEnum(ITypeBinding typeBinding, Expression expression, ResolutionStatus resolutionStatus, int lineNumber, int columnNumber, int length, String line)
    {
        if (expression == null)
            return null;

        String fullExpression = null;
        String enumPackage = null;
        String enumClassName = null;
        if (typeBinding != null)
        {
            fullExpression = typeBinding.getQualifiedName();
            enumPackage = typeBinding.getPackage().getName();
            enumClassName = typeBinding.getName();
        }

        if (expression instanceof Name)
        {
            if (StringUtils.isNotBlank(fullExpression))
                fullExpression += ".";

            if (expression instanceof QualifiedName)
                fullExpression += ((QualifiedName) expression).getName();
            else
                fullExpression += ((Name) expression).getFullyQualifiedName();
        }

        ClassReference reference = new ClassReference(fullExpression, enumPackage, enumClassName, null, resolutionStatus,
                    TypeReferenceLocation.ENUM_CONSTANT,
                    lineNumber,
                    columnNumber,
                    length,
                    line);
        this.classReferences.add(reference);
        return reference;
    }
    
    /**
     * The method determines if the type can be resolved and if not, will try to guess the qualified name using the information from the imports.
     */
    private ClassReference processType(Type type, TypeReferenceLocation typeReferenceLocation, int lineNumber, int columnNumber, int length,
                String line)
    {
        if (type == null)
            return null;

        ITypeBinding resolveBinding = type.resolveBinding();
        if (resolveBinding == null)
        {
            ResolveClassnameResult resolvedResult = resolveClassname(type.toString());
            ResolutionStatus status = resolvedResult.found ? ResolutionStatus.RECOVERED : ResolutionStatus.UNRESOLVED;
            PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedName(resolvedResult.result);

            return processTypeAsString(resolvedResult.result, packageAndClassName.packageName, packageAndClassName.className, status,
                        typeReferenceLocation, lineNumber,
                        columnNumber, length, line);
        }
        else
        {
            return processTypeBinding(type.resolveBinding(), ResolutionStatus.RESOLVED, typeReferenceLocation, lineNumber,
                        columnNumber, length, line);
        }

    }

    private ClassReference processTypeAsString(String sourceString, String packageName, String className, ResolutionStatus resolutionStatus,
                TypeReferenceLocation referenceLocation,
                int lineNumber,
                int columnNumber, int length, String line)
    {
        if (sourceString == null)
            return null;
        line = line.replaceAll("(\\n)|(\\r)", "");
        ClassReference typeRef = new ClassReference(sourceString, packageName, className, null, resolutionStatus, referenceLocation, lineNumber,
                    columnNumber,
                    length,
                    line);
        this.classReferences.add(typeRef);
        return typeRef;
    }

    @Override
    public boolean visit(MethodDeclaration node)
    {
        // register method return type
        final ResolutionStatus resolutionStatus;
        IMethodBinding resolveBinding = node.resolveBinding();
        ITypeBinding returnType = null;
        if (resolveBinding != null)
        {
            resolutionStatus = ResolutionStatus.RESOLVED;
            returnType = node.resolveBinding().getReturnType();
        }
        else
        {
            resolutionStatus = ResolutionStatus.RECOVERED;
        }

        if (returnType != null)
        {
            processTypeBinding(returnType, ResolutionStatus.RESOLVED, TypeReferenceLocation.RETURN_TYPE,
                        compilationUnit.getLineNumber(node.getStartPosition()),
                        compilationUnit.getColumnNumber(node.getStartPosition()), node.getLength(), extractDefinitionLine(node.toString()));
        }
        else
        {
            Type methodReturnType = node.getReturnType2();
            processType(methodReturnType, TypeReferenceLocation.RETURN_TYPE, compilationUnit.getLineNumber(node.getStartPosition()),
                        compilationUnit.getColumnNumber(node.getStartPosition()), node.getLength(), extractDefinitionLine(node.toString()));
        }
        // register parameters and register them for next processing
        List<String> qualifiedArguments = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = node.parameters();
        if (parameters != null)
        {
            for (SingleVariableDeclaration type : parameters)
            {
                state.getNames().add(type.getName().toString());
                String typeName = type.getType().toString();
                typeName = resolveClassname(typeName).result;
                qualifiedArguments.add(typeName);
                state.getNameInstance().put(type.getName().toString(), typeName);

                ClassReference parameterClassReference = processType(type.getType(), TypeReferenceLocation.METHOD_PARAMETER, compilationUnit.getLineNumber(node.getStartPosition()),
                            compilationUnit.getColumnNumber(node.getStartPosition()), node.getLength(), extractDefinitionLine(node.toString()));
                processModifiers(parameterClassReference, type.modifiers());
            }
        }
        // register thow declarations
        @SuppressWarnings("unchecked")
        List<Type> throwsTypes = node.thrownExceptionTypes();
        if (throwsTypes != null)
        {
            for (Type type : throwsTypes)
            {
                processType(type, TypeReferenceLocation.THROWS_METHOD_DECLARATION,
                            compilationUnit.getLineNumber(node.getStartPosition()),
                            compilationUnit.getColumnNumber(type.getStartPosition()), type.getLength(), extractDefinitionLine(node.toString()));
            }
        }

        // register method declaration
        MethodType methodCall = new MethodType(state.getNameInstance().get("this"), this.packageName, this.className, node.getName().toString(),
                    qualifiedArguments);
        ClassReference methodReference = processMethod(methodCall, resolutionStatus, TypeReferenceLocation.METHOD, compilationUnit.getLineNumber(node.getName().getStartPosition()),
                    compilationUnit.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(),
                    extractDefinitionLine(node.toString()), (returnType != null) ? returnType.getQualifiedName() : null);
        processModifiers(methodReference, node.modifiers());
        return super.visit(node);
    }

    @Override
    public boolean visit(InstanceofExpression node)
    {
        Type type = node.getRightOperand();
        processType(type, TypeReferenceLocation.INSTANCE_OF, compilationUnit.getLineNumber(node.getStartPosition()),
                    compilationUnit.getColumnNumber(type.getStartPosition()), type.getLength(), node.toString());

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.ThrowStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            Type type = cic.getType();
            processType(type, TypeReferenceLocation.THROW_STATEMENT, compilationUnit.getLineNumber(node.getStartPosition()),
                        compilationUnit.getColumnNumber(cic.getStartPosition()), cic.getLength(), node.toString());
        }

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.CatchClause node)
    {
        Type catchType = node.getException().getType();
        processType(catchType, TypeReferenceLocation.CATCH_EXCEPTION_STATEMENT, compilationUnit.getLineNumber(node.getStartPosition()),
                    compilationUnit.getColumnNumber(catchType.getStartPosition()), catchType.getLength(), node.toString());

        return super.visit(node);
    }

    @Override
    public boolean visit(ReturnStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            ITypeBinding typeBinding = cic.getType().resolveBinding();
            if (typeBinding == null)
            {
                String qualifiedClass = cic.getType().toString();
                ResolveClassnameResult result = resolveClassname(qualifiedClass);
                qualifiedClass = result.result;
                ResolutionStatus resolutionStatus = result.found ? ResolutionStatus.RECOVERED : ResolutionStatus.UNRESOLVED;
                PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedName(qualifiedClass);

                processTypeAsString(qualifiedClass, packageAndClassName.packageName, packageAndClassName.className, resolutionStatus,
                            TypeReferenceLocation.CONSTRUCTOR_CALL,
                            compilationUnit.getLineNumber(node.getStartPosition()),
                            compilationUnit.getColumnNumber(cic.getStartPosition()), cic.getLength(), node.toString());
            }
            else
            {
                processTypeBinding(typeBinding, ResolutionStatus.RESOLVED, TypeReferenceLocation.CONSTRUCTOR_CALL,
                            compilationUnit.getLineNumber(node.getStartPosition()),
                            compilationUnit.getColumnNumber(cic.getStartPosition()), cic.getLength(), node.toString());
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node)
    {
        return super.visit(node);
    }

    @Override
    public boolean visit(FieldDeclaration node)
    {
        for (int i = 0; i < node.fragments().size(); ++i)
        {
            String nodeType = node.getType().toString();
            nodeType = resolveClassname(nodeType).result;
            VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(i);
            Expression expression = frag.getInitializer();
            
            final int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
            final int columnNumber = compilationUnit.getColumnNumber(node.getStartPosition());
            final ITypeBinding resolveBinding = node.getType().resolveBinding();
            
            if (expression instanceof Name)
            {
                Name expressionName = (Name) expression;
                IBinding binding = expressionName.resolveBinding();
                if (binding == null)
                {
                    ResolveClassnameResult result = resolveClassname(expressionName.getFullyQualifiedName());
                    ResolutionStatus status = result.found ? ResolutionStatus.RECOVERED : ResolutionStatus.UNRESOLVED;
                    // when dealing with FieldDeclaration with an initializer Expression of type Name (SimpleName or QualifiedName [1])
                    // with a null binding (i.e. not from code like "private int i = 0; private int j = i;"),
                    // it means we're in front of a constant from https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.28
                    // and hence we can use the PackageAndClassName.parseFromQualifiedNameWithConstant method
                    // [1] https://help.eclipse.org/2020-03/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/index.html
                    PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedNameWithConstant(result.result);

                    processTypeAsString(result.result,
                                packageAndClassName.packageName,
                                packageAndClassName.className,
                                status,
                                TypeReferenceLocation.VARIABLE_INITIALIZER,
                                lineNumber,
                                columnNumber, node.getLength(), node.toString());
                }
                else
                {   
                    //additionally add Enum Constant type reference
                    if (resolveBinding.isEnum())
                    {
                        processTypeAsEnum(resolveBinding, expressionName, ResolutionStatus.RESOLVED, lineNumber, columnNumber, node.getLength(), extractDefinitionLine(node.toString()));
                    }
                    
                    processTypeBinding(resolveBinding, ResolutionStatus.RESOLVED, TypeReferenceLocation.VARIABLE_INITIALIZER,
                                lineNumber,
                                columnNumber, node.getLength(), node.toString());
                }
            }

            state.getNames().add(frag.getName().getIdentifier());
            state.getNameInstance().put(frag.getName().toString(), nodeType.toString());

            ITypeBinding resolvedTypeBinding = resolveBinding;
            ClassReference reference;
            if (resolvedTypeBinding != null)
            {
                reference = processTypeBinding(resolvedTypeBinding, ResolutionStatus.RESOLVED, TypeReferenceLocation.FIELD_DECLARATION,
                        lineNumber,
                        columnNumber, node.getLength(), node.toString());
            }
            else
            {
                ResolveClassnameResult result = resolveClassname(node.getType().toString());
                ResolutionStatus status = result.found ? ResolutionStatus.RECOVERED : ResolutionStatus.UNRESOLVED;

                PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedName(result.result);
                reference = processTypeAsString(result.result,
                        packageAndClassName.packageName,
                        packageAndClassName.className,
                        status,
                        TypeReferenceLocation.FIELD_DECLARATION,
                        lineNumber,
                        columnNumber, node.getLength(), node.toString());
            }
            processModifiers(reference, node.modifiers());
        }
        return true;
    }

    private AnnotationValue getAnnotationValueForExpression(ClassReference annotatedReference, Expression expression)
    {
        AnnotationValue value;
        if (expression instanceof BooleanLiteral)
            value = new AnnotationLiteralValue(boolean.class, ((BooleanLiteral) expression).booleanValue());
        else if (expression instanceof CharacterLiteral)
            value = new AnnotationLiteralValue(char.class, ((CharacterLiteral) expression).charValue());
        else if (expression instanceof NumberLiteral)
        {
            ITypeBinding binding = expression.resolveTypeBinding();
            if (binding == null)
            {
                value = new AnnotationLiteralValue(String.class, expression.toString());
            }
            else
            {
                value = new AnnotationLiteralValue(resolveLiteralType(binding), ((NumberLiteral) expression).resolveConstantExpressionValue());
            }
        }
        else if (expression instanceof TypeLiteral)
        {
            Object literalValue = ((TypeLiteral) expression).resolveConstantExpressionValue();
            if (literalValue == null)
            {
                Type type = ((TypeLiteral) expression).getType();
                literalValue = type == null ? null : type.toString();
            }
            value = new AnnotationLiteralValue(Class.class, literalValue);
        }
        else if (expression instanceof StringLiteral)
            value = new AnnotationLiteralValue(String.class, ((StringLiteral) expression).getLiteralValue());
        else if (expression instanceof NormalAnnotation)
            value = processAnnotation(annotatedReference, (NormalAnnotation) expression);
        else if (expression instanceof ArrayInitializer)
        {
            ArrayInitializer arrayInitializer = (ArrayInitializer) expression;
            List<AnnotationValue> arrayValues = new ArrayList<>(arrayInitializer.expressions().size());
            for (Object arrayExpression : arrayInitializer.expressions())
            {
                arrayValues.add(getAnnotationValueForExpression(annotatedReference, (Expression) arrayExpression));
            }
            value = new AnnotationArrayValue(arrayValues);
        }
        else if (expression instanceof QualifiedName)
        {
            QualifiedName qualifiedName = (QualifiedName) expression;
            Object expressionValue = qualifiedName.resolveConstantExpressionValue();
            if (expressionValue == null)
            {
                value = new AnnotationLiteralValue(String.class, qualifiedName.toString());
            }
            else
            {
                Class<?> expressionType = expressionValue.getClass();
                value = new AnnotationLiteralValue(expressionType, expressionValue);
            }
        }
        else if (expression instanceof CastExpression)
        {
            CastExpression cast = (CastExpression) expression;
            AnnotationValue castExpressionValue = getAnnotationValueForExpression(annotatedReference, cast.getExpression());
            if (castExpressionValue instanceof AnnotationLiteralValue)
            {
                AnnotationLiteralValue literalValue = (AnnotationLiteralValue) castExpressionValue;
                ITypeBinding binding = cast.getType().resolveBinding();
                if (binding == null)
                {
                    value = new AnnotationLiteralValue(String.class, literalValue.getLiteralValue());
                }
                else
                {
                    Class<?> type = resolveLiteralType(cast.getType().resolveBinding());
                    value = new AnnotationLiteralValue(type, literalValue.getLiteralValue());
                }
            }
            else
            {
                value = castExpressionValue;
            }
        }
        else if (expression instanceof SimpleName)
        {
            SimpleName simpleName = (SimpleName) expression;
            ITypeBinding binding = simpleName.resolveTypeBinding();
            if (binding == null)
            {
                value = new AnnotationLiteralValue(String.class, simpleName.toString());
            }
            else
            {
                Object expressionValue = simpleName.resolveConstantExpressionValue();
                Class<?> expressionType = expressionValue == null ? null : expressionValue.getClass();
                value = new AnnotationLiteralValue(expressionType, expressionValue);
            }
        } else if (expression instanceof NullLiteral)
        {
            return new AnnotationLiteralValue(Object.class, null);
        } else
        {
            LOG.warning("Unexpected type: " + expression.getClass().getCanonicalName() + " in type: " + this.path
                        + " just attempting to use it as a string value");
            value = new AnnotationLiteralValue(String.class, expression == null ? null : expression.toString());
        }
        return value;

    }

    /**
     * Adds parameters contained in the annotation into the annotation type reference
     *
     * @param typeRef
     * @param node
     */
    private void addAnnotationValues(ClassReference annotatedReference, AnnotationClassReference typeRef, Annotation node)
    {
        Map<String, AnnotationValue> annotationValueMap = new HashMap<>();
        if (node instanceof SingleMemberAnnotation)
        {
            SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) node;
            AnnotationValue value = getAnnotationValueForExpression(annotatedReference, singleMemberAnnotation.getValue());
            annotationValueMap.put("value", value);
        }
        else if (node instanceof NormalAnnotation)
        {
            @SuppressWarnings("unchecked")
            List<MemberValuePair> annotationValues = ((NormalAnnotation) node).values();
            for (MemberValuePair annotationValue : annotationValues)
            {
                String key = annotationValue.getName().toString();
                Expression expression = annotationValue.getValue();
                AnnotationValue value = getAnnotationValueForExpression(annotatedReference, expression);
                annotationValueMap.put(key, value);
            }
        }
        typeRef.setAnnotationValues(annotationValueMap);
    }

    private Class<?> resolveLiteralType(ITypeBinding binding)
    {
        switch (binding.getName())
        {
        case "byte":
            return byte.class;
        case "short":
            return short.class;
        case "int":
            return int.class;
        case "long":
            return long.class;
        case "float":
            return float.class;
        case "double":
            return double.class;
        case "boolean":
            return boolean.class;
        case "char":
            return char.class;
        default:
            throw new ASTException("Unrecognized literal type: " + binding.getName());
        }
    }

    private AnnotationClassReference processAnnotation(ClassReference annotatedReference, Annotation node)
    {
        final ITypeBinding typeBinding = node.resolveTypeBinding();
        final AnnotationClassReference reference;
        final String qualifiedName;
        final String packageName;
        final String className;
        final ResolutionStatus status;
        if (typeBinding != null)
        {
            status = ResolutionStatus.RESOLVED;
            qualifiedName = typeBinding.getQualifiedName();
            packageName = typeBinding.getPackage().getName();
            className = typeBinding.getName();
        }
        else
        {
            ResolveClassnameResult result = resolveClassname(node.getTypeName().toString());
            status = result.found ? ResolutionStatus.RECOVERED : ResolutionStatus.UNRESOLVED;
            qualifiedName = result.result;

            PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedName(result.result);
            packageName = packageAndClassName.packageName;
            className = packageAndClassName.className;
        }

        reference = new AnnotationClassReference(
                    annotatedReference,
                    qualifiedName,
                    packageName,
                    className,
                    status,
                    compilationUnit.getLineNumber(node.getStartPosition()),
                    compilationUnit.getColumnNumber(node.getStartPosition()),
                    node.getLength(),
                    node.toString());

        addAnnotationValues(annotatedReference, reference, node);

        return reference;
    }

    private void processModifiers(ClassReference originalReference, List<?> modifiers)
    {
        for (Object modifier : modifiers)
        {
            if (modifier instanceof NormalAnnotation)
            {
                AnnotationClassReference reference = processAnnotation(originalReference, (NormalAnnotation)modifier);
                this.classReferences.add(reference);
            }
            else if (modifier instanceof SingleMemberAnnotation)
            {
                AnnotationClassReference reference = processAnnotation(originalReference, (SingleMemberAnnotation)modifier);
                this.classReferences.add(reference);
            }
            else if (modifier instanceof MarkerAnnotation)
            {
                AnnotationClassReference reference = processAnnotation(originalReference, (MarkerAnnotation)modifier);
                this.classReferences.add(reference);
            } else if (modifier instanceof Modifier)
            {
                // throw if this is an annotation (as we should be processing all of those), ignore otherwise
                if (((Modifier)modifier).isAnnotation())
                    throw new RuntimeException("Failed due to unexpected Modifier that is also an annotation: " + modifier.getClass().getCanonicalName());

            }
            else
            {
                throw new RuntimeException("Failed due to unexpected type: " + modifier.getClass().getCanonicalName());
            }
        }
    }

//    @Override
//    public boolean visit(NormalAnnotation node)
//    {
//        AnnotationClassReference reference = processAnnotation(node);
//        this.classReferences.add(reference);
//
//        // false to avoid recursively processing nested annotations (our code already handles that)
//        return false;
//    }
//
//    @Override
//    public boolean visit(SingleMemberAnnotation node)
//    {
//        AnnotationClassReference reference = processAnnotation(node);
//        this.classReferences.add(reference);
//        return false;
//    }
//
//    @Override
//    public boolean visit(MarkerAnnotation node)
//    {
//        AnnotationClassReference reference = processAnnotation(node);
//        this.classReferences.add(reference);
//        return false;
//    }

    public boolean visit(TypeDeclaration node)
    {
        Object clzInterfaces = node.getStructuralProperty(TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
        Object clzSuperClasses = node.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY);

        if (clzInterfaces != null)
        {
            if (List.class.isAssignableFrom(clzInterfaces.getClass()))
            {
                TypeReferenceLocation typeReferenceLocation = node.isInterface() ? TypeReferenceLocation.INHERITANCE
                            : TypeReferenceLocation.IMPLEMENTS_TYPE;

                List<?> clzInterfacesList = (List<?>) clzInterfaces;
                for (Object clzInterface : clzInterfacesList)
                {
                    ParameterizedType parameterizedType = null;
                    if (clzInterface instanceof ParameterizedType)
                    {
                        parameterizedType = (ParameterizedType) clzInterface;
                        clzInterface = parameterizedType.getType();
                    }

                    if (clzInterface instanceof SimpleType)
                    {
                        SimpleType simpleType = (SimpleType) clzInterface;
                        ITypeBinding resolvedSuperInterface = simpleType.resolveBinding();
                        if (resolvedSuperInterface == null)
                        {
                            ResolveClassnameResult result = resolveClassname(simpleType.getName().toString());
                            ResolutionStatus status = result.found ? ResolutionStatus.RECOVERED : ResolutionStatus.UNRESOLVED;
                            PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedName(result.result);
                            processTypeAsString(result.result, packageAndClassName.packageName, packageAndClassName.className, status,
                                        typeReferenceLocation,
                                        compilationUnit.getLineNumber(node.getStartPosition()),
                                        compilationUnit.getColumnNumber(node.getStartPosition()),
                                        node.getLength(), extractDefinitionLine(node.toString()));
                        }
                        else
                        {
                            /*
                             * Register all the implemented interfaces (even super interfaces, if we are able to resolve them.)
                             */
                            Stack<ITypeBinding> stack = new Stack<>();
                            stack.push(resolvedSuperInterface);
                            while (!stack.isEmpty())
                            {
                                resolvedSuperInterface = stack.pop();
                                processTypeBinding(resolvedSuperInterface, ResolutionStatus.RESOLVED, typeReferenceLocation,
                                            compilationUnit.getLineNumber(node.getStartPosition()),
                                            compilationUnit.getColumnNumber(node.getStartPosition()), node.getLength(),
                                            extractDefinitionLine(node.toString()));
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
                    }
                }
            }
        }
        if (clzSuperClasses != null)
        {
            if (clzSuperClasses instanceof SimpleType)
            {
                ITypeBinding resolvedSuperClass = ((SimpleType) clzSuperClasses).resolveBinding();

                if (resolvedSuperClass == null)
                {
                    ResolveClassnameResult result = resolveClassname(((SimpleType) clzSuperClasses).getName().toString());
                    ResolutionStatus status = result.found ? ResolutionStatus.RECOVERED : ResolutionStatus.UNRESOLVED;
                    PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedName(result.result);
                    processTypeAsString(result.result, packageAndClassName.packageName, packageAndClassName.className, status,
                                TypeReferenceLocation.INHERITANCE,
                                compilationUnit.getLineNumber(node.getStartPosition()),
                                compilationUnit.getColumnNumber(node.getStartPosition()),
                                node.getLength(), extractDefinitionLine(node.toString()));
                }

                // register all the superClasses up to Object
                while (resolvedSuperClass != null && !resolvedSuperClass.getQualifiedName().equals("java.lang.Object"))
                {
                    processTypeBinding(resolvedSuperClass, ResolutionStatus.RESOLVED, TypeReferenceLocation.INHERITANCE,
                                compilationUnit.getLineNumber(node.getStartPosition()),
                                compilationUnit.getColumnNumber(node.getStartPosition()), node.getLength(), extractDefinitionLine(node.toString()));

                    for (ITypeBinding iface : resolvedSuperClass.getInterfaces())
                    {
                        processTypeBinding(iface, ResolutionStatus.RESOLVED, TypeReferenceLocation.IMPLEMENTS_TYPE,
                                    compilationUnit.getLineNumber(node.getStartPosition()),
                                    compilationUnit.getColumnNumber(node.getStartPosition()), node.getLength(),
                                    extractDefinitionLine(node.toString()));
                    }
                    resolvedSuperClass = resolvedSuperClass.getSuperclass();
                }
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
            VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(i);
            state.getNames().add(frag.getName().getIdentifier());
            state.getNameInstance().put(frag.getName().toString(), nodeType.toString());
        }

        processType(node.getType(), TypeReferenceLocation.VARIABLE_DECLARATION,
                    compilationUnit.getLineNumber(node.getStartPosition()),
                    compilationUnit.getColumnNumber(node.getStartPosition()), node.getLength(), node.toString());
        return super.visit(node);
    }

    private void processImport(ImportDeclaration node)
    {
        String name = node.getName().toString();
        if (node.isOnDemand())
        {
            state.getWildcardImports().add(name);

            String[] resolvedNames = this.wildcardImportResolver.resolve(name);
            for (String resolvedName : resolvedNames)
            {
                processImport(resolvedName, ResolutionStatus.RESOLVED, compilationUnit.getLineNumber(node.getName().getStartPosition()),
                            compilationUnit.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(), node.toString().trim());
            }

            /*
             * Also, register the wildcard itself so that we can have rules that match against wildcard imports, event if we do not know what classes
             * may be contained in that wildcard
             */
            processImport(name + ".*", ResolutionStatus.RESOLVED, compilationUnit.getLineNumber(node.getName().getStartPosition()),
                        compilationUnit.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(), node.toString().trim());
        }
        else
        {
            String clzName = StringUtils.substringAfterLast(name, ".");
            state.getClassNameLookedUp().add(clzName);
            state.getClassNameToFQCN().put(clzName, name);
            ResolutionStatus status = node.resolveBinding() != null ? ResolutionStatus.RESOLVED : ResolutionStatus.RECOVERED;
            processImport(name, status, compilationUnit.getLineNumber(node.getName().getStartPosition()),
                        compilationUnit.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(), node.toString().trim());
        }
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
        List<String> qualifiedInstances = new ArrayList<>();
        List<String> argumentsQualified = new ArrayList<>();
        // get qualified arguments of the method
        IMethodBinding resolveTypeBinding = node.resolveMethodBinding();
        final ResolutionStatus resolutionStatus;
        int columnNumber = compilationUnit.getColumnNumber(node.getName().getStartPosition());
        int length = node.getName().getLength();
        if (resolveTypeBinding != null)
        {
            resolutionStatus = ResolutionStatus.RESOLVED;
            ITypeBinding[] argumentTypeBindings = resolveTypeBinding.getParameterTypes();
            List<Expression> arguments = node.arguments();
            int index = 0;
            for (ITypeBinding type : argumentTypeBindings)
            {
                argumentsQualified.add(type.getQualifiedName());

                if (type.isEnum())
                {
                    // there is different number of passed arguments and possible arguments from declaration
                    if (arguments.size() > index)
                    {
                        Expression expression = arguments.get(index);
                        processTypeAsEnum(type, expression, resolutionStatus,
                                compilationUnit.getLineNumber(node.getName().getStartPosition()),
                                columnNumber,
                                length, extractDefinitionLine(node.toString()));
                    }
                    index++;
                }
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
                                List<String> interfaceMethodArguments = new ArrayList<>();
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
            resolutionStatus = ResolutionStatus.RECOVERED;
            String nodeName = StringUtils.removeStart(node.toString(), "this.");
            String objRef = StringUtils.substringBefore(nodeName, "." + node.getName().toString());
            if (state.getNameInstance().containsKey(objRef))
            {
                objRef = state.getNameInstance().get(objRef);
            }
            objRef = resolveClassname(objRef).result;

            @SuppressWarnings("unchecked")
            List<Expression> arguments = node.arguments();
            for (Expression expression : arguments)
            {
                ITypeBinding argumentBinding = expression.resolveTypeBinding();
                if (argumentBinding != null)
                {
                    argumentsQualified.add(argumentBinding.getQualifiedName());

                    if (argumentBinding.isEnum())
                    {
                        // FIXME -- Test
                        String constantEnum = expression.toString();
                        if (constantEnum != null)
                        {
                            processTypeAsEnum(argumentBinding, expression, ResolutionStatus.RESOLVED, compilationUnit.getLineNumber(node.getName().getStartPosition()),
                                    columnNumber, length, extractDefinitionLine(node.toString()));
                        }
                    }
                }
                else
                {
                    PackageAndClassName argumentQualifiedGuess = PackageAndClassName.parseFromQualifiedName(expression.toString());
                    argumentsQualified.add(argumentQualifiedGuess.toString());
                }
            }
            qualifiedInstances.add(objRef);
        }

        for (String qualifiedInstance : qualifiedInstances)
        {
            PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedName(qualifiedInstance);
            MethodType methodCall = new MethodType(qualifiedInstance, packageAndClassName.packageName, packageAndClassName.className,
                        node.getName().toString(), argumentsQualified);
            processMethod(methodCall, resolutionStatus, TypeReferenceLocation.METHOD_CALL,
                        compilationUnit.getLineNumber(node.getName().getStartPosition()),
                        columnNumber, length, node.toString(), null);
        }

        return super.visit(node);
    }

    @Override
    public boolean visit(PackageDeclaration node)
    {
        return super.visit(node);
    }

    private String getQualifiedName(ITypeBinding typeBinding)
    {
        if (typeBinding == null)
            return null;

        String qualifiedName = typeBinding.getQualifiedName();

        if (StringUtils.isEmpty(qualifiedName))
        {
            if (typeBinding.isAnonymous())
            {
                if (typeBinding.getSuperclass() != null)
                    qualifiedName = getQualifiedName(typeBinding.getSuperclass());
                else if (typeBinding instanceof AnonymousClassDeclaration)
                {
                    qualifiedName = ((AnonymousClassDeclaration) typeBinding).toString();
                }
            }
            else if (StringUtils.isEmpty(qualifiedName) && typeBinding.isNested())
                qualifiedName = typeBinding.getName();
        }

        return qualifiedName;
    }

    @Override
    public boolean visit(ClassInstanceCreation node)
    {
        final int lineNumber = compilationUnit.getLineNumber(node.getType().getStartPosition());
        final int columnNumber = compilationUnit.getColumnNumber(node.getType().getStartPosition());
        final int length = node.getType().getLength();

        IMethodBinding constructorBinding = node.resolveConstructorBinding();
        String qualifiedClass = "";
        List<String> constructorMethodQualifiedArguments = new ArrayList<>();
        if (constructorBinding != null && constructorBinding.getDeclaringClass() != null)
        {
            ITypeBinding declaringClass = constructorBinding.getDeclaringClass();
            qualifiedClass = getQualifiedName(declaringClass);

            @SuppressWarnings("unchecked")
            List<Expression> arguments = node.arguments();
            int index = 0;
            for (ITypeBinding type : constructorBinding.getParameterTypes())
            {
                if (type.isEnum())
                {
                    // there is different number of passed arguments and possible arguments from declaration
                    if (arguments.size() > index)
                    {
                        Expression argument = arguments.get(index);
                        processTypeAsEnum(type, argument, ResolutionStatus.RESOLVED, lineNumber, columnNumber, length, extractDefinitionLine(node.toString()));
                    }
                }
                index++;

                String qualifiedArgumentClass = type.getQualifiedName();
                if (qualifiedArgumentClass != null)
                {
                    constructorMethodQualifiedArguments.add(qualifiedArgumentClass);
                }
            }
        }

        if (constructorMethodQualifiedArguments.isEmpty() && !node.arguments().isEmpty())
        {
            @SuppressWarnings("unchecked")
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

        final ResolutionStatus resolutionStatus;
        /*
         * Qualified class may not be resolved in case of anonymous classes
         */
        if (qualifiedClass == null || qualifiedClass.isEmpty())
        {
            qualifiedClass = node.getType().toString();
            ResolveClassnameResult result = resolveClassname(qualifiedClass);
            qualifiedClass = result.result;
            resolutionStatus = result.found ? ResolutionStatus.RECOVERED : ResolutionStatus.UNRESOLVED;
        }
        else
        {
            resolutionStatus = ResolutionStatus.RESOLVED;
        }

        ConstructorType resolvedConstructor = new ConstructorType(qualifiedClass, constructorMethodQualifiedArguments);
        processConstructor(resolvedConstructor, resolutionStatus, lineNumber, columnNumber, length, node.toString());

        return super.visit(node);
    }

    private List<String> methodParameterGuesser(List<?> arguments)
    {
        List<String> resolvedParams = new ArrayList<>(arguments.size());
        for (Object o : arguments)
        {
            if (o instanceof SimpleName)
            {
                String name = state.getNameInstance().get(o.toString());
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
                if (state.getNames().contains(field))
                {
                    resolvedParams.add(state.getNameInstance().get(field));
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
                nodeType = resolveClassname(nodeType).result;
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
            else if (o instanceof QualifiedName)
            {
                String paramGuessed = ((QualifiedName) o).getFullyQualifiedName();
                PackageAndClassName qualifiedParam = PackageAndClassName.parseFromQualifiedName(paramGuessed);
                resolvedParams.add(qualifiedParam.toString());
            }
            else
            {
                resolvedParams.add("Undefined");
            }
        }
        return resolvedParams;
    }

    private ResolveClassnameResult resolveClassname(String sourceClassname)
    {
        /*
         * If the type contains a "." assume that it is fully qualified.
         *
         * FIXME - This is a carryover from the original Windup code, and I don't think that this assumption is valid.
         */
        if (!StringUtils.contains(sourceClassname, "."))
        {
            if (state.getClassNameLookedUp().contains(sourceClassname))
            {
                String qualifiedName = state.getClassNameToFQCN().get(sourceClassname);
                if (qualifiedName != null)
                {
                    return new ResolveClassnameResult(true, qualifiedName);
                }
                else
                {
                    return new ResolveClassnameResult(false, sourceClassname);
                }
            }
            else
            {
                state.getClassNameLookedUp().add(sourceClassname);
                String resolvedClassName = this.wildcardImportResolver.resolve(this.state.getWildcardImports(), sourceClassname);
                if (resolvedClassName != null)
                {
                    state.getClassNameToFQCN().put(sourceClassname, resolvedClassName);
                    return new ResolveClassnameResult(true, resolvedClassName);
                }
                return new ResolveClassnameResult(false, sourceClassname);
            }
        }
        else
        {
            return new ResolveClassnameResult(true, sourceClassname);
        }
    }

    private String qualifyType(String objRef)
    {
        /*
         * Temporarily remove '[]' to resolve array types.
         */
        objRef = StringUtils.removeEnd(objRef, "[]");
        if (state.getNameInstance().containsKey(objRef))
        {
            objRef = state.getNameInstance().get(objRef);
        }
        objRef = resolveClassname(objRef).result;
        return objRef;
    }

    private static class MethodType
    {
        private final String qualifiedName;
        private final String packageName;
        private final String className;
        private final String methodName;
        private final List<String> qualifiedParameters;

        MethodType(String qualifiedName, String packageName, String className, String methodName, List<String> qualifiedParameters)
        {
            this.qualifiedName = qualifiedName;
            this.packageName = packageName;
            this.className = className;
            this.methodName = methodName;

            if (qualifiedParameters != null)
            {
                this.qualifiedParameters = qualifiedParameters;
            }
            else
            {
                this.qualifiedParameters = new LinkedList<>();
            }
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(qualifiedName).append(".").append(methodName);
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

    private static class ConstructorType
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
                this.qualifiedParameters = new LinkedList<>();
            }

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

    private static class PackageAndClassName
    {
        private String packageName;
        private String className;

        public PackageAndClassName(String packageName, String className)
        {
            this.packageName = packageName;
            this.className = className;
        }
        
        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            if (this.packageName != null)
            {
                sb.append(this.packageName).append(".");
            }
            if (this.className != null)
                sb.append(this.className);

            return sb.toString();
        }
        
        public static PackageAndClassName parseFromQualifiedName(String qualifiedName)
        {
            final String packageName;
            final String className;

            // remove the .* if this was a package import
            if (qualifiedName.contains(".*"))
            {
                packageName = qualifiedName.replace(".*", "");
                className = null;
            }
            else
            {
                int lastDot = qualifiedName.lastIndexOf('.');
                if (lastDot == -1)
                {
                    packageName = null;
                    className = qualifiedName;
                }
                else
                {
                    packageName = qualifiedName.substring(0, lastDot);
                    className = qualifiedName.substring(lastDot + 1);
                }
            }
            return new PackageAndClassName(packageName, className);
        }

        private static PackageAndClassName parseFromQualifiedNameWithConstant(String qualifiedName)
        {
            int lastDot = qualifiedName.lastIndexOf('.');
            return lastDot > -1 ? parseFromQualifiedName(qualifiedName.substring(0, lastDot)) : parseFromQualifiedName(qualifiedName);
        }
    }

    private class ResolveClassnameResult
    {
        private boolean found;
        private String result;

        public ResolveClassnameResult(boolean found, String result)
        {
            this.found = found;
            this.result = result;
        }
    }
}
