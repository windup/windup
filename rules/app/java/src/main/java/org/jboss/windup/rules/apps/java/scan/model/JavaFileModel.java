package org.jboss.windup.rules.apps.java.scan.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.renderer.api.Label;


/**
 *  This is good for searches for Java files, which will be frequent.
 */
@TypeValue("JavaFile")
public interface JavaFileModel extends FileModel
{
    public static final String PROPERTY_QUALIFIED_NAME = "qualifiedName";
    public static final String PROPERTY_CLASS_NAME = "className";
    public static final String PROPERTY_PACKAGE_NAME = "packageName";

    @Label
    @Property(PROPERTY_QUALIFIED_NAME)
    public String getQualifiedName();

    @Property(PROPERTY_QUALIFIED_NAME)
    public void setQualifiedName(String qualifiedName);

    @Property(PROPERTY_PACKAGE_NAME)
    public String getPackageName();

    @Property(PROPERTY_PACKAGE_NAME)
    public void setPackageName(String packageName);

    @Property(PROPERTY_CLASS_NAME)
    public void setClassName(String className);

    @Property(PROPERTY_CLASS_NAME)
    public String getClassName();
    
}