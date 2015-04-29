package org.jboss.windup.graph.service;

import java.util.zip.ZipEntry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.model.resource.ZipEntryModel;

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
        filePath = FilenameUtils.separatorsToUnix(filePath);


        for (ResourceModel resourceModel : archiveModel.getContainedResourceModels())
        {
            String pathWithinProject = StringUtils.removeStart(resourceModel.getPrettyPathWithinProject(), archiveModel.getArchiveName());
            pathWithinProject = StringUtils.removeStart(pathWithinProject, "/");
            if (pathWithinProject.equals(filePath))
                return resourceModel;
        }

        return null;
    }

    public static String getRelativePath(ArchiveModel archiveModel, ZipEntryModel entry)
    {
        return StringUtils.removeStart(entry.getFilePath(), getPrefix(archiveModel));
    }

    private String getFilePathForZipEntry(ArchiveModel archiveModel, ZipEntry entry)
    {
        String entryName = FilenameUtils.separatorsToUnix(entry.getName());
        entryName = StringUtils.removeEnd(entryName, "/");
        return getPrefix(archiveModel) + entryName;
    }

    private static String getPrefix(ArchiveModel archiveModel)
    {
        return "zip://" + archiveModel.getArchiveName() + "." + archiveModel.asVertex().getId() + "/";
    }

    public ZipEntryModel createEntry(ArchiveModel archiveModel, ZipEntry entry)
    {
        GraphService<ZipEntryModel> service = new GraphService<>(getGraphContext(), ZipEntryModel.class);

        String entryName = FilenameUtils.separatorsToUnix(entry.getName());
        entryName = StringUtils.removeEnd(entryName, "/");
        String filePath = getFilePathForZipEntry(archiveModel, entry);

        ZipEntryModel entryModel = service.create();
        entryModel.setFileName(FilenameUtils.getName(entryName));
        entryModel.setDirectory(entry.isDirectory());
        entryModel.setParentArchive(archiveModel);
        entryModel.setFilePath(filePath);
        entryModel.setLength(entry.getSize());
        return entryModel;
    }
}
