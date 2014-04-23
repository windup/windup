package org.jboss.windup.graph.model.meta;

import org.jboss.windup.graph.model.resource.ArchiveResource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeField("type") 
@TypeValue("ApplicationReference")
public interface ApplicationReference extends VertexFrame {
    
    @Adjacency(label="archive", direction=Direction.OUT)
    public void setArchive(final ArchiveResource module);

    @Adjacency(label="archive", direction=Direction.OUT)
    public ArchiveResource getArchive();
}
