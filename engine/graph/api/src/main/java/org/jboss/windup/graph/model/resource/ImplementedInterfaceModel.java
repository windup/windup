package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;

public interface ImplementedInterfaceModel extends VertexFrame
{
    @Property("className")
    public String getClassName();
    @Property("className")
    public void setClassName(String className);
}
