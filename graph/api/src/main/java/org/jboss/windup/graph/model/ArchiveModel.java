package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents an archive on disk (for example, a ".zip", ".jar", or ".ear" file).
 */
@TypeValue(ArchiveModel.TYPE)
public interface ArchiveModel extends FileModel
{
    String TYPE = "ArchiveModel:";
    String ARCHIVE_FILES = "archiveFiles";
    String ARCHIVE_NAME = TYPE + "archiveName";
    String PARENT_ARCHIVE = "parentArchive";
    String CHILD_ARCHIVE = "childArchive";
    String UNZIPPED_DIRECTORY = "unzippedDirectory";
    String DECOMPILED_FILES = "decompiledFiles";

    @Adjacency(label = PARENT_ARCHIVE, direction = Direction.IN)
    public ArchiveModel getParentArchive();

    @Adjacency(label = PARENT_ARCHIVE, direction = Direction.IN)
    public void setParentArchive(ArchiveModel resource);

    @Property(ARCHIVE_NAME)
    public String getArchiveName();

    @Property(ARCHIVE_NAME)
    public void setArchiveName(String archiveName);

    @Adjacency(label = CHILD_ARCHIVE, direction = Direction.OUT)
    public Iterable<ArchiveModel> getChildrenArchive();

    @Adjacency(label = CHILD_ARCHIVE, direction = Direction.OUT)
    public void addChildArchive(final ArchiveModel resource);

    @Adjacency(label = CHILD_ARCHIVE, direction = Direction.IN)
    public ArchiveModel getChildArchive();

    @Adjacency(label = UNZIPPED_DIRECTORY, direction = Direction.OUT)
    public void setUnzippedDirectory(ResourceModel fileResourceModel);

    @Adjacency(label = UNZIPPED_DIRECTORY, direction = Direction.OUT)
    public ResourceModel getUnzippedDirectory();

    @Adjacency(label = ARCHIVE_FILES, direction = Direction.OUT)
    public Iterable<ResourceModel> getContainedResourceModels();

    @Adjacency(label = ARCHIVE_FILES, direction = Direction.OUT)
    public void addContainedResourceModel(ResourceModel archiveFile);

    @Adjacency(label = DECOMPILED_FILES, direction = Direction.OUT)
    public Iterable<ResourceModel> getDecompiledResourceModels();

    @Adjacency(label = DECOMPILED_FILES, direction = Direction.OUT)
    public void addDecompiledResourceModel(ResourceModel archiveFile);
}
