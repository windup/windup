package org.jboss.windup.graph.model.meta;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeField("type") 
@TypeValue("Meta")
public interface Meta extends VertexFrame {

	@GremlinGroovy("it.in('meta')")
	public Vertex getMetaReference();
}
