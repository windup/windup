package org.jboss.windup.config.operation.ruleelement;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.ApplicationReferenceModel;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class UnzipArchiveToTemporaryFolder extends AbstractIterationOperator<ArchiveModel>
{
    public static final String WINDUP_TEMP_ARCHIVE_FOLDER_NAME = "windup_temp_archive";

    public UnzipArchiveToTemporaryFolder(String variableName)
    {
        super(ArchiveModel.class, variableName);
    }

    public static UnzipArchiveToTemporaryFolder unzip(String variableName)
    {
        return new UnzipArchiveToTemporaryFolder(variableName);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
    {
        // create a temp folder for all archive contents
        Path windupTempFolder = event.getWindupTemporaryFolder();
        Path windupTempUnzippedArchiveFolder = Paths.get(windupTempFolder.toString(), "archives");
        if (!Files.isDirectory(windupTempUnzippedArchiveFolder))
        {
            try
            {
                Files.createDirectories(windupTempUnzippedArchiveFolder);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to create temporary folder: " + windupTempUnzippedArchiveFolder
                            + " due to: " + e.getMessage(), e);
            }
        }

        if (payload instanceof FileResourceModel)
        {
            FileResourceModel fileResourceModel = (FileResourceModel) payload;
            unzipToTempDirectory(event, windupTempUnzippedArchiveFolder, fileResourceModel.asFile(), payload);
        }
    }

    private void unzipToTempDirectory(final GraphRewrite event, final Path tempFolder,
                final File inputZipFile,
                final ArchiveModel archiveModel)
    {
        // Setup a temp folder for the archive
        String appArchiveName = archiveModel.getArchiveName();
        Path appArchiveFolder = Paths.get(tempFolder.toString(), appArchiveName);

        int fileIdx = 1;
        // if it is already created, try another folder name
        while (Files.exists(appArchiveFolder))
        {
            appArchiveFolder = Paths.get(tempFolder.toString(), appArchiveName + "." + fileIdx);
            fileIdx++;
        }

        try
        {
            Files.createDirectories(appArchiveFolder);
        }
        catch (IOException e)
        {
            throw new WindupException("Could not create a temporary directory for application \""
                        + appArchiveName + "\" at \"" + appArchiveFolder.toString() + "\" due to: " + e.getMessage(), e);
        }

        // unzip to the temp folder
        try
        {
            ZipUtil.unzipToFolder(inputZipFile, appArchiveFolder.toFile());
        }
        catch (IOException e)
        {
            throw new WindupException("Unzipping archive: " + inputZipFile.getAbsolutePath() + " failed due to: "
                        + e.getMessage(), e);
        }

        // add a folder reference for this application
        ApplicationReferenceModel appRefModel = archiveModel.getApplicationReferenceModel();

        FileResourceModel newFileModel = event.getGraphContext().getFramed()
                    .addVertex(null, FileResourceModel.class);
        newFileModel.setFilePath(appArchiveFolder.toAbsolutePath().toString());
        // mark the path to the archive
        archiveModel.setUnzippedDirectory(newFileModel);

        if (appRefModel != null)
        {
            appRefModel.setFileResourceModel(newFileModel);
        }

        try
        {
            // scan for subarchives, unzip those, and set their parent objects
            Files.walkFileTree(appArchiveFolder, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    FileResourceModel childFile = event.getGraphContext().getFramed()
                                .addVertex(null, FileResourceModel.class);
                    childFile.setFilePath(file.toAbsolutePath().toString());
                    archiveModel.addContainedFileModel(childFile);

                    if (ZipUtil.endsWithZipExtension(file.toAbsolutePath().toString()))
                    {
                        File newZipFile = file.toFile();
                        ArchiveModel newArchiveModel = GraphUtil.addTypeToModel(event.getGraphContext(), childFile,
                                    ArchiveModel.class);
                        newArchiveModel.setParentArchive(archiveModel);
                        newArchiveModel.setArchiveName(newZipFile.getName());
                        archiveModel.addChildArchive(newArchiveModel);
                        unzipToTempDirectory(event, tempFolder, newZipFile, newArchiveModel);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to walk directory tree: " + appArchiveFolder.toString()
                        + " due to: " + e.getMessage(), e);
        }
    }
}
