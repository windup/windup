package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("FileResource")
public interface FileResource extends Resource {
	
	@Property("filePath")
	public String getFilePath();
	
	@Property("filePath")
	public void setFilePath(String filePath);
	
}
