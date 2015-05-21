package org.jboss.windup.rules.apps.javaee.model.association;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(JNDIReferenceModel.TYPE)
public interface JNDIReferenceModel extends WindupVertexFrame
{
    public static final String TYPE = "JndiReferenceModel";
    public static final String REF = "jndi";
    
    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = REF, direction = Direction.OUT)
    public JNDIResourceModel getJndiReference();

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = REF, direction = Direction.OUT)
    public void setJndiReference(JNDIResourceModel jndiReference);
}
