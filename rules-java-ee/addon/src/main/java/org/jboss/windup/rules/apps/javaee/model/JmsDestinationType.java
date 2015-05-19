package org.jboss.windup.rules.apps.javaee.model;

/**
 * Defines whether or not a JMS destination represents a JMS Topic or a Queue.
 */
public enum JmsDestinationType
{
    QUEUE("Queue"), TOPIC("Topic");
    
    private final String name;
    
    private JmsDestinationType(String name)
    {
        this.name = name;
    }
    
    public String toString() {
        return name;
    }
}
