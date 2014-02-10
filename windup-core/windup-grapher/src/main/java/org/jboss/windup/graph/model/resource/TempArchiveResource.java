package org.jboss.windup.graph.model.resource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Indicates an archive that was exploded from another archive.
 * 
 * @author bradsdavis@gmail.com
 *
 */
@TypeValue("TempArchiveResource")
public interface TempArchiveResource extends FileResource {
	
	@Property("archiveEntry")
	public String getArchiveEntry();

	@Property("archiveEntry")
	public void setArchiveEntry(String archiveEntry);
	
	@Adjacency(label="child", direction=Direction.IN)
	public ArchiveResource getArchive();
	
	@Adjacency(label="child", direction=Direction.IN)
	public void setArchive(ArchiveResource archive);
	
}
