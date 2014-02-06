package org.jboss.windup.graph.model.resource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ArchiveEntryResource")
public interface ArchiveEntryResource extends Resource {

	@Property("archiveEntry")
	public String getArchiveEntry();

	@Property("archiveEntry")
	public void setArchiveEntry(String archiveEntry);
	
	@Adjacency(label="child", direction=Direction.IN)
	public Archive getArchive();
	
	@Adjacency(label="child", direction=Direction.IN)
	public void setArchive(Archive archive);
}
