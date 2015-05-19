package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a JDNI resource found within the application.
 */
@TypeValue(JNDIResourceModel.TYPE)
public interface JNDIResourceModel extends WindupVertexFrame
{
    public static final String TYPE = "JNDIResourceModel";
    public static final String JNDI_LOCATION = "JNDI_LOCATION";
    
    /**
     * Contains JNDI Location
     */
    @Indexed
    @Property(JNDI_LOCATION)
    public String getJndiLocation();

    /**
     * Contains JNDI Location
     */
    @Property(JNDI_LOCATION)
    public void setJndiLocation(String jndiLocation);

}
