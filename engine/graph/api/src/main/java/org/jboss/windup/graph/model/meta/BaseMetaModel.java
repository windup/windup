package org.jboss.windup.graph.model.meta;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("Meta")
public interface BaseMetaModel extends WindupVertexFrame {

	@GremlinGroovy("it.in('meta')")
	public Vertex getMetaReference();
}
