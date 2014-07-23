package org.jboss.windup.config.selectables;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("parent")
public interface TestParentModel extends WindupVertexFrame
{
    String NAME = "name";

    @Property(NAME)
    String getName();

    @Property(NAME)
    void setName(String name);
}
