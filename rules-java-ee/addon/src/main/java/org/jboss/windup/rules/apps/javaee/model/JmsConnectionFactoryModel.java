package org.jboss.windup.rules.apps.javaee.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a JMS Connection Factory that is defined or referenced by the application.
 */
@TypeValue(JmsConnectionFactoryModel.TYPE)
public interface JmsConnectionFactoryModel extends JNDIResourceModel
{
    public static final String TYPE = "JmsConnectionFactoryModel";
    public static final String CONNECTION_FACTORY_TYPE = "connectionFactoryType";

    /**
     * Contains JMS destination type (queue / topic)
     */
    @Property(CONNECTION_FACTORY_TYPE)
    public JmsDestinationType getConnectionFactoryType();

    /**
     * Contains JMS destination type (queue / topic)
     */
    @Property(CONNECTION_FACTORY_TYPE)
    public void setConnectionFactoryType(JmsDestinationType type);

}
