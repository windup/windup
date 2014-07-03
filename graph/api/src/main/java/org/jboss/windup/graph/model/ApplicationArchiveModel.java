package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ApplicationArchive")
public interface ApplicationArchiveModel extends ApplicationModel
{
    @Adjacency(label = "archive", direction = Direction.OUT)
    public void setOriginalArchive(final ArchiveModel module);

    @Adjacency(label = "archive", direction = Direction.OUT)
    public ArchiveModel getOriginalArchive();

    @Adjacency(label = "directory", direction = Direction.OUT)
    public FileModel getUnzippedLocation();

    @Adjacency(label = "directory", direction = Direction.OUT)
    public FileModel setUnzippedLocation(FileModel model);
}
