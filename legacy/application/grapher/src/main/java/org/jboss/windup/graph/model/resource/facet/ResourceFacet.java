package org.jboss.windup.graph.model.resource.facet;

import org.jboss.windup.graph.model.resource.Resource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

@TypeField("type")
public interface ResourceFacet extends VertexFrame {
	@Adjacency(label="resource", direction=Direction.OUT)
	public Resource getResource();
	
	@Adjacency(label="resource", direction=Direction.OUT)
	public Resource setResource(final Resource resource);
}
