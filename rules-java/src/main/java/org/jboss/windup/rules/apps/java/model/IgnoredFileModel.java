package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Indicates that a given file was ignored by windup.
 */
@TypeValue(IgnoredFileModel.TYPE)
public interface IgnoredFileModel extends FileModel
{
    public static final String TYPE = "IgnoredFileModel";
}
