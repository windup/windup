package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileModelService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.model.IgnoredFileModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jboss.windup.util.Logging;

public class UnzipArchiveToOutputFolder extends AbstractIterationOperation<ArchiveModel>
{
    private static final String ARCHIVES = "archives";
    private static final Logger LOG = Logging.get(UnzipArchiveToOutputFolder.class);

    public UnzipArchiveToOutputFolder(String variableName)
    {
        super(variableName);
    }

    public UnzipArchiveToOutputFolder()
    {
        super();
    }

    public static UnzipArchiveToOutputFolder unzip(String variableName)
    {
        return new UnzipArchiveToOutputFolder(variableName);
    }

    public static UnzipArchiveToOutputFolder unzip()
    {
        return new UnzipArchiveToOutputFolder();
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
    {
        LOG.info("Unzipping archive: " + payload.toPrettyString());
        File zipFile = payload.asFile();

        if (!zipFile.isFile())
        {
            final String msg = "Input path doesn't point to a file: " + zipFile.getAbsolutePath();
            throw new WindupException(msg);
        }

        // create a temp folder for all archive contents
        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        String windupOutputFolder = cfg.getOutputPath().getFilePath();

        Path windupTempUnzippedArchiveFolder = Paths.get(windupOutputFolder, ARCHIVES);
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

        FileModel newFileModel = fileService.createByFilePath(appArchiveFolder.toString());
        // mark the path to the archive
        archiveModel.setUnzippedDirectory(newFileModel);
        newFileModel.setParentArchive(archiveModel);

        // add all unzipped files, and make sure their parent archive is set
        recurseAndAddFiles(context, tempFolder, fileService, archiveModel, newFileModel);
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
                FileModel parentFileModel)
    {
        File fileReference = parentFileModel.asFile();
        WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(
                    context);
        if (fileReference.isDirectory())
        {
            File[] subFiles = fileReference.listFiles();
            if (subFiles != null)
            {
                for (File subFile : subFiles)
                {
                    FileModel subFileModel = fileService.createByFilePath(parentFileModel, subFile.getAbsolutePath());
                    subFileModel.setParentArchive(archiveModel);
                    //check if this file should not be ignored
                    if(checkIfIgnored(subFile.getName(),windupJavaConfigurationService.getIgnoredFileRegexes())) {
                        GraphService.addTypeToModel(context, subFileModel, IgnoredFileModel.class);
                        LOG.info("File/Directory placed in " + subFile.getAbsolutePath() + " was ignored, because matched some of the ignore regex.");
                        continue;
                    }
                    
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
                        recurseAndAddFiles(context, tempFolder, fileService, archiveModel, subFileModel);
                    }
                }
            }
        }
    }
    
    private boolean checkIfIgnored(String fileName,List<String> regexes) {
        boolean ignored = false;
        if (regexes != null && regexes.size()!=0)
        {
            for (String r : regexes)
            {
                if (Pattern.matches(r, fileName))
                {
                    ignored = true;
                    continue;
                }
            }
        }
        return ignored;
    }

    @Override
    public String toString()
    {
        return "UnzipArchivesToOutputFolder";
    }
}
