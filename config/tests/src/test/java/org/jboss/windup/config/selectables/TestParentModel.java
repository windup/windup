package org.jboss.windup.config.selectables;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.jboss.windup.graph.Property;

@TypeValue("parent")
public interface TestParentModel extends WindupVertexFrame {
    String NAME = "name";

    @Property(NAME)
    String getName();

    @Property(NAME)
    void setName(String name);
}
