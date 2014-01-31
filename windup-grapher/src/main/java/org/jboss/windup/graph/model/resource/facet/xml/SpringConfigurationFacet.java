package org.jboss.windup.graph.model.resource.facet.xml;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SpringConfigurationFacet")
public interface SpringConfigurationFacet extends XmlMetaFacet {

	@Label
	@Property("specificationVersion")
	public double getSpecificationVersion();

	@Property("specificationVersion")
	public void getSpecificationVersion(double version);
	
}
