package org.jboss.windup.graph.service;

import java.util.StringTokenizer;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

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
     * Eg, getChildFile(ArchiveModel, "/META-INF/MANIFEST.MF") will return a {@link ResourceModel} if a file named
     * /META-INF/MANIFEST.MF exists within the archive
     * 
     * This function expects filePath to use "/" characters to index within the archive, regardless of the underlying
     * operating system platform being used.
     * 
     * @return Returns the located {@link ResourceModel} or null if no file with this path could be located
     */
    public ResourceModel getChildFile(ArchiveModel archiveModel, String filePath)
    {
        StringTokenizer stk = new StringTokenizer(filePath, "/");

        ResourceModel currentResourceModel = archiveModel.getUnzippedDirectory();

        while (stk.hasMoreTokens() && currentResourceModel != null)
        {
            String pathElement = stk.nextToken();

            currentResourceModel = findResourceModel(currentResourceModel, pathElement);
        }
        return currentResourceModel;
    }

    private ResourceModel findResourceModel(ResourceModel fm, String pathElement)
    {
        ResourceModel result = null;
        for (ResourceModel child : fm.getFilesInDirectory())
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
