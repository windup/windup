package org.jboss.windup.graph.model;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

/**
 * The base {@link VertexFrame} type implemented by all model types.
 */
@TypeField(WindupFrame.TYPE_PROP)
public interface WindupVertexFrame extends VertexFrame, WindupFrame<Vertex>
{
}
