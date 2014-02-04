package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.graph.model.resource.XmlResource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

@TypeField("type")
public interface XmlMetaFacet {

	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public void setJavaClassFacet(XmlResource facet);
	
	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public XmlResource getJavaClassFacet();
}
