package org.jboss.windup.graph.model.resource.facet.javaclass;

import org.jboss.windup.graph.model.resource.facet.JavaClassFacet;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

@TypeField("type")
public interface JavaClassMetaFacet {

	@Adjacency(label="javaClass", direction=Direction.OUT)
	public void setJavaClassFacet(JavaClassFacet facet);
	
	@Adjacency(label="javaClass", direction=Direction.OUT)
	public JavaClassFacet getJavaClassFacet();
}
