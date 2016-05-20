package org.jboss.windup.graph.model;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents an archive within the input application.
 */
@TypeValue(ArchiveModel.TYPE)
public interface ArchiveModel extends FileModel
{
    String TYPE = "ArchiveModel:";
    String ARCHIVE_NAME = TYPE + "archiveName";
    String UNZIPPED_DIRECTORY = "unzippedDirectory";
    String PARENT_ARCHIVE = "parentArchive";

    /**
     * Contains the parent archive.
     */
    @Adjacency(label = PARENT_ARCHIVE, direction = Direction.IN)
    ArchiveModel getParentArchive();

    /**
     * Contains the parent archive.
     */
    @Adjacency(label = PARENT_ARCHIVE, direction = Direction.IN)
    void setParentArchive(ArchiveModel archive);

    /**
     * Contains the name of this archive (original filename).
     */
    @Property(ARCHIVE_NAME)
    String getArchiveName();

    /**
     * Contains the name of this archive (original filename).
     */
    @Property(ARCHIVE_NAME)
    void setArchiveName(String archiveName);

    /**
     * Contains the directory to which this archive has been unzipped. It will be null if the archive has not been unzipped.
     */
    @Property(UNZIPPED_DIRECTORY)
    void setUnzippedDirectory(String unzippedPath);

    /**
     * Contains the directory to which this archive has been unzipped. It will be null if the archive has not been unzipped.
     */
    @Property(UNZIPPED_DIRECTORY)
    String getUnzippedDirectory();

    /**
     * Contains a link to the organization which produced this archive.
     */
    @Adjacency(label = OrganizationModel.ARCHIVE_MODEL, direction = Direction.IN)
    Iterable<OrganizationModel> getOrganizationModels();

    /**
     * Gets all files in this archive, including subfiles, but not including subfiles of embedded archives.
     */
    @JavaHandler
    Iterable<FileModel> getAllFiles();

    @Adjacency(label = DuplicateArchiveModel.CANONICAL_ARCHIVE, direction = Direction.IN)
    Iterable<DuplicateArchiveModel> getDuplicateArchives();

    abstract class Impl extends FileModel.Impl implements ArchiveModel, JavaHandlerContext<Vertex>
    {
        @Override
        public Iterable<FileModel> getAllFiles()
        {
            Set<FileModel> results = new LinkedHashSet<>();

            for (FileModel child : getFilesInDirectory())
                addAllFiles(results, child);

            return results;
        }

        private void addAllFiles(Set<FileModel> files, FileModel file)
        {
            files.add(file);

            // don't include children of embedded archives
            if (file instanceof ArchiveModel)
                return;

            for (FileModel child : file.getFilesInDirectory())
                addAllFiles(files, child);
        }
    }
}
