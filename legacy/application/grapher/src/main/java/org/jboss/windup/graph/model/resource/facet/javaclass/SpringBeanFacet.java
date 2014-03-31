package org.jboss.windup.graph.model.resource.facet.javaclass;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SpringBeanFacet")
public interface SpringBeanFacet extends JavaClassMetaFacet {

	@Label
	@Property("springBeanName")
	public String getSpringBeanName();

	@Property("springBeanName")
	public String setSpringBeanName(String springBeanName);
	
}
