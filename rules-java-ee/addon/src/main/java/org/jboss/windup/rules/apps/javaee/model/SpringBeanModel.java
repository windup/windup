package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

import java.util.Set;

/**
 * Contains metadata associated with a Spring Bean.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
@TypeValue(SpringBeanModel.TYPE)
public interface SpringBeanModel extends WindupVertexFrame
{
    String TYPE = "SpringBeanModel";
    String SPRING_BEAN_TO_JAVA_CLASS = "springBeanToJavaClass";
    String SPRING_CONFIGURATION = "springConfiguration";
    String SPRING_BEAN_NAME = "springBeanName";
    String APPLICATIONS = "applicationS";

    /**
     * The name of this spring bean
     */
    @Indexed
    @Property(SPRING_BEAN_NAME)
    String getSpringBeanName();

    /**
     * The name of this spring bean
     */
    @Property(SPRING_BEAN_NAME)
    String setSpringBeanName(String springBeanName);

    /**
     * The Spring configuration file in which this Spring Bean was defined.
     */
    @Adjacency(label = SPRING_CONFIGURATION, direction = Direction.IN)
    SpringConfigurationFileModel getSpringConfiguration();

    /**
     * The Spring configuration file in which this Spring Bean was defined.
     */
    @Adjacency(label = SPRING_CONFIGURATION, direction = Direction.IN)
    void setSpringConfiguration(SpringConfigurationFileModel springConfiguration);

    /**
     * This points to the @{link JavaClassModel} referenced by this Spring Bean
     */
    @Adjacency(label = SPRING_BEAN_TO_JAVA_CLASS, direction = Direction.OUT)
    JavaClassModel getJavaClass();

    /**
     * This points to the @{link JavaClassModel} referenced by this Spring Bean
     */
    @Adjacency(label = SPRING_BEAN_TO_JAVA_CLASS, direction = Direction.OUT)
    void setJavaClass(JavaClassModel m);

    /**
     * Contains the application in which this Spring Bean was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    Set<ProjectModel> getApplications();

    /**
     * Contains the application in which this Spring Bean  was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    void setApplications(Iterable<ProjectModel> applications);
}
