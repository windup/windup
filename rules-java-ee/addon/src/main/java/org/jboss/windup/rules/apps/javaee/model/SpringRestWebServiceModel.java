package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

/**
 * JaxRS REST Web Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(SpringRestWebServiceModel.TYPE)
public interface SpringRestWebServiceModel extends WebServiceModel
{
    String TYPE = "SpringRestWebServiceModel";
    String JAXRS_IMPLEMENTATION_CLASS = "springRestImplementationClass";
    String JAXRS_INTERFACE = "springRestInterface";
    
    String PATH = "springRestPath";


    /**
     * Contains the URL path to the Spring Rest service.
     */
    @Property(PATH)
    @Indexed
    String getPath();

    /**
     * Contains the URL path to the Spring Rest service.
     */
    @Property(PATH)
    void setPath(String packageName);
    
    /**
     * Contains the Spring Rest implementation class
     */
    @Adjacency(label = JAXRS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the Spring Rest implementation class
     */
    @Adjacency(label = JAXRS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getImplementationClass();

    /**
     * Contains the Spring Rest implementation class
     */
    @Adjacency(label = JAXRS_INTERFACE, direction = Direction.OUT)
    void setInterface(JavaClassModel interfaceRef);

    /**
     * Contains the Spring Rest implementation class
     */
    @Adjacency(label = JAXRS_INTERFACE, direction = Direction.OUT)
    JavaClassModel getInterface();
}
