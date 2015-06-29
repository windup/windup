package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * JAX-RPC Web Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(JaxRPCWebServiceModel.TYPE)
public interface JaxRPCWebServiceModel extends WebServiceModel
{
    public static final String TYPE = "JaxRPCService";
    public static final String JAXRPC_IMPLEMENTATION_CLASS = "jaxrpcImplementationClass";
    public static final String JAXRPC_INTERFACE = "jaxrpcInterface";
    public static final String JAXRPC_XML_DESCRIPTOR = "jaxrpcXmlDescriptor";
    
    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public void setImplementationClass(JavaClassModel restImplementation);

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public JavaClassModel getImplementationClass();

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_INTERFACE, direction = Direction.OUT)
    public void setInterface(JavaClassModel jaxrpcInterface);

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_INTERFACE, direction = Direction.OUT)
    public JavaClassModel getInterface();
    
    
    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_XML_DESCRIPTOR, direction = Direction.OUT)
    public void setXmlDescriptor(XmlFileModel xmlFile);

    /**
     * Contains the JAX-WS implementation class
     */
    @Adjacency(label = JAXRPC_XML_DESCRIPTOR, direction = Direction.OUT)
    public XmlFileModel getXmlDescriptor();
    
}
