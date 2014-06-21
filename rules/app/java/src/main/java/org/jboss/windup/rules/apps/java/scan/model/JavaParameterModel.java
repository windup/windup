package org.jboss.windup.rules.apps.java.scan.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.reporting.renderer.api.Label;


@TypeValue("JavaParameter")
public interface JavaParameterModel extends WindupVertexFrame
{

    @Adjacency(label = "methodParameter", direction = Direction.IN)
    public JavaMethodModel getJavaMethod();

    @Adjacency(label = "methodParameter", direction = Direction.IN)
    public void setJavaMethod(JavaMethodModel method);

    @Label
    @Property("parameterPosition")
    public int getPosition();

    @Property("parameterPosition")
    public void setPosition(int parameterPosition);

    @Adjacency(label = "methodParameterType", direction = Direction.OUT)
    public JavaClassModel getJavaType();

    @Adjacency(label = "methodParameterType", direction = Direction.OUT)
    public void setJavaType(JavaClassModel clz);
}
