package org.jboss.windup.graph.model.resource;

import java.util.Iterator;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ArchiveResource")
public interface Archive extends File {
	
	@Property("archiveName")
	public String getArchiveName();
	
	@Property("archiveName")
	public void setArchiveName(String archiveName);
	
	@Adjacency(label="child", direction=Direction.OUT)
	public Iterator<Resource> getChildren();
	
	@Adjacency(label="child", direction=Direction.OUT)
	public void addChild(final Resource resource);
	
	@Adjacency(label="child", direction=Direction.IN)
	public Resource getParent();
	
	@Adjacency(label="child", direction=Direction.IN)
	public void setChild(final Resource resource);
	
}
