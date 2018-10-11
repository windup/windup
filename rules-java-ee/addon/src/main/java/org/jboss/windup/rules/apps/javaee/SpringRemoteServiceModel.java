package org.jboss.windup.rules.apps.javaee;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.RemoteServiceModel;

/**
 * RMI Service marker interface.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(SpringRemoteServiceModel.TYPE)
public interface SpringRemoteServiceModel extends RemoteServiceModel
{
    String TYPE = "SpringRemoteServiceModel";
    String REMOTESERVICE_IMPLEMENTATION_CLASS = "springremoteImplementationClass";
    String REMOTESERVICE_INTERFACE = "springremoteInterface";
    String SPRINGEXPORTER_INTERFACE = "springremoteExporterInterface";

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = REMOTESERVICE_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = REMOTESERVICE_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getImplementationClass();

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = REMOTESERVICE_INTERFACE, direction = Direction.OUT)
    void setInterface(JavaClassModel interfaceRef);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = REMOTESERVICE_INTERFACE, direction = Direction.OUT)
    JavaClassModel getInterface();

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = SPRINGEXPORTER_INTERFACE, direction = Direction.OUT)
    JavaClassModel setSpringExporterInterface(JavaClassModel springExporterInterfaceRef);

    /**
     * Contains the RMI implementation class
     */
    @Adjacency(label = SPRINGEXPORTER_INTERFACE, direction = Direction.OUT)
    JavaClassModel getSpringExporterInterface();
}
