package org.jboss.windup.graph.model.resource.facet.javaclass;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EJBFacet")
public interface EjbEntityFacet extends JavaClassMetaFacet {

	@Label
	@Property("ejbEntityName")
	public String getEjbEntityName();

	@Property("ejbEntityName")
	public String setEjbEntityName(String ejbEntityName);
	
}
