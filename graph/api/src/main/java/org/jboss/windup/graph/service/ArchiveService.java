package org.jboss.windup.graph.service;

import java.util.StringTokenizer;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;

/**
 * Provides methods for searching, creating, and deleting ArchiveModel Vertices.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ArchiveService extends GraphService<ArchiveModel>
{
    public ArchiveService(GraphContext context)
    {
        super(context, ArchiveModel.class);
    }

    /**
     * Finds the file at the provided path within the archive.
     * 
     * Eg, getChildFile(ArchiveModel, "/META-INF/MANIFEST.MF") will return a {@link FileModel} if a file named
     * /META-INF/MANIFEST.MF exists within the archive
     * 
     * This function expects filePath to use "/" characters to index within the archive, regardless of the underlying
     * operating system platform being used.
     * 
     * @return Returns the located {@link FileModel} or null if no file with this path could be located
     */
    public FileModel getChildFile(ArchiveModel archiveModel, String filePath)
    {
        StringTokenizer stk = new StringTokenizer(filePath, "/");

        FileModel currentFileModel = archiveModel.getUnzippedDirectory();

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
