package org.jboss.windup.ast.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

/**
 * Supports scanning a .class file and extracting information about which methods and variables are declared within it.
 *
 * A classpath can be provided so that it can properly resolve superclass and interface information for methods as well.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ClassFileScanner
{
    private static Logger LOG = Logger.getLogger(ClassFileScanner.class.getName());

    private Set<String> classpath;
    private Map<String, ClassInfo> classInfoCache = new HashMap<>();

    public ClassFileScanner()
    {

    }

    public ClassFileScanner(Set<String> classpath)
    {
        this.classpath = classpath;
        populateCache();
    }

    public Collection<ClassReference> scanClass(Path classFile)
    {
        List<ClassReference> results = new ArrayList<>();

        ClassReader classReader = createClassReader(classFile);
        classReader.accept(new ClassVisitor(Opcodes.ASM6)
        {
            private String classname;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
            {
                super.visit(version, access, name, signature, superName, interfaces);
                Collection<ClassReference> superClass = createClassReference(parseType(superName), TypeReferenceLocation.INHERITANCE);
                results.addAll(superClass);

                for (String iface : interfaces)
                {
                    Collection<ClassReference> ifaceReference = createClassReference(parseType(iface), TypeReferenceLocation.IMPLEMENTS_TYPE);
                    results.addAll(ifaceReference);
                }

                this.classname = name;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible)
            {
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
            {
                return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access)
            {
                super.visitInnerClass(name, outerName, innerName, access);
            }

            @Override
            public void visitOuterClass(String owner, String name, String desc)
            {
                super.visitOuterClass(owner, name, desc);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
            {
                if (desc != null)
                {
                    String type = parseType(desc);
                    Collection<ClassReference> reference = createClassReference(type, TypeReferenceLocation.FIELD_DECLARATION);
                    results.addAll(reference);
                }

                return super.visitField(access, name, desc, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
            {
                ClassReference[] references = parseMethod(classname, name, desc, true);
                results.addAll(Arrays.asList(references));
                return new MethodVisitor(Opcodes.ASM6)
                {
                    @Override
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
                    {
                        Collection<ClassReference> reference = createClassReference(parseType(desc), TypeReferenceLocation.VARIABLE_DECLARATION);
                        results.addAll(reference);
                        super.visitLocalVariable(name, desc, signature, start, end, index);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf)
                    {
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                        ClassReference[] references = parseMethod(owner, name, desc, false);
                        results.addAll(Arrays.asList(references));
                    }
                };
            }
        }, 0);
        return results;
    }

    private String parseType(String bytecodeType)
    {
        String result = fixClassname(bytecodeType);
        if (result.endsWith(";"))
            result = result.substring(0, result.length() - 1);

        StringBuilder arraySuffix = new StringBuilder();
        for (int i = 0; i < result.length(); i++)
        {
            if (result.charAt(i) != '[')
                break;

            arraySuffix.append("[]");
        }
        result = result.substring(arraySuffix.length() / 2);

        if (result.startsWith("L"))
            result = result.substring(1);
        else if (result.startsWith("Z"))
            result = "boolean";
        else if (result.startsWith("C"))
            result = "char";
        else if (result.startsWith("B"))
            result = "byte";
        else if (result.startsWith("S"))
            result = "short";
        else if (result.startsWith("I"))
            result = "int";
        else if (result.startsWith("F"))
            result = "float";
        else if (result.startsWith("J"))
            result = "long";
        else if (result.startsWith("D"))
            result = "double";
        result += arraySuffix;

        return result;
    }

    private String fixClassname(String asmClassname)
    {
        return asmClassname.replace("/", ".").replace("\\", ".");
    }

    private ClassReference[] parseMethod(String owner, String name, String description, boolean methodDeclaration)
    {
        owner = fixClassname(owner);

        description = StringUtils.removeStart(description, "(");
        String returnType = StringUtils.substringAfterLast(description, ")");
        description = StringUtils.substringBeforeLast(description, ")");

        List<String> parameters = new ArrayList<>();
        for (String token : description.split(";"))
        {
            String type = parseType(token);

            parameters.add(type);
        }
        StringBuilder signature = new StringBuilder(owner + "." + name + "(");
        for (String param : parameters)
        {
            if (!signature.toString().endsWith("("))
                signature.append(", ");
            signature.append(param);
        }
        signature.append(")");

        List<ClassReference> results = new ArrayList<>();
        for (String parameter : parameters)
        {
            if (!StringUtils.isBlank(parameter))
                results.addAll(createClassReference(parameter, TypeReferenceLocation.METHOD_PARAMETER));
        }

        TypeReferenceLocation location = methodDeclaration ? TypeReferenceLocation.METHOD : TypeReferenceLocation.METHOD_CALL;
        String qualifiedName = signature.toString();

        results.addAll(createClassReference(owner, qualifiedName, name, location));

        if (returnType.equals("V"))
        {
            returnType = "void";
        }
        results.addAll(createClassReference(parseType(returnType), TypeReferenceLocation.RETURN_TYPE));

        return results.toArray(new ClassReference[results.size()]);
    }

    private Collection<ClassReference> createClassReference(String qualifiedName, TypeReferenceLocation location)
    {
        return createClassReference(null, qualifiedName, null, location);
    }

    private Collection<ClassReference> createClassReference(String owner, String qualifiedName, String methodName, TypeReferenceLocation location)
    {
        /*
         * If we have an owner, use that, otherwise just use the qualified name. This is applicable in cases where we are looking at a method call and
         * the qualifiedName would have method information at the end that would not be useful for getting the package and class name parsed.
         */
        owner = owner != null ? owner : qualifiedName;
        Set<String> potentialOwners = calculatePotentialOwners(owner);

        // Return one reference for each type (final type, plus all implemented interfaces and superclasses)
        Set<ClassReference> result = new HashSet<>();
        for (String potentialOwner : potentialOwners)
        {
            // Try to parse a package name
            PackageAndClassName packageAndClassName = PackageAndClassName.parseFromQualifiedName(potentialOwner);
            String newQualifiedName = qualifiedName;
            if (newQualifiedName.startsWith(owner))
            {
                newQualifiedName = potentialOwner + newQualifiedName.substring(owner.length());
            }

            // Return a type with fake location data (since we don't always get accurate location data from classes, so we
            // intentionally do not rely on it being accurate).
            result.add(new ClassReference(newQualifiedName, packageAndClassName.getPackageName(), packageAndClassName.getClassName(),
                    methodName, ResolutionStatus.RESOLVED, location, 1, 1, 1, qualifiedName));
        }
        return result;
    }

    private ClassReader createClassReader(Path classFile)
    {
        try (FileInputStream fis = new FileInputStream(classFile.toFile()))
        {
            return new ClassReader(fis);
        }
        catch (Exception e)
        {
            throw new WindupException("Failed to load class: " + classFile + " due to: " + e.getMessage(), e);
        }
    }

    private Set<String> calculatePotentialOwners(String owner)
    {
        Set<String> owners = new HashSet<>();
        owners.add(owner);
        ClassInfo classInfo = this.classInfoCache.get(owner);
        if (classInfo != null)
        {
            owners.addAll(classInfo.superclass);
            owners.addAll(classInfo.implementedInterfaces);
        }
        return owners;
    }

    private void populateCache()
    {
        for (String pathString : this.classpath)
        {
            try
            {
                Path path = Paths.get(pathString);
                if (Files.isDirectory(path) || ZipUtil.endsWithZipExtension(pathString))
                {
                    // handle directory
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>()
                    {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                        {
                            if (!StringUtils.endsWithIgnoreCase(file.getFileName().toString(), ".class"))
                                return FileVisitResult.CONTINUE;

                            try
                            {
                                ClassReader classReader = createClassReader(file);
                                String classname = fixClassname(classReader.getClassName());
                                String superclass = fixClassname(classReader.getSuperName());
                                String[] interfaces = Arrays
                                            .stream(classReader.getInterfaces())
                                            .map(interfaceName -> fixClassname(interfaceName))
                                            .collect(Collectors.toList()).toArray(new String[0]);

                                ClassInfo classInfo = classInfoCache.get(classname);
                                if (classInfo != null)
                                {
                                    classInfo.addSuperclass(superclass);
                                    classInfo.addInterfaces(interfaces);
                                } else
                                {
                                    classInfo = new ClassInfo(classname, superclass, interfaces);
                                    classInfoCache.put(classname, classInfo);
                                }
                            }
                            catch (Throwable t)
                            {
                                LOG.warning("Could not parse class data for: " + file + " due to: " + t.getMessage());
                                t.printStackTrace();
                            }

                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
            catch (Throwable t)
            {
                LOG.warning("Could not parse class data for path: " + pathString + " due to: " + t.getMessage());
            }
        }
    }

    private static class ClassInfo
    {
        String fqdn;

        /**
         * Does it seem strange for this to be a list? That is because it is possible for the same class to appear in the same classpath multiple
         * times with different superclasses.
         *
         * We just include all of the scenarios, even though this isn't technically accurate. It is more important to not miss a potential scenario,
         * than it is to only include the perfectly accurate ones.
         */
        Set<String> superclass = new HashSet<>();
        Set<String> implementedInterfaces = new HashSet<>();

        public ClassInfo(String fqdn, String superclass, String[] implementedInterfaces)
        {
            this.fqdn = fqdn;
            this.superclass.add(superclass);
            this.implementedInterfaces.addAll(Arrays.asList(implementedInterfaces));
        }

        public void addSuperclass(String superclass)
        {
            this.superclass.add(superclass);
        }

        public void addInterfaces(String[] interfaces)
        {
            this.implementedInterfaces.addAll(Arrays.asList(interfaces));
        }

        public String getFqdn()
        {
            return fqdn;
        }

        public Set<String> getSuperclass()
        {
            return superclass;
        }

        public Set<String> getImplementedInterfaces()
        {
            return implementedInterfaces;
        }
    }
}
