package org.jboss.windup.graph.model.resource.facet.xml;

import org.jboss.windup.graph.model.resource.facet.XmlFacet;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

@TypeField("type")
public interface XmlMetaFacet {

	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public void setJavaClassFacet(XmlFacet facet);
	
	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public XmlFacet getJavaClassFacet();
}
