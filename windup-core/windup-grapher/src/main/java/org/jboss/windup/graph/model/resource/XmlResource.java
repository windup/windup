package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("XmlResource")
public interface XmlResource extends Resource {

	@Adjacency(label="xmlResourceFacet", direction=Direction.IN)
	public Resource getResource();

	@Adjacency(label="xmlResourceFacet", direction=Direction.IN)
	public void setResource(Resource resource);

	@Adjacency(label="doctype", direction=Direction.OUT)
	public void setDoctype(DoctypeMeta doctype);
	
	@Adjacency(label="doctype", direction=Direction.OUT)
	public DoctypeMeta getDoctype();

	@Adjacency(label="namespace", direction=Direction.OUT)
	public void addNamespace(NamespaceMeta namespace);
	
	@Adjacency(label="namespace", direction=Direction.OUT)
	public Iterable<NamespaceMeta> getNamespaces();
	
}
