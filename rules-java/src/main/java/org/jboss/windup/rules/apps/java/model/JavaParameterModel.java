package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains information regarding parameters to a Java Method.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(JavaParameterModel.TYPE)
public interface JavaParameterModel extends WindupVertexFrame
{

    public static final String METHOD_PARAMETER_TYPE = "methodParameterType";
    public static final String PARAMETER_POSITION = "parameterPosition";
    public static final String TYPE = "JavaParameter";

    /**
     * The {@link JavaMethodModel} containing this parameter.
     */
    @Adjacency(label = JavaMethodModel.METHOD_PARAMETER, direction = Direction.IN)
    public JavaMethodModel getJavaMethod();

    /**
     * The {@link JavaMethodModel} containing this parameter.
     */
    @Adjacency(label = JavaMethodModel.METHOD_PARAMETER, direction = Direction.IN)
    public void setJavaMethod(JavaMethodModel method);

    /**
     * Contains the parameter's position within the parameter list (0-based index)
     */
    @Property(PARAMETER_POSITION)
    public int getPosition();

    /**
     * Contains the parameter's position within the parameter list (0-based index)
     */
    @Property(PARAMETER_POSITION)
    public void setPosition(int parameterPosition);

    /**
     * Contains the parameter type
     */
    @Adjacency(label = METHOD_PARAMETER_TYPE, direction = Direction.OUT)
    public JavaClassModel getJavaType();

    /**
     * Contains the parameter type
     */
    @Adjacency(label = METHOD_PARAMETER_TYPE, direction = Direction.OUT)
    public void setJavaType(JavaClassModel clz);
}
