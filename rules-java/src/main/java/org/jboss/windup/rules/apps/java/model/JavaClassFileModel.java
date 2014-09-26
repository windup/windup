package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This Model represents Java class files on disk (eg, /path/to/Foo.class). This does not represent Java source files
 * (.java files). The class itself is represented by the {@link JavaClassModel} frame.
 */
@TypeValue(JavaClassFileModel.TYPE)
public interface JavaClassFileModel extends FileModel
{
    public static final String UNPARSEABLE_CLASS_CLASSIFICATION = "Unparseable Class File";
    public static final String UNPARSEABLE_CLASS_DESCRIPTION = "This Class file could not be parsed";

    public static final String MINOR_VERSION = "minorVersion";
    public static final String MAJOR_VERSION = "majorVersion";
    public static final String TYPE = "JavaClassFileModel";
    public static final String PROPERTY_PACKAGE_NAME = "packageName";

    /**
     * Contains the package name represented by this class file.
     */
    @Property(PROPERTY_PACKAGE_NAME)
    public String getPackageName();

    /**
     * Contains the package name represented by this class file.
     */
    @Property(PROPERTY_PACKAGE_NAME)
    public void setPackageName(String packageName);

    /**
     * Contains the {@link JavaClassModel} represented by this .class file.
     */
    @Adjacency(label = JavaSourceFileModel.JAVA_CLASS_MODEL, direction = Direction.OUT)
    public void setJavaClass(JavaClassModel model);

    /**
     * Contains the {@link JavaClassModel} represented by this .class file.
     */
    @Adjacency(label = JavaSourceFileModel.JAVA_CLASS_MODEL, direction = Direction.OUT)
    public JavaClassModel getJavaClass();

    /**
     * Contains the Major version of this class file
     */
    @Property(MAJOR_VERSION)
    public int getMajorVersion();

    /**
     * Contains the Major version of this class file
     */
    @Property(MAJOR_VERSION)
    public void setMajorVersion(int majorVersion);

    /**
     * Contains the Minor version of this class file
     */
    @Property(MINOR_VERSION)
    public int getMinorVersion();

    /**
     * Contains the Minor version of this class file
     */
    @Property(MINOR_VERSION)
    public void setMinorVersion(int minorVersion);
}