package org.jboss.windup.graph.model.meta;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EnvironmentReference")
public interface EnvironmentReferenceModel extends BaseMetaModel {

	@Property("referenceId")
	public String getReferenceId();

	@Property("referenceId")
	public void setReferenceId(String resourceId);
	
	@Property("name")
	public String getName();
	
	@Property("name")
	public void setName(String name);
	
	@Property("referenceType")
	public String getReferenceType();

	@Property("referenceType")
	public void setReferenceType(String referenceType);
}
