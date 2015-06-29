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
@TypeValue(JaxWSWebServiceBeanModel.TYPE)
public interface JaxWSWebServiceBeanModel extends WebServiceBeanModel
{
    public static final String TYPE = "JaxWSService";
    public static final String JAXWS_IMPLEMENTATION_CLASS = "jaxwsImplementationClass";
    public static final String JAXWS_INTERFACE = "jaxwsInterface";
     
    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXWS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public void setImplementationClass(JavaClassModel restImplementation);

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXWS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public JavaClassModel getImplementationClass();

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXWS_INTERFACE, direction = Direction.OUT)
    public void setInterface(JavaClassModel jaxwsInterface);

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXWS_INTERFACE, direction = Direction.OUT)
    public JavaClassModel getInterface();
}
