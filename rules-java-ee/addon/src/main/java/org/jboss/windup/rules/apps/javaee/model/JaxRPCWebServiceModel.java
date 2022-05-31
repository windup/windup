package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

/**
 * JAX-RPC Web Service.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(JaxRPCWebServiceModel.TYPE)
public interface JaxRPCWebServiceModel extends WebServiceModel {
    String TYPE = "JaxRPCWebServiceModel";
    String JAXRPC_IMPLEMENTATION_CLASS = "jaxrpcImplementationClass";
    String JAXRPC_INTERFACE = "jaxrpcInterface";
    String JAXRPC_XML_DESCRIPTOR = "jaxrpcXmlDescriptor";

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getImplementationClass();

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_INTERFACE, direction = Direction.OUT)
    JavaClassModel getInterface();

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_INTERFACE, direction = Direction.OUT)
    void setInterface(JavaClassModel interfaceRef);

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_XML_DESCRIPTOR, direction = Direction.OUT)
    XmlFileModel getXmlDescriptor();

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_XML_DESCRIPTOR, direction = Direction.OUT)
    void setXmlDescriptor(XmlFileModel xmlFile);
}
