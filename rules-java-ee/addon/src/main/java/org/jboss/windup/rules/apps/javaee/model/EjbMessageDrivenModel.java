package org.jboss.windup.rules.apps.javaee.model;

import java.util.Map;

import org.jboss.windup.graph.MapInAdjacentProperties;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains EJB Message Driven model information and related data.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(EjbMessageDrivenModel.TYPE)
public interface EjbMessageDrivenModel extends EjbBeanBaseModel
{

    public static final String TYPE = "EjbMessageDriven";
    public static final String DESTINATION = "destination";
    

    
    /**
     * Contains the destination address, typically a JMS queue or topic
     */
    @Adjacency(label = DESTINATION, direction = Direction.IN)
    JmsDestinationModel getDestination();

    /**
     * Contains the destination address, typically a JMS queue or topic
     */
    @Adjacency(label = DESTINATION, direction = Direction.IN)
    void setDestination(JmsDestinationModel destination);

    /**
     * References the Deployment Descriptor containing EJB.
     */
    @Adjacency(label = EjbDeploymentDescriptorModel.MESSAGE_DRIVEN, direction = Direction.IN)
    public EjbDeploymentDescriptorModel getEjbDeploymentDescriptor();


    /**
     * Timeouts for each method pattern in seconds, * is wildcard
     */
    @MapInAdjacentProperties(label = "txTimeouts")
    Map<String, Integer> getTxTimeouts();

    /**
     * Timeouts for each method pattern, * is wildcard
     */
    @MapInAdjacentProperties(label = "txTimeouts")
    void setTxTimeouts(Map<String, Integer> map);
}
