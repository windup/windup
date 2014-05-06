package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("XmlMetaFacetModel")
public interface XmlMetaFacetModel extends WindupVertexFrame {

	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public void setXmlFacet(XmlResourceModel facet);
	
	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public XmlResourceModel getXmlFacet();
	
	@Property("rootTagName")
	public String getRootTagName();

	@Property("rootTagName")
	public void setRootTagName(String rootTagName);
}
