package org.jboss.windup.graph.model;

import com.syncleus.ferma.VertexFrame;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * The base {@link VertexFrame} type implemented by all model types.
 */
@TypeField(WindupFrame.TYPE_PROP)
public interface WindupVertexFrame extends VertexFrame, WindupFrame<Vertex>
{
}
