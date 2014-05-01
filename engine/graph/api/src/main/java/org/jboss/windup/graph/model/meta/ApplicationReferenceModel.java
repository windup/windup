package org.jboss.windup.graph.model.meta;

import org.jboss.windup.graph.model.resource.ArchiveResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeField("type") 
@TypeValue("ApplicationReference")
public interface ApplicationReferenceModel extends VertexFrame {
    
    @Adjacency(label="archive", direction=Direction.OUT)
    public void setArchive(final ArchiveResourceModel module);

    @Adjacency(label="archive", direction=Direction.OUT)
    public ArchiveResourceModel getArchive();
}
