package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ApplicationReference")
public interface ApplicationReferenceModel extends WindupVertexFrame
{

    @Adjacency(label = "archive", direction = Direction.OUT)
    public void setArchive(final ArchiveModel module);

    @Adjacency(label = "archive", direction = Direction.OUT)
    public ArchiveModel getArchive();

    @Adjacency(label = "directory", direction = Direction.OUT)
    public FileModel getFileResourceModel();

    @Adjacency(label = "directory", direction = Direction.OUT)
    public FileModel setFileResourceModel(FileModel model);
}
