package org.jboss.windup.rules.apps.java.scan.model;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.resource.ResourceModel;

@TypeValue("JavaMethod")
public interface JavaMethodModel extends ResourceModel
{

    @Adjacency(label = "javaMethod", direction = Direction.IN)
    public JavaClassModel getJavaClass();

    @Adjacency(label = "javaMethod", direction = Direction.IN)
    public void setJavaClass(JavaClassModel clz);

    @Label
    @Property("methodName")
    public String getMethodName();

    @Property("methodName")
    public void setMethodName(String methodName);

    @GremlinGroovy(value = "it.out('methodParameter').count()", frame = false)
    public Long countParameters();

    @Adjacency(label = "methodParameter", direction = Direction.OUT)
    public Iterable<JavaParameterModel> getMethodParameters();

    @Adjacency(label = "methodParameter", direction = Direction.OUT)
    public void addMethodParameter(JavaParameterModel param);

    @GremlinGroovy("it.out('methodParameter').has('parameterPosition', parameterPosition)")
    public JavaParameterModel getParameter(@GremlinParam("parameterPosition") int parameterPosition);

}
