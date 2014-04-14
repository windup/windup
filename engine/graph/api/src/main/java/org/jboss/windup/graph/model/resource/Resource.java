package org.jboss.windup.graph.model.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.jboss.windup.graph.model.meta.Meta;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;


@TypeField("type") 
@TypeValue("BaseResource")
public interface Resource extends VertexFrame {

	@Adjacency(label="meta", direction=Direction.OUT)
	public Iterator<Meta> getMeta();
	
	@Adjacency(label="meta", direction=Direction.OUT)
	public void addMeta(final Meta resource);

	public InputStream asInputStream() throws IOException;
}
