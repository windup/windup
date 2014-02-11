package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.graph.model.meta.Meta;
import org.jboss.windup.graph.model.resource.XmlResource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("DoctypeMeta")
public interface DoctypeMeta extends Meta {

	@Adjacency(label="doctype", direction=Direction.IN)
	public void addXmlResource(XmlResource facet);

	@Adjacency(label="doctype", direction=Direction.IN)
	public Iterable<XmlResource> getXmlResources();

	
	@Property("name")
	public String getName();
	
	@Property("name")
	public void setName(String name);
	
	
	@Property("publicId")
	public String getPublicId();
	
	@Property("publicId")
	public void setPublicId(String publicId);
	
	
	@Property("systemId")
	public String getSystemId();

	@Property("systemId")
	public void setSystemId(String systemId);
	
	
	@Property("baseURI")
	public String getBaseURI();

	@Property("baseURI")
	public void setBaseURI(String baseURI);
}
