package org.jboss.windup.graph.model;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(ApplicationArchiveModel.TYPE)
public interface ApplicationArchiveModel extends ApplicationModel, ArchiveModel
{
    String TYPE = "ApplicationArchiveModel";
}
