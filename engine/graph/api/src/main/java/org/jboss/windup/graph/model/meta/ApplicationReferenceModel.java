package org.jboss.windup.graph.model.meta;

import org.jboss.windup.graph.model.resource.ArchiveResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ApplicationReference")
public interface ApplicationReferenceModel extends WindupVertexFrame {
    
    @Adjacency(label="archive", direction=Direction.OUT)
    public void setArchive(final ArchiveResourceModel module);

    @Adjacency(label="archive", direction=Direction.OUT)
    public ArchiveResourceModel getArchive();
}
