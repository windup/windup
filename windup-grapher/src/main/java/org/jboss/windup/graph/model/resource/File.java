package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("FileResource")
public interface File extends Resource {
	
	@Property("filePath")
	public String getFilePath();
	
	@Property("fileName")
	public void setFilePath(String filePath);
}
