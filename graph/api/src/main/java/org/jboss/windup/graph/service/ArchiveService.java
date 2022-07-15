package org.jboss.windup.graph.service;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.io.FilenameUtils;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

/**
 * Provides methods for searching, creating, and deleting ArchiveModel Vertices.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ArchiveService extends GraphService<ArchiveModel> {
    public ArchiveService(GraphContext context) {
        super(context, ArchiveModel.class);
    }

    /**
     * Finds all {@link ArchiveModel}s with the given sha1 hash.
     */
    public Iterable<ArchiveModel> findBySHA1(String sha1) {
        List<Vertex> query = getGraphContext().getGraph().traversal()
                .V()
                .property(FileModel.SHA1_HASH, sha1)
                .property(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ArchiveModel.TYPE)
                .toList();

        return new FramedVertexIterable<>(getGraphContext().getFramed(), query, ArchiveModel.class);
    }

    /**
     * Finds the file at the provided path within the archive.
     * <p>
     * Eg, getChildFile(ArchiveModel, "/META-INF/MANIFEST.MF") will return a {@link FileModel} if a file named
     * /META-INF/MANIFEST.MF exists within the archive
     *
     * @return Returns the located {@link FileModel} or null if no file with this path could be located
     */
    public FileModel getChildFile(ArchiveModel archiveModel, String filePath) {
        filePath = FilenameUtils.separatorsToUnix(filePath);
        StringTokenizer stk = new StringTokenizer(filePath, "/");

        FileModel currentFileModel = archiveModel;
        while (stk.hasMoreTokens() && currentFileModel != null) {
            String pathElement = stk.nextToken();

            currentFileModel = findFileModel(currentFileModel, pathElement);
        }
        return currentFileModel;
    }

    private FileModel findFileModel(FileModel fm, String pathElement) {
        FileModel result = null;
        for (FileModel child : fm.getFilesInDirectory()) {
            if (child.getFileName().equals(pathElement)) {
                result = child;
                break;
            }
        }
        return result;
    }
}
