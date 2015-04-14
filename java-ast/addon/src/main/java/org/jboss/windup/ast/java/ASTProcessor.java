package org.jboss.windup.ast.java;

import java.io.IOException;
import java.nio.file.Path;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
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
import org.jboss.windup.ast.java.data.ClassReferences;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.ast.java.data.annotations.AnnotationArrayValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationClassReference;
import org.jboss.windup.ast.java.data.annotations.AnnotationLiteralValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationValue;

/**
 * Provides the ability to parse a Java file and return a {@link ClassReferences} object containing the fully qualified names of all of the contained
 * references.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class ASTProcessor extends ASTVisitor
{
    private static Logger LOG = Logger.getLogger(ASTProcessor.class.getName());

    private final WildcardImportResolver wildcardImportResolver;
    private CompilationUnit cu;
    private Path javaFile;
    private ASTParser parser;

    private final Set<String> libraryPaths;
    private final Set<String> sourcePaths;

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

    private ClassReferences classReferences;

    /**
     * Processes a java file using the default {@link WildcardImportResolver}.
     * 
     * See also: {@see JavaASTProcessor#analyzeJavaFile(WildcardImportResolver, Set, Set, Path)}
     */
    public static ClassReferences analyzeJavaFile(Set<String> libraryPaths, Set<String> sourcePaths, Path sourceFile)
    {
        return new ASTProcessor(new NoopWildcardImportResolver(), libraryPaths, sourcePaths).analyzeFile(sourceFile);
    }

    /**
     * Parses the provided file, using the given libraryPaths and sourcePaths as context. The libraries may be either jar files or references to
     * directories containing class files.
     * 
     * The sourcePaths must be a reference to the top level directory for sources (eg, for a file src/main/java/org/example/Foo.java, the source path
     * would be src/main/java).
     * 
     * The wildcard resolver provides a fallback for processing wildcard imports that the underlying parser was unable to resolve.
     */
    public static ClassReferences analyzeJavaFile(WildcardImportResolver importResolver, Set<String> libraryPaths, Set<String> sourcePaths,
                Path sourceFile)
    {
        return new ASTProcessor(importResolver, libraryPaths, sourcePaths).analyzeFile(sourceFile);
    }

    public ASTProcessor(WildcardImportResolver importResolver, Set<String> libraryPaths, Set<String> sourcePaths)
    {
        this.wildcardImportResolver = importResolver;
        this.parser = ASTParser.newParser(AST.JLS8);
        this.libraryPaths = libraryPaths;
        this.sourcePaths = sourcePaths;
    }

    public ClassReferences analyzeFile(Path javaFile)
    {
        this.classReferences = new ClassReferences();
        this.wildcardImports.clear();
        this.classNameLookedUp.clear();
        this.classNameToFQCN.clear();
        this.names.clear();
        this.nameInstance.clear();
        this.javaFile = javaFile;

        parser.setEnvironment(libraryPaths.toArray(new String[libraryPaths.size()]), sourcePaths.toArray(new String[sourcePaths.size()]), null, true);
        parser.setBindingsRecovery(false);
        parser.setResolveBindings(true);

        String fileName = javaFile.getFileName().toString();
        parser.setUnitName(fileName);
        try
        {
            parser.setSource(FileUtils.readFileToString(javaFile.toFile()).toCharArray());
        }
        catch (IOException e)
        {
            throw new ASTException("Failed to get source for file: " + javaFile.toString() + " due to: " + e.getMessage(), e);
        }
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        this.cu = (CompilationUnit) parser.createAST(null);

        PackageDeclaration packageDeclaration = cu.getPackage();
        String packageName = packageDeclaration == null ? "" : packageDeclaration.getName().getFullyQualifiedName();
        @SuppressWarnings("unchecked")
        List<TypeDeclaration> types = cu.types();
        String fqcn = null;
        if (!types.isEmpty())
        {
            TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
            String className = typeDeclaration.getName().getFullyQualifiedName();

            if (packageName.equals(""))
            {
                fqcn = className;
            }
            else
            {
                fqcn = packageName + "." + className;
            }
            this.classReferences.addReference(new ClassReference(fqcn, TypeReferenceLocation.TYPE, cu.getLineNumber(typeDeclaration
                        .getStartPosition()), cu.getColumnNumber(cu.getStartPosition()), cu.getLength(), extractDefinitionLine(typeDeclaration
                        .toString())));

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
                    this.classReferences.addReference(new ClassReference(resolveBinding.getQualifiedName(), TypeReferenceLocation.TYPE, cu
                                .getLineNumber(typeDeclaration
                                            .getStartPosition()), cu.getColumnNumber(cu.getStartPosition()), cu.getLength(),
                                extractDefinitionLine(typeDeclaration.toString())));
                }
                resolveBinding = resolveBinding.getSuperclass();
            }
        }

        this.names.add("this");
        this.nameInstance.put("this", fqcn);

        cu.accept(this);
        return this.classReferences;
    }

    private String extractDefinitionLine(String typeDeclaration)
    {
        String typeLine = "";
        String[] lines = typeDeclaration.split("\n");
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

    public ClassReferences getJavaClassReferences()
    {
        return this.classReferences;
    }

    private void processConstructor(ConstructorType interest, int lineNumber, int columnNumber, int length, String line)
    {
        String text = interest.toString();
        this.classReferences.addReference(new ClassReference(text, TypeReferenceLocation.CONSTRUCTOR_CALL, lineNumber, columnNumber, length,
                    line));
    }

    private void processMethod(MethodType interest, TypeReferenceLocation location, int lineNumber, int columnNumber, int length, String line)
    {
        String text = interest.toString();
        this.classReferences.addReference(new ClassReference(text, location, lineNumber, columnNumber, length, line));
    }

    private void processImport(String interest, int lineNumber, int columnNumber, int length, String line)
    {
        this.classReferences.addReference(new ClassReference(interest, TypeReferenceLocation.IMPORT, lineNumber, columnNumber, length, line));
    }

    private ClassReference processTypeBinding(ITypeBinding type, TypeReferenceLocation referenceLocation, int lineNumber, int columnNumber,
                int length, String line)
    {
        if (type == null)
            return null;
        String sourceString = type.getQualifiedName();
        return processTypeAsString(sourceString, referenceLocation, lineNumber, columnNumber, length, line);
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
            return processTypeAsString(resolveClassname(type.toString()), typeReferenceLocation, lineNumber,
                        columnNumber, length, line);
        }
        else
        {
            return processTypeBinding(type.resolveBinding(), typeReferenceLocation, lineNumber,
                        columnNumber, length, line);
        }

    }

    private ClassReference processTypeAsString(String sourceString, TypeReferenceLocation referenceLocation, int lineNumber,
                int columnNumber, int length, String line)
    {
        if (sourceString == null)
            return null;
        line = line.replaceAll("(\\n)|(\\r)", "");
        ClassReference typeRef = new ClassReference(sourceString, referenceLocation, lineNumber, columnNumber, length, line);
        this.classReferences.addReference(typeRef);
        return typeRef;
    }

    @Override
    public boolean visit(MethodDeclaration node)
    {
        //register method return type
        IMethodBinding resolveBinding = node.resolveBinding();
        ITypeBinding returnType = null;
        if (resolveBinding != null)
        {
            returnType = node.resolveBinding().getReturnType();
        }
        if (returnType != null)
        {
            processTypeBinding(returnType, TypeReferenceLocation.RETURN_TYPE, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength(), extractDefinitionLine(node.toString()));
        }
        else
        {
            Type methodReturnType = node.getReturnType2();
            processType(methodReturnType, TypeReferenceLocation.RETURN_TYPE, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(node.getStartPosition()), node.getLength(), extractDefinitionLine(node.toString()));
        }
        // register parameters and register them for next processing
        List<String> qualifiedArguments = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) node.parameters();
        if (parameters != null)
        {
            for (SingleVariableDeclaration type : parameters)
            {
                this.names.add(type.getName().toString());
                String typeName = type.getType().toString();
                typeName = resolveClassname(typeName);
                qualifiedArguments.add(typeName);
                this.nameInstance.put(type.getName().toString(), typeName);
                processType(type.getType(), TypeReferenceLocation.METHOD_PARAMETER, cu.getLineNumber(node.getStartPosition()),
                            cu.getColumnNumber(node.getStartPosition()), node.getLength(), extractDefinitionLine(node.toString()));
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
                            cu.getLineNumber(node.getStartPosition()),
                            cu.getColumnNumber(type.getStartPosition()), type.getLength(), extractDefinitionLine(node.toString()));
            }
        }

        // register method declaration
        MethodType methodCall = new MethodType(nameInstance.get("this"), node.getName().toString(), qualifiedArguments);
        processMethod(methodCall, TypeReferenceLocation.METHOD, cu.getLineNumber(node.getName().getStartPosition()),
                    cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(), extractDefinitionLine(node.toString()));
        return super.visit(node);
    }

    @Override
    public boolean visit(InstanceofExpression node)
    {
        Type type = node.getRightOperand();
        processType(type, TypeReferenceLocation.INSTANCE_OF, cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(type.getStartPosition()), type.getLength(), node.toString());

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.ThrowStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            Type type = cic.getType();
            processType(type, TypeReferenceLocation.THROW_STATEMENT, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(cic.getStartPosition()), cic.getLength(), node.toString());
        }

        return super.visit(node);
    }

    public boolean visit(org.eclipse.jdt.core.dom.CatchClause node)
    {
        Type catchType = node.getException().getType();
        processType(catchType, TypeReferenceLocation.CATCH_EXCEPTION_STATEMENT, cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(catchType.getStartPosition()), catchType.getLength(), node.toString());

        return super.visit(node);
    }

    @Override
    public boolean visit(ReturnStatement node)
    {
        if (node.getExpression() instanceof ClassInstanceCreation)
        {
            ClassInstanceCreation cic = (ClassInstanceCreation) node.getExpression();
            processTypeBinding(cic.getType().resolveBinding(), TypeReferenceLocation.CONSTRUCTOR_CALL, cu.getLineNumber(node.getStartPosition()),
                        cu.getColumnNumber(cic.getStartPosition()), cic.getLength(), node.toString());
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
                        cu.getColumnNumber(node.getStartPosition()), node.getLength(), node.toString());
        }
        return true;
    }

    private AnnotationValue getAnnotationValueForExpression(Expression expression)
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
            value = new AnnotationLiteralValue(Class.class, ((TypeLiteral) expression).resolveConstantExpressionValue());
        else if (expression instanceof StringLiteral)
            value = new AnnotationLiteralValue(String.class, ((StringLiteral) expression).getLiteralValue());
        else if (expression instanceof NormalAnnotation)
            value = processAnnotation((NormalAnnotation) expression);
        else if (expression instanceof ArrayInitializer)
        {
            ArrayInitializer arrayInitializer = (ArrayInitializer) expression;
            List<AnnotationValue> arrayValues = new ArrayList<>(arrayInitializer.expressions().size());
            for (Object arrayExpression : arrayInitializer.expressions())
            {
                arrayValues.add(getAnnotationValueForExpression((Expression) arrayExpression));
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
            AnnotationValue castExpressionValue = getAnnotationValueForExpression(cast.getExpression());
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
        }
        else
        {
            LOG.warning("Unexpected type: " + expression.getClass().getCanonicalName() + " in file: " + this.javaFile
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
    private void addAnnotationValues(AnnotationClassReference typeRef, Annotation node)
    {
        Map<String, AnnotationValue> annotationValueMap = new HashMap<>();
        if (node instanceof SingleMemberAnnotation)
        {
            SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) node;
            AnnotationValue value = getAnnotationValueForExpression(singleMemberAnnotation.getValue());
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
                AnnotationValue value = getAnnotationValueForExpression(expression);
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

    private AnnotationClassReference processAnnotation(Annotation node)
    {
        ITypeBinding typeBinding = node.resolveTypeBinding();
        AnnotationClassReference reference;
        String qualifiedName;
        if (typeBinding != null)
            qualifiedName = typeBinding.getQualifiedName();
        else
            qualifiedName = resolveClassname(node.getTypeName().toString());

        reference = new AnnotationClassReference(
                    qualifiedName,
                    cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(node.getStartPosition()),
                    node.getLength(),
                    node.toString());

        addAnnotationValues(reference, node);

        return reference;
    }

    @Override
    public boolean visit(NormalAnnotation node)
    {
        AnnotationClassReference reference = processAnnotation(node);
        this.classReferences.addReference(reference);

        // false to avoid recursively processing nested annotations (our code already handles that)
        return false;
    }

    @Override
    public boolean visit(SingleMemberAnnotation node)
    {
        AnnotationClassReference reference = processAnnotation(node);
        this.classReferences.addReference(reference);
        return false;
    }

    @Override
    public boolean visit(MarkerAnnotation node)
    {
        AnnotationClassReference reference = processAnnotation(node);
        this.classReferences.addReference(reference);
        return false;
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
                                        cu.getColumnNumber(node.getStartPosition()), node.getLength(), extractDefinitionLine(node.toString()));
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
        if (clzSuperClasses != null)
        {
            if (clzSuperClasses instanceof SimpleType)
            {
                ITypeBinding resolvedSuperClass = ((SimpleType) clzSuperClasses).resolveBinding();
                // register all the superClasses up to Object
                while (resolvedSuperClass != null && !resolvedSuperClass.getQualifiedName().equals("java.lang.Object"))
                {
                    processTypeBinding(resolvedSuperClass, TypeReferenceLocation.INHERITANCE, cu.getLineNumber(node.getStartPosition()),
                                cu.getColumnNumber(node.getStartPosition()), node.getLength(), extractDefinitionLine(node.toString()));
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
            nodeType = resolveClassname(nodeType);
            VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(i);
            this.names.add(frag.getName().getIdentifier());
            this.nameInstance.put(frag.getName().toString(), nodeType.toString());
        }
        processType(node.getType(), TypeReferenceLocation.VARIABLE_DECLARATION,
                    cu.getLineNumber(node.getStartPosition()),
                    cu.getColumnNumber(node.getStartPosition()), node.getLength(), node.toString());
        return super.visit(node);
    }

    @Override
    public boolean visit(ImportDeclaration node)
    {
        String name = node.getName().toString();
        if (node.isOnDemand())
        {
            wildcardImports.add(name);

            String[] resolvedNames = this.wildcardImportResolver.resolve(name);
            for (String resolvedName : resolvedNames)
            {
                processImport(resolvedName, cu.getLineNumber(node.getName().getStartPosition()),
                            cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(), node.toString());
            }
        }
        else
        {
            String clzName = StringUtils.substringAfterLast(name, ".");
            classNameLookedUp.add(clzName);
            classNameToFQCN.put(clzName, name);
            processImport(name, cu.getLineNumber(node.getName().getStartPosition()),
                        cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(), node.toString());
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
            processMethod(methodCall, TypeReferenceLocation.METHOD_CALL, cu.getLineNumber(node.getName().getStartPosition()),
                        cu.getColumnNumber(node.getName().getStartPosition()), node.getName().getLength(), node.toString());
        }

        return super.visit(node);
    }

    @Override
    public boolean visit(PackageDeclaration node)
    {
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
                    cu.getColumnNumber(node.getType().getStartPosition()), node.getType().getLength(), node.toString());

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
                classNameLookedUp.add(sourceClassname);
                String resolvedClassName = this.wildcardImportResolver.resolve(this.wildcardImports, sourceClassname);
                if (resolvedClassName != null)
                {
                    classNameToFQCN.put(sourceClassname, resolvedClassName);
                    return resolvedClassName;
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
