package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * JAX-WS Web Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(JaxWSWebServiceModel.TYPE)
public interface JaxWSWebServiceModel extends WebServiceModel
{
    String TYPE = "JaxWSWebServiceModel";
    String JAXWS_IMPLEMENTATION_CLASS = "jaxwsImplementationClass";
    String JAXWS_INTERFACE = "jaxwsInterface";
     
    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXWS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXWS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getImplementationClass();

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXWS_INTERFACE, direction = Direction.OUT)
    void setInterface(JavaClassModel interfaceRef);

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXWS_INTERFACE, direction = Direction.OUT)
    JavaClassModel getInterface();
}
