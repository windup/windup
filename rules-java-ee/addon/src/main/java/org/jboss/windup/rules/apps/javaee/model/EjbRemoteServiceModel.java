package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Marker interface for Ejb Remote Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(EjbRemoteServiceModel.TYPE)
public interface EjbRemoteServiceModel extends RemoteServiceModel
{
    public static final String TYPE = "EjbRemoteService";
    public static final String EJB_IMPLEMENTATION_CLASS = "ejbImplementationClass";
    public static final String EJB_INTERFACE = "ejbRemoteInterface";
    
    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = EJB_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = EJB_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public JavaClassModel getImplementationClass();

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = EJB_INTERFACE, direction = Direction.OUT)
    public void setInterface(JavaClassModel interfaceRef);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = EJB_INTERFACE, direction = Direction.OUT)
    public JavaClassModel getInterface();
}
