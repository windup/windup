package org.jboss.windup.graph.service;

import java.util.StringTokenizer;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.DirectoryModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.PathModel;

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

        DirectoryModel currentDirModel = archiveModel.getUnzippedDirectory();

        while (stk.hasMoreTokens() && currentDirModel != null)
        {
            String pathElement = stk.nextToken();
            if(stk.hasMoreTokens())
                currentDirModel = findChildDirectoryModel(currentDirModel, pathElement);
            else
                return findChildFileModel(currentDirModel, pathElement);
        }
        return null;
    }

    /**
     * Finds a file by name in given directory.
     * TODO: Should search by indexed file name. #PERF
     */
    private DirectoryModel findChildDirectoryModel(DirectoryModel fm, String pathElement)
    {
        for (PathModel child : fm.getPathsInDirectory())
        {
            if (child.getFileName().equals(pathElement)){
                if (child instanceof DirectoryModel)
                    return (DirectoryModel) child;
                break;
            }
        }
        return null;
    }

    /**
     * Finds a file by name in given directory.
     * TODO: Should search by indexed file name. #PERF
     */
    private FileModel findChildFileModel(DirectoryModel fm, String pathElement)
    {
        for (PathModel child : fm.getPathsInDirectory())
        {
            if (child.getFileName().equals(pathElement)){
                if (child instanceof FileModel)
                    return (FileModel) child;
                break;
            }
        }
        return null;
    }
}
