package org.jboss.windup.rules.apps.ejb.model.meta.xml;

import org.jboss.windup.rules.apps.ejb.model.HibernateSessionFactoryModel;
import org.jboss.windup.rules.apps.ejb.model.HibernateEntityModel;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;

@TypeValue("HibernateConfigurationFacet")
public interface HibernateMappingFacetModel extends XmlMetaFacetModel {

	@Label
	@Property("specificationVersion")
	public String getSpecificationVersion();

	@Property("specificationVersion")
	public void setSpecificationVersion(String version);
	
	@Adjacency(label="hibernateEntity", direction=Direction.OUT)
	public HibernateEntityModel getHibernateEntities();

	@Adjacency(label="hibernateEntity", direction=Direction.OUT)
	public void setHibernateEntity(HibernateEntityModel hibernateEntity);
	
	@Adjacency(label="hibernateSessionFactory", direction=Direction.OUT)
	public Iterable<HibernateSessionFactoryModel> getHibernateSessionFactories();

	@Adjacency(label="hibernateSessionFactory", direction=Direction.OUT)
	public void addHibernateSessionFactory(HibernateSessionFactoryModel hibernateSessionFactor);

}
