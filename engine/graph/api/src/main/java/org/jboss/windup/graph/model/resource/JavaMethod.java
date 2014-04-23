package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaMethod")
public interface JavaMethod extends Resource
{

    @Adjacency(label = "javaMethod", direction = Direction.IN)
    public JavaClass getJavaClass();

    @Adjacency(label = "javaMethod", direction = Direction.IN)
    public void setJavaClass(JavaClass clz);

    @Label
    @Property("methodName")
    public String getMethodName();

    @Property("methodName")
    public void setMethodName(String methodName);

    @GremlinGroovy(value = "it.out('methodParameter').count()", frame = false)
    public Long countParameters();

    @Adjacency(label = "methodParameter", direction = Direction.OUT)
    public Iterable<JavaParameter> getMethodParameters();

    @Adjacency(label = "methodParameter", direction = Direction.OUT)
    public void addMethodParameter(JavaParameter param);

    @GremlinGroovy("it.out('methodParameter').has('parameterPosition', parameterPosition)")
    public JavaParameter getParameter(@GremlinParam("parameterPosition") int parameterPosition);

}
