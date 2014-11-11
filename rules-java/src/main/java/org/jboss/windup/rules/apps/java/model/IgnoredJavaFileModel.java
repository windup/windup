package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(IgnoredJavaFileModel.TYPE)
public interface IgnoredJavaFileModel extends FileModel
{
    public static final String TYPE = "IgnoredJavaFileModel";
}
