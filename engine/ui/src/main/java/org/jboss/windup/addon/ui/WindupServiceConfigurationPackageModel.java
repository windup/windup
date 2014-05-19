package org.jboss.windup.addon.ui;

import org.jboss.windup.graph.model.meta.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("WindupServiceConfigurationPackageModel")
public interface WindupServiceConfigurationPackageModel extends WindupVertexFrame
{
    @Property("packageName")
    public String getPackageName();

    @Property("packageName")
    public void setPackageName(String pkgName);
}
