package org.jboss.windup.graph.model.meta;

import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacetModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("HibernateConfigurationFacet")
public interface HibernateSessionFactoryModel extends VertexFrame {

	@Property("name")
	public String getName();
	
	@Property("name")
	public void setName(String name);
	
	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public void setXmlFacet(XmlResourceModel facet);
	
	@Adjacency(label="xmlFacet", direction=Direction.OUT)
	public XmlResourceModel getXmlFacet();
	
	@Adjacency(label="hibernateEntity", direction=Direction.OUT)
	public Iterable<HibernateEntityFacetModel> getHibernateEntity();

	@Adjacency(label="hibernateEntity", direction=Direction.OUT)
	public void addHibernateEntityReference(HibernateEntityFacetModel hibernateEntity);

}
