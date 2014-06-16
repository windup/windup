package org.jboss.windup.rules.apps.ejb.model;

import org.jboss.windup.rules.apps.java.scan.model.JavaClassMetaModel;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacetModel;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SpringBeanFacet")
public interface SpringBeanFacetModel extends JavaClassMetaModel {

	@Label
	@Property("springBeanName")
	public String getSpringBeanName();

	@Property("springBeanName")
	public String setSpringBeanName(String springBeanName);


	@Adjacency(label="springConfiguration", direction=Direction.IN)
	public SpringConfigurationFacetModel getSpringConfiguration();

	@Adjacency(label="springConfiguration", direction=Direction.IN)
	public void setSpringConfiguration(SpringConfigurationFacetModel springConfiguration);

}
