package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(ArchiveModel.TYPE)
public interface ArchiveModel extends FileModel
{
    String TYPE = "Archive:";
    String ARCHIVE_NAME = TYPE + "archiveName";
    String DECOMPILED_FILES = "decompiledFiles";
    String UNZIPPED_DIRECTORY = "unzippedDirectory";


    @Adjacency(label = "parentArchive", direction = Direction.IN)
    public ArchiveModel getParentArchive();

    @Adjacency(label = "parentArchive", direction = Direction.IN)
    public void setParentArchive(ArchiveModel resource);

    @Property(ARCHIVE_NAME)
    public String getArchiveName();

    @Property(ARCHIVE_NAME)
    public void setArchiveName(String archiveName);

    @Adjacency(label = "childArchive", direction = Direction.OUT)
    public Iterable<ArchiveModel> getChildrenArchive();

    @Adjacency(label = "childArchive", direction = Direction.OUT)
    public void addChildArchive(final ArchiveModel resource);

    @Adjacency(label = "childArchive", direction = Direction.IN)
    public ArchiveModel getChildArchive();

    @Adjacency(label = UNZIPPED_DIRECTORY, direction = Direction.OUT)
    public void setUnzippedDirectory(FileModel fileResourceModel);

    @Adjacency(label = UNZIPPED_DIRECTORY, direction = Direction.OUT)
    public FileModel getUnzippedDirectory();

    @Adjacency(label = FileModel.ARCHIVE_FILES, direction = Direction.OUT)
    public Iterable<FileModel> getContainedFileModels();

    @Adjacency(label = FileModel.ARCHIVE_FILES, direction = Direction.OUT)
    public void addContainedFileModel(FileModel archiveFile);

    @Adjacency(label = DECOMPILED_FILES, direction = Direction.OUT)
    public Iterable<FileModel> getDecompiledFileModels();

    @Adjacency(label = DECOMPILED_FILES, direction = Direction.OUT)
    public void addDecompiledFileModel(FileModel archiveFile);

    @Adjacency(label = OrganizationModel.ARCHIVE_MODEL, direction = Direction.IN)
    public Iterable<OrganizationModel> getOrganizationModels();

}
