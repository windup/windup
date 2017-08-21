package org.jboss.windup.rules.apps.java.model;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.BelongsToProject;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a JavaClass, either from a .class file or a .java source file.
 *
 */
@TypeValue(JavaClassModel.TYPE)
public interface JavaClassModel extends WindupVertexFrame, BelongsToProject
{
    String TYPE = "JavaClass";

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
    Iterable<JavaClassModel> getInterfaces();

    /**
     * Lists classes which implement this class
     */
    @Adjacency(label = INTERFACES, direction = Direction.IN)
    Iterable<JavaClassModel> getImplementedBy();

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
    @GremlinGroovy("it.out('" + JAVA_METHOD + "').has('" + JavaMethodModel.METHOD_NAME + "', methodName)")
    Iterable<JavaMethodModel> getMethod(@GremlinParam("methodName") String methodName);

    /**
     * Adds a {@link JavaMethodModel} to this {@link JavaClassModel}
     */
    @Adjacency(label = JAVA_METHOD, direction = Direction.OUT)
    void addJavaMethod(final JavaMethodModel javaMethod);

    /**
     * Adds a {@link JavaMethodModel} to this {@link JavaClassModel}
     */
    @Adjacency(label = JAVA_METHOD, direction = Direction.OUT)
    Iterable<JavaMethodModel> getJavaMethods();

    @JavaHandler
    @Override
    Iterable<ProjectModel> getRootProjectModels();

    abstract class Impl implements JavaHandlerContext<Vertex>, JavaClassModel, BelongsToProject
    {

        @Override
        public Iterable<ProjectModel> getRootProjectModels()
        {
            FileModel sourceModel = this.getSourceFile();

            if (sourceModel == null)
            {
                return Collections.emptyList();
            }
            
            return sourceModel.getRootProjectModels();
        }

        protected FileModel getSourceFile()
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
    }
}
