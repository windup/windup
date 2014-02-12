package org.jboss.windup.graph.model.meta;

import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacet;
import org.jboss.windup.graph.model.resource.XmlResource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("HibernateConfigurationFacet")
public interface HibernateSessionFactory extends VertexFrame {

	@Property("name")
	public String getName();
	
	@Property("name")
	public void setName(String name);
	
	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public void setXmlFacet(XmlResource facet);
	
	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public XmlResource getXmlFacet();
	
	@Adjacency(label="hibernateEntity", direction=Direction.OUT)
	public Iterable<HibernateEntityFacet> getHibernateEntity();

	@Adjacency(label="hibernateEntity", direction=Direction.OUT)
	public void addHibernateEntityReference(HibernateEntityFacet hibernateEntity);

}
