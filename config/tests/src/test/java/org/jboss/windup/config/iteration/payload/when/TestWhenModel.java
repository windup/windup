package org.jboss.windup.config.iteration.payload.when;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("TestWhenModel")
public interface TestWhenModel extends WindupVertexFrame
{

    public static final String SECOND_NAME = "secondName";
    public static final String NAME = "name";

    @Property(NAME)
    String getName();

    @Property(NAME)
    void setName(String name);
    
    @Property(SECOND_NAME)
    String getSecondName();

    @Property(SECOND_NAME)
    void setSecondName(String name);
}
