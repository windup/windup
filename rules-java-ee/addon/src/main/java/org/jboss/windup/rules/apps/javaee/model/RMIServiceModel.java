package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * RMI Service marker interface.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(RMIServiceModel.TYPE)
public interface RMIServiceModel extends RemoteServiceModel
{
    String TYPE = "RMIServiceModel";
    String RMI_IMPLEMENTATION_CLASS = "rmiImplementationClass";
    String RMI_INTERFACE = "rmiInterface";
    
    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getImplementationClass();

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_INTERFACE, direction = Direction.OUT)
    void setInterface(JavaClassModel interfaceRef);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_INTERFACE, direction = Direction.OUT)
    JavaClassModel getInterface();
}
