package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a Java Method within a {@link JavaClassModel}
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(JavaMethodModel.TYPE)
public interface JavaMethodModel extends WindupVertexFrame
{
    public static final String METHOD_PARAMETER = "methodParameter";
    public static final String METHOD_NAME = "methodName";
    public static final String JAVA_METHOD = "javaMethod";
    public static final String TYPE = "JavaMethod";

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
    @GremlinGroovy(value = "it.out('" + METHOD_PARAMETER + "').count()", frame = false)
    public long countParameters();

    /**
     * Returns all parameters to this method
     */
    @Adjacency(label = METHOD_PARAMETER, direction = Direction.OUT)
    public Iterable<JavaParameterModel> getMethodParameters();

    /**
     * Adds the provided {@link JavaParameterModel} parameter
     */
    @Adjacency(label = METHOD_PARAMETER, direction = Direction.OUT)
    public void addMethodParameter(JavaParameterModel param);

    /**
     * Returns the {@link JavaParameterModel} at the provided position in the parameter list.
     */
    @GremlinGroovy("it.out('methodParameter').has('parameterPosition', parameterPosition)")
    public JavaParameterModel getParameter(@GremlinParam("parameterPosition") int parameterPosition);

}
