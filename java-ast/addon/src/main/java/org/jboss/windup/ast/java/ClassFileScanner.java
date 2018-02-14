package org.jboss.windup.ast.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipError;

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
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

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

    private Map<String, ClassReader> classReaderCache = new HashMap<>();
    private Map<String, ClassInfo> classInfoByPath = new HashMap<>();
    private Map<String, List<ClassInfo>> classInfoByFQDN = new HashMap<>();

    public ClassFileScanner()
    {

    }

    public ClassFileScanner(Set<String> classpath)
    {
        this.classpath = classpath;
        populateCache();
    }

    public ClassInfo getClassInfo(String path)
    {
        String normalizedPath = Paths.get(path).normalize().toAbsolutePath().toString();
        return classInfoByPath.get(normalizedPath);
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
                ClassReference[] references = parseMethod(classname, name, desc, signature, true);
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
                        ClassReference[] references = parseMethod(owner, name, desc, null, false);
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

    private ClassReference[] parseMethod(String owner, String name, String description, String asmSignature, boolean methodDeclaration)
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

        if (asmSignature != null)
        {
            SignatureReader signatureReader = new SignatureReader(asmSignature);
            signatureReader.accept(new SignatureVisitor(Opcodes.ASM6)
            {
                private boolean returnTypeMode = false;
                private boolean exceptionMode = false;

                @Override
                public SignatureVisitor visitReturnType()
                {
                    returnTypeMode = true;
                    exceptionMode = false;
                    return super.visitReturnType();
                }

                @Override
                public SignatureVisitor visitExceptionType()
                {
                    returnTypeMode = false;
                    exceptionMode = true;
                    return super.visitExceptionType();
                }

                private void addType(String type)
                {
                    String parsedType = parseType(type);
                    TypeReferenceLocation location = null;
                    if (returnTypeMode)
                        location = TypeReferenceLocation.RETURN_TYPE;
                    else if (!exceptionMode)
                        location = TypeReferenceLocation.METHOD_PARAMETER;

                    if (location != null)
                        results.addAll(createClassReference(parsedType, location));
                }

                @Override
                public void visitBaseType(char descriptor)
                {
                    addType("" + descriptor);
                    super.visitBaseType(descriptor);
                }

                @Override
                public void visitTypeVariable(String name)
                {
                    addType(name);
                    super.visitTypeVariable(name);
                }

                @Override
                public void visitClassType(String name)
                {
                    addType(name);
                    super.visitClassType(name);
                }

                @Override
                public void visitInnerClassType(String name)
                {
                    addType(name);
                    super.visitInnerClassType(name);
                }
            });
        }
        else
        {
            for (String parameter : parameters)
            {
                if (!StringUtils.isBlank(parameter))
                    results.addAll(createClassReference(parameter, TypeReferenceLocation.METHOD_PARAMETER));
            }
            if (returnType.equals("V"))
            {
                returnType = "void";
            }
            results.addAll(createClassReference(parseType(returnType), TypeReferenceLocation.RETURN_TYPE));
        }

        TypeReferenceLocation location = methodDeclaration ? TypeReferenceLocation.METHOD : TypeReferenceLocation.METHOD_CALL;
        String qualifiedName = signature.toString();

        results.addAll(createClassReference(owner, qualifiedName, name, location));

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
        classFile = classFile.normalize().toAbsolutePath();
        ClassReader result = classReaderCache.get(classFile.toString());
        if (result != null)
            return result;

        try (FileInputStream fis = new FileInputStream(classFile.toFile()))
        {
            result = new ClassReader(fis);
            classReaderCache.put(classFile.toString(), result);
            return result;
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
        List<ClassInfo> classInfoList = this.classInfoByFQDN.get(owner);
        if (classInfoList != null)
        {
            classInfoList.forEach(classInfo -> {
                owners.add(classInfo.superclass);
                owners.addAll(classInfo.implementedInterfaces);
            });
        }
        return owners;
    }

    private void populateCache()
    {
        for (String pathString : this.classpath)
        {
            Path parentPath = Paths.get(pathString);
            SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path childFile, BasicFileAttributes attrs) throws IOException
                {
                    if (!StringUtils.endsWithIgnoreCase(childFile.getFileName().toString(), ".class"))
                        return FileVisitResult.CONTINUE;

                    String normalizedPath = childFile.normalize().toAbsolutePath().toString();
                    try
                    {
                        ClassReader classReader = createClassReader(childFile);
                        classReaderCache.put(normalizedPath, classReader);
                        String classname = fixClassname(classReader.getClassName());
                        String superclass = fixClassname(classReader.getSuperName());
                        String[] interfaces = Arrays
                                    .stream(classReader.getInterfaces())
                                    .map(interfaceName -> fixClassname(interfaceName))
                                    .collect(Collectors.toList()).toArray(new String[0]);

                        List<ClassInfo> classInfoList = classInfoByFQDN.computeIfAbsent(classname, (key) -> new ArrayList<>());

                        ClassInfo classInfo = new ClassInfo(classname, superclass, interfaces);
                        classReader.accept(new ClassVisitor(Opcodes.ASM6)
                        {
                            @Override
                            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
                            {
                                classInfo.setMajorVersion(version);
                            }
                        }, 0);
                        boolean isInterface = (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
                        classInfo.setIsInterface(isInterface);
                        boolean isPublic = (classReader.getAccess() & Opcodes.ACC_PUBLIC) != 0;
                        classInfo.setPublic(isPublic);

                        classInfoList.add(classInfo);
                        classInfoByFQDN.put(classname, classInfoList);
                        classInfoByPath.put(normalizedPath, classInfo);
                    }
                    catch (Throwable t)
                    {
                        LOG.warning("Could not parse class data for: " + childFile + " due to: " + t.getMessage());
                        t.printStackTrace();
                        ClassInfo errorInfo = new ClassInfo(null, null, null);
                        errorInfo.setError("Could not parse class data for: " + childFile + " due to: " + t.getMessage());
                        classInfoByPath.put(normalizedPath, errorInfo);
                    }

                    return FileVisitResult.CONTINUE;
                }
            };

            try
            {
                if (Files.isDirectory(parentPath))
                {
                    // handle directory
                    Files.walkFileTree(parentPath, visitor);

                }
                else if (ZipUtil.endsWithZipExtension(pathString))
                {
                    try (FileSystem zipFilesystem = FileSystems.newFileSystem(parentPath, null))
                    {
                        Files.walkFileTree(zipFilesystem.getPath("/"), visitor);
                    }
                }
            }
            catch (Exception | ZipError e)
            {
                LOG.log(Level.WARNING, "Failed to crawl subtree: " + parentPath + " due to: " + e.getMessage(), e);
            }
        }
    }

    public static class ClassInfo
    {
        String fqdn;

        /**
         * Does it seem strange for this to be a list? That is because it is possible for the same class to appear in the same classpath multiple
         * times with different superclasses.
         *
         * We just include all of the scenarios, even though this isn't technically accurate. It is more important to not miss a potential scenario,
         * than it is to only include the perfectly accurate ones.
         */
        private boolean isPublic;
        private boolean isInterface;
        private String superclass;
        private Set<String> implementedInterfaces = new HashSet<>();
        private int majorVersion;
        private String error;

        public ClassInfo(String fqdn, String superclass, String[] implementedInterfaces)
        {
            this.fqdn = fqdn;
            this.superclass = superclass;
            if (implementedInterfaces != null)
                this.implementedInterfaces.addAll(Arrays.asList(implementedInterfaces));
        }

        public String getPackageName()
        {
            if (StringUtils.isBlank(fqdn) || !fqdn.contains("."))
                return "";
            else
                return fqdn.substring(0, fqdn.lastIndexOf("."));
        }

        public String getFqdn()
        {
            return fqdn;
        }

        public String getSuperclass()
        {
            return superclass;
        }

        public Set<String> getImplementedInterfaces()
        {
            return implementedInterfaces;
        }

        public boolean isPublic()
        {
            return isPublic;
        }

        private void setPublic(boolean aPublic)
        {
            isPublic = aPublic;
        }

        public boolean isInterface()
        {
            return isInterface;
        }

        private void setIsInterface(boolean isInterface)
        {
            this.isInterface = isInterface;
        }

        public int getMajorVersion()
        {
            return majorVersion;
        }

        private void setMajorVersion(int majorVersion)
        {
            this.majorVersion = majorVersion;
        }

        public String getError()
        {
            return error;
        }

        private void setError(String error)
        {
            this.error = error;
        }
    }
}
