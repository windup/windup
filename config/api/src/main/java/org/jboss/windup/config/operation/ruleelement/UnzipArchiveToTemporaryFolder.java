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
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.ApplicationArchiveModel;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnzipArchiveToTemporaryFolder extends AbstractIterationOperator<ArchiveModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(UnzipArchiveToTemporaryFolder.class);

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
        if (payload instanceof FileModel)
        {
            FileModel fileResourceModel = (FileModel) payload;
            File zipFile = fileResourceModel.asFile();

            if (!zipFile.isFile())
            {
                throw new WindupException("Input file \"" + zipFile.getAbsolutePath() + "\" does not exist.");
            }

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

            unzipToTempDirectory(event, windupTempUnzippedArchiveFolder, zipFile, payload);
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
        LOG.info("Unzipping " + inputZipFile.getPath() + " to " + appArchiveFolder.toString());
        try
        {
            ZipUtil.unzipToFolder(inputZipFile, appArchiveFolder.toFile());
        }
        catch (Throwable e)
        {
            throw new WindupException("Unzipping archive: " + inputZipFile.getAbsolutePath() + " failed due to: "
                        + e.getMessage(), e);
        }

        // add a folder reference for this application
        ApplicationArchiveModel appRefModel = archiveModel.getApplicationReferenceModel();

        FileModel newFileModel = event.getGraphContext().getFramed()
                    .addVertex(null, FileModel.class);
        newFileModel.setFilePath(appArchiveFolder.toAbsolutePath().toString());
        // mark the path to the archive
        archiveModel.setUnzippedDirectory(newFileModel);

        if (appRefModel != null)
        {
            appRefModel.setUnzippedLocation(newFileModel);
        }

        try
        {
            // scan for subarchives, unzip those, and set their parent objects
            Files.walkFileTree(appArchiveFolder, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    FileModel childFile = event.getGraphContext().getFramed()
                                .addVertex(null, FileModel.class);
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
