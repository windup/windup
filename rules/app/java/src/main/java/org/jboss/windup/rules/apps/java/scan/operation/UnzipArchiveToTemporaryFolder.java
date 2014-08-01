package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.FileModelService;
import org.jboss.windup.graph.model.ApplicationArchiveModel;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnzipArchiveToTemporaryFolder extends AbstractIterationOperation<ArchiveModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(UnzipArchiveToTemporaryFolder.class);

    public static final String WINDUP_TEMP_ARCHIVE_FOLDER_NAME = "windup_temp_archive";

    public UnzipArchiveToTemporaryFolder(String variableName)
    {
        super(ArchiveModel.class, variableName);
    }
    
    public UnzipArchiveToTemporaryFolder()
    {
        super(ArchiveModel.class);
    }

    public static UnzipArchiveToTemporaryFolder unzip(String variableName)
    {
        return new UnzipArchiveToTemporaryFolder(variableName);
    }
    
    public static UnzipArchiveToTemporaryFolder unzip()
    {
        return new UnzipArchiveToTemporaryFolder();
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
    {
        File zipFile = payload.asFile();

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

        unzipToTempDirectory(event.getGraphContext(), windupTempUnzippedArchiveFolder, zipFile, payload);
    }

    private Path getAppArchiveFolder(Path tempFolder, String appArchiveName)
    {
        Path appArchiveFolder = Paths.get(tempFolder.toString(), appArchiveName);

        int fileIdx = 1;
        // if it is already created, try another folder name
        while (Files.exists(appArchiveFolder))
        {
            appArchiveFolder = Paths.get(tempFolder.toString(), appArchiveName + "." + fileIdx);
            fileIdx++;
        }
        return appArchiveFolder;
    }

    private void unzipToTempDirectory(final GraphContext context, final Path tempFolder,
                final File inputZipFile,
                final ArchiveModel archiveModel)
    {
        final FileModelService fileService = new FileModelService(context);

        // Setup a temp folder for the archive
        String appArchiveName = archiveModel.getArchiveName();

        final Path appArchiveFolder = getAppArchiveFolder(tempFolder, appArchiveName);

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

        FileModel newFileModel = fileService.createByFilePath(appArchiveFolder.toString());
        // mark the path to the archive
        archiveModel.setUnzippedDirectory(newFileModel);
        newFileModel.setParentArchive(archiveModel);

        if (appRefModel != null)
        {
            appRefModel.setUnzippedLocation(newFileModel);
        }

        // add all unzipped files, and make sure their parent archive is set
        recurseAndAddFiles(context, tempFolder, fileService, archiveModel, newFileModel, false);
    }

    /**
     * Recurses the given folder and adds references to these files to the graph as FileModels.
     * 
     * We don't set the parent file model in the case of the inital children, as the direct parent is really the archive
     * itself. For example for file "root.zip/pom.xml" - the parent for pom.xml is root.zip, not the directory temporary
     * directory that happens to hold it.
     */
    private void recurseAndAddFiles(GraphContext context, Path tempFolder, FileModelService fileService,
                ArchiveModel archiveModel,
                FileModel parentFileModel, boolean setParentFile)
    {
        File fileReference = parentFileModel.asFile();

        if (fileReference.isDirectory())
        {
            File[] subFiles = fileReference.listFiles();
            if (subFiles != null)
            {
                for (File subFile : subFiles)
                {
                    FileModel subFileModel;
                    if (setParentFile)
                    {
                        subFileModel = fileService.createByFilePath(parentFileModel, subFile.getAbsolutePath());
                    }
                    else
                    {
                        subFileModel = fileService.createByFilePath(subFile.getAbsolutePath());
                    }
                    subFileModel.setParentArchive(archiveModel);

                    if (subFile.isFile() && ZipUtil.endsWithZipExtension(subFileModel.getFilePath()))
                    {
                        File newZipFile = subFileModel.asFile();
                        ArchiveModel newArchiveModel = GraphService.addTypeToModel(context, subFileModel,
                                    ArchiveModel.class);
                        newArchiveModel.setParentArchive(archiveModel);
                        newArchiveModel.setArchiveName(newZipFile.getName());
                        archiveModel.addChildArchive(newArchiveModel);
                        unzipToTempDirectory(context, tempFolder, newZipFile, newArchiveModel);
                    }

                    if (subFile.isDirectory())
                    {
                        recurseAndAddFiles(context, tempFolder, fileService, archiveModel, subFileModel, true);
                    }
                }
            }
        }
    }
}
