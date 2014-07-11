package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This is good for searches for Java files, which will be frequent.
 */
@TypeValue("JavaFile")
public interface JavaFileModel extends FileModel
{
    public static final String PROPERTY_PACKAGE_NAME = "packageName";

    @Property(PROPERTY_PACKAGE_NAME)
    public String getPackageName();

    @Property(PROPERTY_PACKAGE_NAME)
    public void setPackageName(String packageName);

    @Adjacency(label = "javaClass", direction = Direction.OUT)
    public Iterable<JavaClassModel> getJavaClasses();

    @Adjacency(label = "javaClass", direction = Direction.OUT)
    public void addJavaClass(JavaClassModel javaClassModel);

}