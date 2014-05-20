package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileResourceModel;

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
    public FileResourceModel getFileResourceModel();

    @Adjacency(label = "directory", direction = Direction.OUT)
    public FileResourceModel setFileResourceModel(FileResourceModel model);
}
