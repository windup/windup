package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

@TypeValue(SpringRemoteServiceModel.TYPE)
public interface SpringRemoteServiceModel extends RemoteServiceModel
{
    String TYPE = "SpringRemoteServiceModel";
    String REMOTESERVICE_IMPLEMENTATION_CLASS = "springremoteImplementationClass";
    String REMOTESERVICE_INTERFACE = "springremoteInterface";
    String SPRINGEXPORTER_INTERFACE = "springremoteExporterInterface";

    @Adjacency(label = REMOTESERVICE_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setImplementationClass(JavaClassModel implRef);

    @Adjacency(label = REMOTESERVICE_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getImplementationClass();

    @Adjacency(label = REMOTESERVICE_INTERFACE, direction = Direction.OUT)
    void setInterface(JavaClassModel interfaceRef);

    @Adjacency(label = REMOTESERVICE_INTERFACE, direction = Direction.OUT)
    JavaClassModel getInterface();

    @Adjacency(label = SPRINGEXPORTER_INTERFACE, direction = Direction.OUT)
    JavaClassModel setSpringExporterInterface(JavaClassModel springExporterInterfaceRef);

    @Adjacency(label = SPRINGEXPORTER_INTERFACE, direction = Direction.OUT)
    JavaClassModel getSpringExporterInterface();
}
