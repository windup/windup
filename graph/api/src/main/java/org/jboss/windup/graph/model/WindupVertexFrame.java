package org.jboss.windup.graph.model;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

/**
 * The base {@link VertexFrame} type implemented by all model types.
 */
@TypeField(WindupFrame.TYPE_PROP)
public interface WindupVertexFrame extends VertexFrame, WindupFrame<Vertex>
{
}
