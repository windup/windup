package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.graph.model.meta.Meta;
import org.jboss.windup.graph.model.resource.XmlResource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("NamespaceMeta")
public interface NamespaceMeta extends Meta {

	@Adjacency(label="namespace", direction=Direction.IN)
	public void addXmlResource(XmlResource facet);

	@Adjacency(label="namespace", direction=Direction.IN)
	public Iterable<XmlResource> getXmlResources();
	
	@Property("namespaceURI")
	public String getURI();
	
	@Property("namespaceURI")
	public void setURI(String uri);
	

	@Property("schemaLocation")
	public String getSchemaLocation();
	
	@Property("schemaLocation")
	public void setSchemaLocation(String schemaLocation);
	
}
