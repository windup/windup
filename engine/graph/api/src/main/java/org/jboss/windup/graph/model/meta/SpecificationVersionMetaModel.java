package org.jboss.windup.graph.model.meta;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SpecificationVersion")
public interface SpecificationVersionMetaModel extends BaseMetaModel {

	@Property("specVersion")
	public String getSpecVersion();
	
	@Property("specVersion")
	public void setSpecVersion(String version);

}
