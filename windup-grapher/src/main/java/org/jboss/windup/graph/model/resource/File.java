package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeField("type") 
@TypeValue("FileResource")
public interface File extends VertexFrame {
	@Property("fileName")
	public String getFileName();
	
	@Property("fileName")
	public void setFileName(String fileName);
}
