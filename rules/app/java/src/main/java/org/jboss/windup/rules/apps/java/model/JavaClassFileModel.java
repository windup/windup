package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This is good for searches for Java class files
 */
@TypeValue(JavaClassFileModel.TYPE)
public interface JavaClassFileModel extends FileModel
{
    public static final String TYPE = "JavaClassFileModel";

    public static final String UNPARSEABLE_CLASS_CLASSIFICATION = "Unparseable Class File";
    public static final String UNPARSEABLE_CLASS_DESCRIPTION = "This Class file could not be parsed";

    public static final String PROPERTY_PACKAGE_NAME = "packageName";

    /**
     * Contains the class' package name
     */
    @Property(PROPERTY_PACKAGE_NAME)
    public String getPackageName();

    /**
     * Contains the class' package name
     */
    @Property(PROPERTY_PACKAGE_NAME)
    public void setPackageName(String packageName);

    /**
     * Contains the {@link JavaClassModel} associated with this .class file
     */
    @Adjacency(label = JavaSourceFileModel.PROPERTY_JAVA_CLASS_MODEL, direction = Direction.OUT)
    public void setJavaClass(JavaClassModel model);

    /**
     * Contains the {@link JavaClassModel} associated with this .class file
     */
    @Adjacency(label = JavaSourceFileModel.PROPERTY_JAVA_CLASS_MODEL, direction = Direction.OUT)
    public JavaClassModel getJavaClass();
}