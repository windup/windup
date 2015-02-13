package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains a Java package name
 *
 */
@TypeValue(PackageModel.TYPE)
public interface PackageModel extends WindupVertexFrame
{
    public static final String TYPE = "WindupServiceConfigurationPackageModel";
    public static final String PACKAGE_NAME = "packageName";

    @Property(PACKAGE_NAME)
    public String getPackageName();

    @Property(PACKAGE_NAME)
    public void setPackageName(String pkgName);
}
