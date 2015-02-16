package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("AmbiguousReference")
public interface AmbiguousReferenceModel<REFERENCETYPE extends WindupVertexFrame> extends WindupVertexFrame
{
    @Adjacency(label = "targets", direction = Direction.OUT)
    public void addReference(final REFERENCETYPE referent);

    @Adjacency(label = "targets", direction = Direction.OUT)
    public Iterable<REFERENCETYPE> getReferences();
}
