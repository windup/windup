package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * Represents a JMS Connection Factory that is defined or referenced by the application.
 */
@TypeValue(JmsConnectionFactoryModel.TYPE)
public interface JmsConnectionFactoryModel extends JNDIResourceModel {
    String TYPE = "JmsConnectionFactoryModel";
    String CONNECTION_FACTORY_TYPE = "connectionFactoryType";

    /**
     * Contains JMS destination type (queue / topic)
     */
    @Property(CONNECTION_FACTORY_TYPE)
    JmsDestinationType getConnectionFactoryType();

    /**
     * Contains JMS destination type (queue / topic)
     */
    @Property(CONNECTION_FACTORY_TYPE)
    void setConnectionFactoryType(JmsDestinationType type);
}
