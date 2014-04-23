package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.meta.Meta;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaParameter")
public interface JavaParameter extends Meta
{

    @Adjacency(label = "methodParameter", direction = Direction.IN)
    public JavaMethod getJavaMethod();

    @Adjacency(label = "methodParameter", direction = Direction.IN)
    public void setJavaMethod(JavaMethod method);

    @Label
    @Property("parameterPosition")
    public int getPosition();

    @Property("parameterPosition")
    public void setPosition(int parameterPosition);

    @Adjacency(label = "methodParameterType", direction = Direction.OUT)
    public JavaClass getJavaType();

    @Adjacency(label = "methodParameterType", direction = Direction.OUT)
    public void setJavaType(JavaClass clz);
}
