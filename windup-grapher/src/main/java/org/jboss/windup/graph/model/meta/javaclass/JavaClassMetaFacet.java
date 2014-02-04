package org.jboss.windup.graph.model.meta.javaclass;

import org.jboss.windup.graph.model.resource.JavaClass;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

@TypeField("type")
public interface JavaClassMetaFacet {

	@Adjacency(label="javaFacet", direction=Direction.OUT)
	public void setJavaClassFacet(JavaClass facet);
	
	@Adjacency(label="javaFacet", direction=Direction.OUT)
	public JavaClass getJavaClassFacet();
}
