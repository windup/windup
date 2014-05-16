package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.ArchiveModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ArchiveEntryResource")
public interface ArchiveEntryResourceModel extends ResourceModel
{

    @Property("archiveEntry")
    public String getArchiveEntry();

    @Property("archiveEntry")
    public void setArchiveEntry(String archiveEntry);

    @Adjacency(label = "childArchiveEntry", direction = Direction.IN)
    public ArchiveModel getArchive();

    @Adjacency(label = "childArchiveEntry", direction = Direction.IN)
    public void setArchive(ArchiveModel archive);

}
