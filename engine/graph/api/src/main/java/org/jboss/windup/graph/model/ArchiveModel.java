package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ArchiveModel")
public interface ArchiveModel extends WindupVertexFrame
{
    @Adjacency(label = "parentArchive", direction = Direction.IN)
    public ArchiveModel getParentArchive();

    @Adjacency(label = "parentArchive", direction = Direction.IN)
    public void setParentArchive(ArchiveModel resource);

    @Property("archiveName")
    public String getArchiveName();

    @Property("archiveName")
    public void setArchiveName(String archiveName);

    @Adjacency(label = "childArchive", direction = Direction.OUT)
    public Iterable<ArchiveModel> getChildrenArchive();

    @Adjacency(label = "childArchive", direction = Direction.OUT)
    public void addChildArchive(final ArchiveModel resource);

    @Adjacency(label = "childArchive", direction = Direction.IN)
    public ArchiveModel getChildArchive();

    @Adjacency(label = "archive", direction = Direction.IN)
    public ApplicationReferenceModel getApplicationReferenceModel();

    @Adjacency(label = "unzippedDirectory", direction = Direction.OUT)
    public void setUnzippedDirectory(FileResourceModel fileResourceModel);

    @Adjacency(label = "unzippedDirectory", direction = Direction.OUT)
    public FileResourceModel getUnzippedDirectory();

    @Adjacency(label = "archiveFiles", direction = Direction.OUT)
    public Iterable<FileResourceModel> getContainedFileModels();

    @Adjacency(label = "archiveFiles", direction = Direction.OUT)
    public void addContainedFileModel(FileResourceModel archiveFile);
}
