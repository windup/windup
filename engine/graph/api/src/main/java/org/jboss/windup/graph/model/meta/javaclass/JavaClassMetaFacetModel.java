package org.jboss.windup.graph.model.meta.javaclass;

import org.jboss.windup.graph.model.resource.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

@TypeField("type")
public interface JavaClassMetaFacetModel extends VertexFrame {

	@Adjacency(label="javaFacet", direction=Direction.OUT)
	public void setJavaClassFacet(JavaClassModel facet);
	
	@Adjacency(label="javaFacet", direction=Direction.OUT)
	public JavaClassModel getJavaClassFacet();
}
