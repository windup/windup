package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents an Archive such as a JAR, WAR, or EAR file.
 */
@TypeValue(ArchiveModel.TYPE)
public interface ArchiveModel extends FileModel
{
    String TYPE = "ArchiveModel:";
    String ARCHIVE_NAME = TYPE + "archiveName";
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
     * Contains the name of the archive.
     */
    @Property(ARCHIVE_NAME)
    String getArchiveName();

    /**
     * Contains the name of the archive.
     */
    @Property(ARCHIVE_NAME)
    void setArchiveName(String archiveName);

    /**
     * Contains the child archives.
     */
    @Adjacency(label = "childArchive", direction = Direction.OUT)
    Iterable<ArchiveModel> getChildArchives();

    /**
     * Contains the child archives.
     */
    @Adjacency(label = "childArchive", direction = Direction.OUT)
    void addChildArchive(final ArchiveModel resource);

    /**
     * Contains the location that this archive was unzipped to
     */
    @Adjacency(label = UNZIPPED_DIRECTORY, direction = Direction.OUT)
    void setUnzippedDirectory(FileModel fileResourceModel);

    /**
     * Contains the location that this archive was unzipped to
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
     * Contains a pointer to the organization responsible for this archive (eg, Apache).
     */
    @Adjacency(label = OrganizationModel.ARCHIVE_MODEL, direction = Direction.IN)
    Iterable<OrganizationModel> getOrganizationModels();

}
