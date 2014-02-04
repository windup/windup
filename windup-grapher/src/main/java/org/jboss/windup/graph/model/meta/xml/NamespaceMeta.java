package org.jboss.windup.graph.model.meta.xml;

import java.util.Iterator;

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
	public Iterator<XmlResource> getXmlResources();
	
	@Property("uri")
	public String getURI();
	
	@Property("uri")
	public void setURI(String uri);
	
}
