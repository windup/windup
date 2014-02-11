package org.jboss.windup.graph.model.meta.javaclass;

import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacet;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SpringBeanFacet")
public interface SpringBeanFacet extends JavaClassMetaFacet {

	@Label
	@Property("springBeanName")
	public String getSpringBeanName();

	@Property("springBeanName")
	public String setSpringBeanName(String springBeanName);


	@Adjacency(label="springConfiguration", direction=Direction.IN)
	public SpringConfigurationFacet getSpringConfiguration();

	@Adjacency(label="springConfiguration", direction=Direction.IN)
	public void setSpringConfiguration(SpringConfigurationFacet springConfiguration);

}
