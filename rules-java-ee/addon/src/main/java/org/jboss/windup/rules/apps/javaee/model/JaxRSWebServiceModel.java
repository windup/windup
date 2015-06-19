package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * JaxRS REST Web Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(JaxRSWebServiceModel.TYPE)
public interface JaxRSWebServiceModel extends WebServiceModel
{
    public static final String TYPE = "JaxRSWebService";
    public static final String JAXRS_IMPLEMENTATION_CLASS = "jaxrsImplementationClass";
    public static final String JAXRS_INTERFACE = "jaxrsInterface";
    
    public static final String PATH = "jaxrsPath";
    

    /**
     * Contains the URL path to the JaxRS service.
     */
    @Property(PATH)
    public String getPath();

    /**
     * Contains the URL path to the JaxRS service.
     */
    @Property(PATH)
    public void setPath(String packageName);
    
    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public void setImplementationClass(JavaClassModel implRef);

    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public JavaClassModel getImplementationClass();

    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_INTERFACE, direction = Direction.OUT)
    public void setInterface(JavaClassModel interfaceRef);

    /**
     * Contains the JAX-RS implementation class
     */
    @Adjacency(label = JAXRS_INTERFACE, direction = Direction.OUT)
    public JavaClassModel getInterface();
}
