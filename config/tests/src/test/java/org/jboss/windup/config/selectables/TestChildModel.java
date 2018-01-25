package org.jboss.windup.config.selectables;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

@TypeValue("child")
public interface TestChildModel extends WindupVertexFrame
{
    String PARENT = "parent";
    String NAME = "name";

    @Property(NAME)
    String getName();

    @Property(NAME)
    void setName(String name);

    @Adjacency(label = PARENT, direction = Direction.OUT)
    TestParentModel getParent();

    @Adjacency(label = PARENT, direction = Direction.OUT)
    TestParentModel setParent(TestParentModel file);
}
