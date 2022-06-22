package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * Contains a Java package name
 */
@TypeValue(PackageModel.TYPE)
public interface PackageModel extends WindupVertexFrame {
    public static final String TYPE = "PackageModel";
    public static final String PACKAGE_NAME = "packageName";

    @Indexed
    @Property(PACKAGE_NAME)
    public String getPackageName();

    @Property(PACKAGE_NAME)
    public void setPackageName(String pkgName);
}
