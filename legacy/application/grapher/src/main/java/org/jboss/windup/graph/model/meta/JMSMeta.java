package org.jboss.windup.graph.model.meta;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JMSMeta")
public interface JMSMeta extends Meta {

	@Property("name")
	public String getName();
	
	@Property("name")
	public void setName(String name);

	@Property("type")
	public String getType();
	
	@Property("type")
	public void setType(String type);

	@Property("version")
	public String getVersion();
	
	@Property("version")
	public void setVersion(String version);

}
