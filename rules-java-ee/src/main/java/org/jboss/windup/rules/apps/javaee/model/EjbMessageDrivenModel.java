package org.jboss.windup.rules.apps.javaee.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains EJB Message Driven model information and related data.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(EjbMessageDrivenModel.TYPE)
public interface EjbMessageDrivenModel extends EjbBeanBaseModel
{

    public static final String TYPE = "EjbMessageDriven";
    public static final String DESTINATION = "destination";

    /**
     * Contains the destination address, typically a JMS queue or topic
     */
    @Property(DESTINATION)
    String getDestination();

    /**
     * Contains the destination address, typically a JMS queue or topic
     */
    @Property(DESTINATION)
    void setDestination(String destination);

    /**
     * References the Deployment Descriptor containing EJB.
     */
    @Adjacency(label = EjbDeploymentDescriptorModel.MESSAGE_DRIVEN, direction = Direction.IN)
    public EjbDeploymentDescriptorModel getEjbDeploymentDescriptor();

}
