package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.rules.apps.ejb.model.SpringBeanFacetModel;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SpringConfigurationFacet")
public interface SpringConfigurationFacetModel extends XmlMetaFacetModel {

	@Label
	@Property("specificationVersion")
	public String getSpecificationVersion();

	@Property("specificationVersion")
	public void setSpecificationVersion(String version);
	
	@Adjacency(label="springBean", direction=Direction.OUT)
	public Iterable<SpringBeanFacetModel> getSpringBeans();

	@Adjacency(label="springBean", direction=Direction.OUT)
	public void addSpringBeanReference(SpringBeanFacetModel springBean);

}
