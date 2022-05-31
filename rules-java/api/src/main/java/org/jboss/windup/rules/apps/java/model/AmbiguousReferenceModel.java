package org.jboss.windup.rules.apps.java.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;

@TypeValue(AmbiguousReferenceModel.TYPE)
public interface AmbiguousReferenceModel<REFERENCETYPE extends WindupVertexFrame> extends WindupVertexFrame {
    String TYPE = "AmbiguousReferenceModel";

    @Adjacency(label = "targets", direction = Direction.OUT)
    void addReference(final REFERENCETYPE referent);

    @Adjacency(label = "targets", direction = Direction.OUT)
    List<REFERENCETYPE> getReferences();
}
