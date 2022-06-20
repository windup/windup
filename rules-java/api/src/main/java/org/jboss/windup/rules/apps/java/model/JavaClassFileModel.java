package org.jboss.windup.rules.apps.java.model;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

/**
 * This Model represents Java class files on disk (eg, /path/to/Foo.class). This does not represent Java source files (.java files). The class itself
 * is represented by the {@link JavaClassModel} frame.
 */
@TypeValue(JavaClassFileModel.TYPE)
public interface JavaClassFileModel extends FileModel {
    String MINOR_VERSION = "minorVersion";
    String MAJOR_VERSION = "majorVersion";
    String TYPE = "JavaClassFileModel";
    String PROPERTY_PACKAGE_NAME = "packageName";
    String SKIP_DECOMPILATION = "skipDecompilation";

    String DECOMPILED_FILE = "decompiledFile";

    /**
     * Indicates that we should not decompile this. This can allow us to skip the decompilation of class files that are not determined to be relevant
     * for source scanning.
     */
    @Property(SKIP_DECOMPILATION)
    Boolean getSkipDecompilation();

    /**
     * Indicates that we should not decompile this. This can allow us to skip the decompilation of class files that are not determined to be relevant
     * for source scanning.
     */
    @Property(SKIP_DECOMPILATION)
    void setSkipDecompilation(boolean skipDecompilation);

    /**
     * Contains the package name represented by this class file.
     */
    @Property(PROPERTY_PACKAGE_NAME)
    String getPackageName();

    /**
     * Contains the package name represented by this class file.
     */
    @Indexed
    @Property(PROPERTY_PACKAGE_NAME)
    void setPackageName(String packageName);

    /**
     * Contains the {@link JavaClassModel} represented by this .class file.
     */
    @Adjacency(label = JavaSourceFileModel.JAVA_CLASS_MODEL, direction = Direction.OUT)
    void setJavaClass(JavaClassModel model);

    /**
     * Contains the {@link JavaClassModel} represented by this .class file.
     */
    @Adjacency(label = JavaSourceFileModel.JAVA_CLASS_MODEL, direction = Direction.OUT)
    JavaClassModel getJavaClass();

    /**
     * Contains the Major version of this class file
     */
    @Property(MAJOR_VERSION)
    int getMajorVersion();

    /**
     * Contains the Major version of this class file
     */
    @Property(MAJOR_VERSION)
    void setMajorVersion(int majorVersion);

    /**
     * Contains the Minor version of this class file
     */
    @Property(MINOR_VERSION)
    int getMinorVersion();

    /**
     * Contains the Minor version of this class file
     */
    @Property(MINOR_VERSION)
    void setMinorVersion(int minorVersion);

    /**
     * Returns the path of this file within the parent project (format suitable for reporting)
     * Uses fully qualified class name notation for classes
     */
    default String getPrettyPathWithinProject(boolean useFQNForClasses) {
        if (!useFQNForClasses) {
            return this.getPrettyPathWithinProject();
        }

        JavaClassModel javaClass = getJavaClass();

        String result;

        if (javaClass == null) {
            result = getPrettyPathWithinProject();
        } else {
            result = javaClass.getQualifiedName();
        }

        return result;
    }

}
