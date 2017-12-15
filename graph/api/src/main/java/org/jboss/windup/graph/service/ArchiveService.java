package org.jboss.windup.graph.service;

import java.util.StringTokenizer;

import com.thinkaurelius.titan.core.attribute.Text;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.commons.io.FilenameUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

/**
 * Provides methods for searching, creating, and deleting ArchiveModel Vertices.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class ArchiveService extends GraphService<ArchiveModel>
{
    public ArchiveService(GraphContext context)
    {
        super(context, ArchiveModel.class);
    }

    /**
     * Finds all {@link ArchiveModel}s with the given sha1 hash.
     */
    public Iterable<ArchiveModel> findBySHA1(String sha1)
    {
        GraphTraversal<Vertex, Vertex> query = new GraphTraversal<>(getGraphContext().getGraph());
        query.V();
        query.has(FileModel.SHA1_HASH, sha1);
        query.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ArchiveModel.TYPE);
        return new FramedVertexIterable<>(getGraphContext().getFramed(), query, ArchiveModel.class);
    }

    /**
     * Finds the file at the provided path within the archive.
     * 
     * Eg, getChildFile(ArchiveModel, "/META-INF/MANIFEST.MF") will return a {@link FileModel} if a file named
     * /META-INF/MANIFEST.MF exists within the archive
     * 
     * @return Returns the located {@link FileModel} or null if no file with this path could be located
     */
    public FileModel getChildFile(ArchiveModel archiveModel, String filePath)
    {
        filePath = FilenameUtils.separatorsToUnix(filePath);
        StringTokenizer stk = new StringTokenizer(filePath, "/");

        FileModel currentFileModel = archiveModel;
        while (stk.hasMoreTokens() && currentFileModel != null)
        {
            String pathElement = stk.nextToken();

            currentFileModel = findFileModel(currentFileModel, pathElement);
        }
        return currentFileModel;
    }

    private FileModel findFileModel(FileModel fm, String pathElement)
    {
        FileModel result = null;
        for (FileModel child : fm.getFilesInDirectory())
        {
            if (child.getFileName().equals(pathElement))
            {
                result = child;
                break;
            }
        }
        return result;
    }
}
