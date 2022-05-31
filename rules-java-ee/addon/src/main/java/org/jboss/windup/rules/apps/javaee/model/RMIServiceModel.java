package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

/**
 * RMI Service marker interface.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(RMIServiceModel.TYPE)
public interface RMIServiceModel extends RemoteServiceModel {
    String TYPE = "RMIServiceModel";
    String RMI_IMPLEMENTATION_CLASS = "rmiImplementationClass";
    String RMI_INTERFACE = "rmiInterface";

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getImplementationClass();

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_INTERFACE, direction = Direction.OUT)
    JavaClassModel getInterface();

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = RMI_INTERFACE, direction = Direction.OUT)
    void setInterface(JavaClassModel interfaceRef);
}
