package org.jboss.windup.graph.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeField("type") 
@TypeValue("FileResource")
public interface FileResource {
	@Property("fileName")
	public String getFileName();
	
	@Property("fileName")
	public void setFileName(String fileName);
}
