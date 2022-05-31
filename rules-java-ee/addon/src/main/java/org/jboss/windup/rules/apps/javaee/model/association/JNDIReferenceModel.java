package org.jboss.windup.rules.apps.javaee.model.association;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;

@TypeValue(JNDIReferenceModel.TYPE)
public interface JNDIReferenceModel extends WindupVertexFrame {
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
