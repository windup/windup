package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata associated with a Spring Bean.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@TypeValue("SpringBean")
public interface SpringBeanModel extends WindupVertexFrame
{
    public static final String SPRING_BEAN_TO_JAVA_CLASS = "springBeanToJavaClass";
    public static final String SPRING_CONFIGURATION = "springConfiguration";
    public static final String SPRING_BEAN_NAME = "springBeanName";

    /**
     * The name of this spring bean
     */
    @Property(SPRING_BEAN_NAME)
    public String getSpringBeanName();

    /**
     * The name of this spring bean
     */
    @Property(SPRING_BEAN_NAME)
    public String setSpringBeanName(String springBeanName);

    /**
     * The Spring configuration file in which this Spring Bean was defined.
     */
    @Adjacency(label = SPRING_CONFIGURATION, direction = Direction.IN)
    public SpringConfigurationFileModel getSpringConfiguration();

    /**
     * The Spring configuration file in which this Spring Bean was defined.
     */
    @Adjacency(label = SPRING_CONFIGURATION, direction = Direction.IN)
    public void setSpringConfiguration(SpringConfigurationFileModel springConfiguration);

    /**
     * This points to the @{link JavaClassModel} referenced by this Spring Bean
     */
    @Adjacency(label = SPRING_BEAN_TO_JAVA_CLASS, direction = Direction.OUT)
    public JavaClassModel getJavaClass();

    /**
     * This points to the @{link JavaClassModel} referenced by this Spring Bean
     */
    @Adjacency(label = SPRING_BEAN_TO_JAVA_CLASS, direction = Direction.OUT)
    public void setJavaClass(JavaClassModel m);
}
