package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * RMI Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(RMIServiceBeanModel.TYPE)
public interface RMIServiceBeanModel extends WebServiceBeanModel
{
    public static final String TYPE = "RMIService";
    public static final String RMI_IMPLEMENTATION_CLASS = "rmiImplementationClass";
    public static final String RMI_INTERFACE = "rmiInterface";
    
    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public void setImplementationClass(JavaClassModel restImplementation);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public JavaClassModel getImplementationClass();

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_INTERFACE, direction = Direction.OUT)
    public void setInterface(JavaClassModel jaxwsInterface);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_INTERFACE, direction = Direction.OUT)
    public JavaClassModel getInterface();
}
