package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents an archive within the input application.
 */
@TypeValue(ArchiveModel.TYPE)
public interface ArchiveModel extends FileModel
{
    String TYPE = "ArchiveModel:";
    String ARCHIVE_NAME = TYPE + "archiveName";
    String DECOMPILED_FILES = "decompiledFiles";
    String UNZIPPED_DIRECTORY = "unzippedDirectory";

    /**
     * Contains the parent archive.
     */
    @Adjacency(label = "parentArchive", direction = Direction.IN)
    ArchiveModel getParentArchive();

    /**
     * Contains the parent archive.
     */
    @Adjacency(label = "parentArchive", direction = Direction.IN)
    void setParentArchive(ArchiveModel resource);

    /**
     * Contains the name of this archive (filename).
     */
    @Property(ARCHIVE_NAME)
    String getArchiveName();

    /**
     * Contains the name of this archive (filename).
     */
    @Property(ARCHIVE_NAME)
    void setArchiveName(String archiveName);

    /**
     * Contains the children of this archive.
     */
    @Adjacency(label = "childArchive", direction = Direction.OUT)
    Iterable<ArchiveModel> getChildrenArchive();

    /**
     * Contains the children of this archive.
     */
    @Adjacency(label = "childArchive", direction = Direction.OUT)
    void addChildArchive(final ArchiveModel resource);

    /**
     * Contains the directory to which this archive has been unzipped. It will be null if the archive has not been unzipped.
     */
    @Adjacency(label = UNZIPPED_DIRECTORY, direction = Direction.OUT)
    void setUnzippedDirectory(FileModel fileResourceModel);

    /**
     * Contains the directory to which this archive has been unzipped. It will be null if the archive has not been unzipped.
     */
    @Adjacency(label = UNZIPPED_DIRECTORY, direction = Direction.OUT)
    FileModel getUnzippedDirectory();

    /**
     * Contains a list of all files contained within this archive.
     */
    @Adjacency(label = FileModel.ARCHIVE_FILES, direction = Direction.OUT)
    Iterable<FileModel> getContainedFileModels();

    /**
     * Contains a list of all files contained within this archive.
     */
    @Adjacency(label = FileModel.ARCHIVE_FILES, direction = Direction.OUT)
    void addContainedFileModel(FileModel archiveFile);

    /**
     * Contains a list of all decompiled files associated with this archive.
     */
    @Adjacency(label = DECOMPILED_FILES, direction = Direction.OUT)
    Iterable<FileModel> getDecompiledFileModels();

    /**
     * Contains a list of all decompiled files associated with this archive.
     */
    @Adjacency(label = DECOMPILED_FILES, direction = Direction.OUT)
    void addDecompiledFileModel(FileModel archiveFile);

    /**
     * Contains a link to the organization which produced this archive.
     */
    @Adjacency(label = OrganizationModel.ARCHIVE_MODEL, direction = Direction.IN)
    Iterable<OrganizationModel> getOrganizationModels();
}
