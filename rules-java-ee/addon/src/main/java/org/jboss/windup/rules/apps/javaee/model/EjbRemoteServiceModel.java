package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

/**
 * Marker interface for Ejb Remote Service.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(EjbRemoteServiceModel.TYPE)
public interface EjbRemoteServiceModel extends RemoteServiceModel {
    String TYPE = "EjbRemoteServiceModel";
    String EJB_IMPLEMENTATION_CLASS = "ejbImplementationClass";
    String EJB_INTERFACE = "ejbRemoteInterface";

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = EJB_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getImplementationClass();

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = EJB_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = EJB_INTERFACE, direction = Direction.OUT)
    JavaClassModel getInterface();

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = EJB_INTERFACE, direction = Direction.OUT)
    void setInterface(JavaClassModel interfaceRef);
}
