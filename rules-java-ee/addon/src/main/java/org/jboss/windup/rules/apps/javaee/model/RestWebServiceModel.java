package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

/**
 * JaxRS REST Web Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(RestWebServiceModel.TYPE)
public interface RestWebServiceModel extends WebServiceModel
{
    String TYPE = "RestWebServiceModel";
    String JAXRS_IMPLEMENTATION_CLASS = "jaxrsImplementationClass";
    String JAXRS_INTERFACE = "jaxrsInterface";
    
    String PATH = "jaxrsPath";
    String ORIGIN = "origin";
    

    /**
     * Contains the URL path to the JaxRS service.
     */
    @Property(PATH)
    @Indexed
    String getPath();

    /**
     * Contains the URL path to the JaxRS service.
     */
    @Property(PATH)
    void setPath(String packageName);
    
    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getImplementationClass();

    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_INTERFACE, direction = Direction.OUT)
    void setInterface(JavaClassModel interfaceRef);

    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_INTERFACE, direction = Direction.OUT)
    JavaClassModel getInterface();

    @Property(ORIGIN)
    @Indexed
    String getOrigin();

    @Property(ORIGIN)
    void setOrigin(String source);
}
