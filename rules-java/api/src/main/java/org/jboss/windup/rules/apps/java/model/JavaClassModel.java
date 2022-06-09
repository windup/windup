package org.jboss.windup.rules.apps.java.model;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.*;
import org.jboss.windup.graph.model.resource.FileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents a JavaClass, either from a .class file or a .java source file.
 *
 */
@TypeValue(JavaClassModel.TYPE)
public interface JavaClassModel extends WindupVertexFrame, BelongsToProject, HasApplications, HasProject
{
    String TYPE = "JavaClassModel";

    String JAVA_METHOD = "javaMethod";
    String CLASS_FILE = "classFile";
    String ORIGINAL_SOURCE = "originalSource";
    String DECOMPILED_SOURCE = "decompiledSource";
    String INTERFACES = "interfaces";
    String EXTENDS = "extends";
    String QUALIFIED_NAME = "qualifiedName";
    String CLASS_NAME = "className";
    String PACKAGE_NAME = "packageName";
    String IS_PUBLIC = "isPublic";
    String IS_INTERFACE = "isInterface";
    String MAJOR_VERSION = "majorVersion";
    String MINOR_VERSION = "minorVersion";

    /**
     * Indicates whether the class is declared "public".
     */
    @Property(IS_PUBLIC)
    Boolean isPublic();

    /**
     * Indicates whether the class is declared "public".
     */
    @Property(IS_PUBLIC)
    void setPublic(boolean isPublic);

    /**
     * Indicates whether this represents a Java Interface
     */
    @Property(IS_INTERFACE)
    Boolean isInterface();

    /**
     * Indicates whether this represents a Java Interface
     */
    @Property(IS_INTERFACE)
    void setInterface(boolean isInterface);

    /**
     * Contains the simple name of the class (no package)
     */
    @Property(CLASS_NAME)
    void setSimpleName(String className);

    /**
     * Contains the simple name of the class (no package)
     */
    @Property(CLASS_NAME)
    String getClassName();

    /**
     * Contains the fully qualified name of the class
     */
    @Indexed
    @Property(QUALIFIED_NAME)
    String getQualifiedName();

    /**
     * Contains the fully qualified name of the class
     */
    @Property(QUALIFIED_NAME)
    void setQualifiedName(String qualifiedName);

    /**
     * Contains the class' package name
     */
    @Indexed
    @Property(PACKAGE_NAME)
    String getPackageName();

    /**
     * Contains the class' package name
     */
    @Property(PACKAGE_NAME)
    void setPackageName(String packageName);

    /**
     * Lists classes extended by this class
     */
    @Adjacency(label = EXTENDS, direction = Direction.OUT)
    JavaClassModel getExtends();

    /**
     * Lists classes extended by this class
     */
    @Adjacency(label = EXTENDS, direction = Direction.OUT)
    void setExtends(final JavaClassModel javaFacet);

    /**
     * Lists interfaces implemented by this class, or extended if this is an interface.
     */
    @Adjacency(label = INTERFACES, direction = Direction.OUT)
    void addInterface(final JavaClassModel javaFacet);

    /**
     * Lists interfaces implemented by this class, or extended if this is an interface.
     */
    @Adjacency(label = INTERFACES, direction = Direction.OUT)
    List<JavaClassModel> getInterfaces();

    /**
     * Lists classes which implement this class
     */
    @Adjacency(label = INTERFACES, direction = Direction.IN)
    List<JavaClassModel> getImplementedBy();

    /**
     * Contains the {@link JavaSourceFileModel} of the decompiled version of this file (assuming that it originally was
     * decompiled from a .class file)
     */
    @Adjacency(label = DECOMPILED_SOURCE, direction = Direction.OUT)
    void setDecompiledSource(JavaSourceFileModel source);

    /**
     * Contains the {@link JavaSourceFileModel} of the decompiled version of this file (assuming that it originally was
     * decompiled from a .class file)
     */
    @Adjacency(label = DECOMPILED_SOURCE, direction = Direction.OUT)
    JavaSourceFileModel getDecompiledSource();

    /**
     * Contains the original source code of this file, assuming that it was originally provided in source form (and not
     * via a decompilation).
     */
    @Adjacency(label = ORIGINAL_SOURCE, direction = Direction.OUT)
    void setOriginalSource(AbstractJavaSourceModel source);

    /**
     * Contains the original source code of this file, assuming that it was originally provided in source form (and not
     * via a decompilation).
     */
    @Adjacency(label = ORIGINAL_SOURCE, direction = Direction.OUT)
    AbstractJavaSourceModel getOriginalSource();

    /**
     * Contains the original .class file, assuming that it was originally provided in binary form (as a java .class
     * file)
     */
    @Adjacency(label = CLASS_FILE, direction = Direction.OUT)
    FileModel getClassFile();

    /**
     * Contains the original .class file, assuming that it was originally provided in binary form (as a java .class
     * file)
     */
    @Adjacency(label = CLASS_FILE, direction = Direction.OUT)
    FileModel setClassFile(FileModel file);

    /**
     * Gets the {@link JavaMethodModel} by name
     */
    default List<JavaMethodModel> getMethod(String methodName)
    {
        List<Vertex> vertices = new GraphTraversalSource(getWrappedGraph().getBaseGraph()).V(getElement())
                .out(JAVA_METHOD)
                .has(JavaMethodModel.METHOD_NAME, methodName)
                .toList();
        return vertices.stream().map(v -> getGraph().frameElement(v, JavaMethodModel.class))
                .collect(Collectors.toList());
    }

    /**
     * Adds a {@link JavaMethodModel} to this {@link JavaClassModel}
     */
    @Adjacency(label = JAVA_METHOD, direction = Direction.OUT)
    void addJavaMethod(final JavaMethodModel javaMethod);

    /**
     * Adds a {@link JavaMethodModel} to this {@link JavaClassModel}
     */
    @Adjacency(label = JAVA_METHOD, direction = Direction.OUT)
    List<JavaMethodModel> getJavaMethods();

    @Override
    default List<ProjectModel> getApplications()
    {
        FileModel sourceModel = this.getSourceFile();

        if (sourceModel == null)
        {
            return Collections.emptyList();
        }

        return sourceModel.getApplications();
    }

    @Override
    default ProjectModel getProjectModel()
    {
        FileModel sourceModel = this.getSourceFile();

        if (sourceModel == null)
        {
            return null;
        }

        return sourceModel.getProjectModel();
    }

    default FileModel getSourceFile()
    {
        FileModel classFile = this.getClassFile();

        if (classFile == null)
        {
            // .jsp class will have originalSource instead of classFile
            AbstractJavaSourceModel originalSource = this.getOriginalSource();

            if (originalSource == null)
            {
                String name = this.getClassName();

                Logger.getLogger(JavaClassModel.class.getName()).log(
                        Level.WARNING,
                        "ClassFile and originalSource null for class: {0}",
                        name
                );

                return null;
            }

            return originalSource;
        }

        return classFile;
    }

    @Override
    default boolean belongsToProject(ProjectModel projectModel)
    {
        FileModel sourceModel = this.getSourceFile();

        if (sourceModel == null)
        {
            return false;
        }

        return sourceModel.belongsToProject(projectModel);
    }
}
