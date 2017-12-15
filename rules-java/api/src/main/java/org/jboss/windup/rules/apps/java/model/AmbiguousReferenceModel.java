package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(AmbiguousReferenceModel.TYPE)
public interface AmbiguousReferenceModel<REFERENCETYPE extends WindupVertexFrame> extends WindupVertexFrame
{
    String TYPE = "AmbiguousReferenceModel";

    @Adjacency(label = "targets", direction = Direction.OUT)
    public void addReference(final REFERENCETYPE referent);

    @Adjacency(label = "targets", direction = Direction.OUT)
    public Iterable<REFERENCETYPE> getReferences();
}
