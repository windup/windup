package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * JaxRS REST Web Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(JaxRSWebServiceBeanModel.TYPE)
public interface JaxRSWebServiceBeanModel extends WebServiceBeanModel
{
    public static final String TYPE = "JaxRSWebService";
    public static final String JAXRS_IMPLEMENTATION_CLASS = "jaxrsImplementationClass";
    public static final String JAXRS_INTERFACE = "jaxrsInterface";
    
    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public void setImplementationClass(JavaClassModel jaxrsImplementation);

    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public JavaClassModel getImplementationClass();

    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_INTERFACE, direction = Direction.OUT)
    public void setInterface(JavaClassModel jaxrsInterface);

    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_INTERFACE, direction = Direction.OUT)
    public JavaClassModel getInterface();
}
