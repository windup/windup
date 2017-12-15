package org.jboss.windup.rules.apps.javaee.model.association;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(JNDIReferenceModel.TYPE)
public interface JNDIReferenceModel extends WindupVertexFrame
{
    String TYPE = "JNDIReferenceModel";
    String REF = "jndi";
    
    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = REF, direction = Direction.OUT)
    JNDIResourceModel getJndiReference();

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = REF, direction = Direction.OUT)
    void setJndiReference(JNDIResourceModel jndiReference);
}
