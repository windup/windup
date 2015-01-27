package org.jboss.windup.rules.apps.javaee.model;

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
}
