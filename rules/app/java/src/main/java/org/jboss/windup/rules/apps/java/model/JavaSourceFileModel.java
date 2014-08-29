package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaSourceFileModel")
public interface JavaSourceFileModel extends FileModel
{
    public static final String PROPERTY_PACKAGE_NAME = "packageName";
    public static final String PROPERTY_JAVA_CLASS_MODEL = "javaClass";

    @Property(PROPERTY_PACKAGE_NAME)
    public String getPackageName();

    @Property(PROPERTY_PACKAGE_NAME)
    public void setPackageName(String packageName);

    @Adjacency(label = PROPERTY_JAVA_CLASS_MODEL, direction = Direction.OUT)
    public Iterable<JavaClassModel> getJavaClasses();

    @Adjacency(label = PROPERTY_JAVA_CLASS_MODEL, direction = Direction.OUT)
    public void addJavaClass(JavaClassModel javaClassModel);
}
