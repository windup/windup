package org.jboss.windup.graph.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("WindupServiceConfigurationPackageModel")
public interface PackageModel extends WindupVertexFrame
{
    @Property("packageName")
    public String getPackageName();

    @Property("packageName")
    public void setPackageName(String pkgName);
}
