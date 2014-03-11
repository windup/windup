package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.graph.model.meta.HibernateSessionFactory;
import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacet;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("HibernateConfigurationFacet")
public interface HibernateMappingFacet extends XmlMetaFacet {

	@Label
	@Property("specificationVersion")
	public String getSpecificationVersion();

	@Property("specificationVersion")
	public void setSpecificationVersion(String version);
	
	@Adjacency(label="hibernateEntity", direction=Direction.OUT)
	public HibernateEntityFacet getHibernateEntities();

	@Adjacency(label="hibernateEntity", direction=Direction.OUT)
	public void setHibernateEntity(HibernateEntityFacet hibernateEntity);
	
	@Adjacency(label="hibernateSessionFactory", direction=Direction.OUT)
	public Iterable<HibernateSessionFactory> getHibernateSessionFactories();

	@Adjacency(label="hibernateSessionFactory", direction=Direction.OUT)
	public void addHibernateSessionFactory(HibernateSessionFactory hibernateSessionFactor);

}
