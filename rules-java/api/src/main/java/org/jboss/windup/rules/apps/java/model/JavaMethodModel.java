package org.jboss.windup.rules.apps.java.model;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

import java.util.List;

/**
 * Represents a Java Method within a {@link JavaClassModel}
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JavaMethodModel.TYPE)
public interface JavaMethodModel extends WindupVertexFrame
{
    public static final String METHOD_PARAMETER = "methodParameter";
    public static final String METHOD_NAME = "methodName";
    public static final String JAVA_METHOD = "javaMethod";
    public static final String TYPE = "JavaMethodModel";
    String PARAMETER_POSITION = "parameterPosition";

    /**
     * The {@link JavaClassModel} that contains this method
     */
    @Adjacency(label = JAVA_METHOD, direction = Direction.IN)
    public JavaClassModel getJavaClass();

    /**
     * The {@link JavaClassModel} that contains this method
     */
    @Adjacency(label = JAVA_METHOD, direction = Direction.IN)
    public void setJavaClass(JavaClassModel clz);

    /**
     * The name of the Java Method
     */
    @Property(METHOD_NAME)
    public String getMethodName();

    /**
     * The name of the Java Method
     */
    @Property(METHOD_NAME)
    public void setMethodName(String methodName);

    /**
     * Returns the number of method parameters to this method
     */
    default long countParameters()
    {
        return new GraphTraversalSource(getWrappedGraph().getBaseGraph()).V(getElement())
                .out(METHOD_PARAMETER).toList().size();
    }


    /**
     * Returns all parameters to this method
     */
    @Adjacency(label = METHOD_PARAMETER, direction = Direction.OUT)
    public List<JavaParameterModel> getMethodParameters();

    /**
     * Adds the provided {@link JavaParameterModel} parameter
     */
    @Adjacency(label = METHOD_PARAMETER, direction = Direction.OUT)
    public void addMethodParameter(JavaParameterModel param);

    /**
     * Returns the {@link JavaParameterModel} at the provided position in the parameter list.
     */
    default JavaParameterModel getParameter(int parameterPosition)
    {
/*
        List<Vertex> vertices = new GraphTraversalSource(getWrappedGraph().getBaseGraph()).V(getElement())
                .in(METHOD_PARAMETER)
                .has(PARAMETER_POSITION, parameterPosition)
                .toList();
        return vertices.stream().map(v -> getGraph().frameElement(v, JavaParameterModel.class)).findFirst().get();
*/
        return this.traverse(v -> v.out(METHOD_PARAMETER).has(PARAMETER_POSITION, parameterPosition)).next(JavaParameterModel.class);
    }


}
