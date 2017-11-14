package org.jboss.windup.graph.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;

@TypeValue(ApplicationInputPathModel.TYPE)
public interface ApplicationInputPathModel extends FileModel
{
    String TYPE = "ApplicationInputPathModel";
}
