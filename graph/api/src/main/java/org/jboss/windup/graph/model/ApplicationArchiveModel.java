package org.jboss.windup.graph.model;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(ApplicationArchiveModel.TYPE)
public interface ApplicationArchiveModel extends ArchiveModel
{
    String TYPE = "ApplicationArchiveModel";
}
