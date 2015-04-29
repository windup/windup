package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Unzips the archive recursively into the {@link UnzipArchiveToOutputFolder#ARCHIVES} folder in the output directory.
 */
public class UnzipArchiveToOutputFolder extends AbstractIterationOperation<ArchiveModel>
{
    private static final String MALFORMED_ARCHIVE = "Malformed archive";
    private static final String ARCHIVES = "archives";
    private static final Logger LOG = Logging.get(UnzipArchiveToOutputFolder.class);

    /**
     * Constructions an instance that will load the data from a variable with the given name.
     */
    public UnzipArchiveToOutputFolder(String variableName)
    {
        super(variableName);
    }

    /**
     * Constructions an instance with the default configuration.
     */
    public UnzipArchiveToOutputFolder()
    {
        super();
    }

    /**
     * Constructs an instance using the variable with the given name.
     */
    public static UnzipArchiveToOutputFolder unzip(String variableName)
    {
        return new UnzipArchiveToOutputFolder(variableName);
    }

    /**
     * Constructs an instance with the default configuration.
     */
    public static UnzipArchiveToOutputFolder unzip()
    {
        return new UnzipArchiveToOutputFolder();
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
    {
        LOG.info("Unzipping archive: " + payload.toPrettyString());
        File zipFile = payload.asFile();

        if (zipFile == null || !zipFile.isFile())
        {
            throw new WindupException("Input path doesn't point to a file: " + (zipFile == null ? "null" : zipFile.getAbsolutePath()));
        }

        // create a folder for all archive contents
        WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        Path archivesPath = configuration.getOutputPath().asFile().toPath().resolve(ARCHIVES);

        if (!Files.isDirectory(archivesPath))
        {
            try
            {
                Files.createDirectories(archivesPath);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to create temporary folder: " + archivesPath + " due to: " + e.getMessage(), e);
            }
        }

        unzip(event.getGraphContext(), archivesPath, zipFile, payload);
    }

    private Path getArchiveFolderName(Path tempFolder, String appArchiveName)
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

    private boolean containsZipFiles(ZipFile zipFile)
    {
        for (ZipEntry entry : Collections.list(zipFile.entries()))
        {
            if (!entry.isDirectory() && ZipUtil.endsWithZipExtension(entry.getName()))
                return true;
        }
        return false;
    }

    private void unzip(final GraphContext context,
                final Path tempFolder, final File inputZipFile,
                final ArchiveModel archiveModel)
    {
        final FileService fileService = new FileService(context);

        // Setup a temp folder for the archive
        String appArchiveName = archiveModel.getArchiveName();
        if (null == appArchiveName)
            throw new IllegalStateException("Archive model doesn't have an archiveName: " + archiveModel.getFilePath());

        final Path appArchiveFolder = getArchiveFolderName(tempFolder, appArchiveName);

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
        LOG.info("Unzipping " + inputZipFile.getPath() + " to "
                    + appArchiveFolder.toString());
        try (ZipFile zipFile = new ZipFile(inputZipFile))
        {
            if (containsZipFiles(zipFile))
                ZipUtil.unzipToFolder(inputZipFile, appArchiveFolder.toFile());
            else
                addZipEntriesToGraph(context, archiveModel, zipFile);
        }
        catch (Throwable e)
        {
            ClassificationService classificationService = new ClassificationService(context);
            classificationService.attachClassification(archiveModel, MALFORMED_ARCHIVE, "Cannot unzip the file");
            LOG.warning("Cannot unzip the file " + inputZipFile.getPath() + " to " + appArchiveFolder.toString()
                        + ". The ArchiveModel was classified as malformed.");
            return;
        }

        FileModel newResourceModel = fileService.createByFilePath(appArchiveFolder.toString());
        // mark the path to the archive even if we didn't actually unzip the file. This might be used if we later need
        // to unzip an individual file from the archive for some reason.
        archiveModel.setUnzippedDirectory(newResourceModel);
        newResourceModel.setParentArchive(archiveModel);

        // add all unzipped files, and make sure their parent archive is set
        recurseAndAddFiles(context, tempFolder, fileService, archiveModel, newResourceModel);
    }

    private void addZipEntriesToGraph(GraphContext context, ArchiveModel archiveModel, ZipFile zipFile)
    {


        for (ZipEntry entry : Collections.list(zipFile.entries()))
        {


        }
    }

    /**
     * Recurses the given folder and adds references to these files to the graph as ResourceModels.
     *
     * We don't set the parent file model in the case of the inital children, as the direct parent is really the archive itself. For example for file
     * "root.zip/pom.xml" - the parent for pom.xml is root.zip, not the directory temporary directory that happens to hold it.
     */
    private void recurseAndAddFiles(GraphContext context, Path tempFolder,
                FileService fileService, ArchiveModel archiveModel,
                FileModel parentResourceModel)
    {
        File fileReference = parentResourceModel.asFile();
        WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(context);
        if (fileReference.isDirectory())
        {
            File[] subFiles = fileReference.listFiles();
            if (subFiles != null)
            {
                for (File subFile : subFiles)
                {
                    FileModel subFileModel = fileService.createByFilePath(parentResourceModel, subFile.getAbsolutePath());
                    subFileModel.setParentArchive(archiveModel);

                    // check if this file should be ignored
                    if (checkIfIgnored(context, subFileModel, windupJavaConfigurationService.getIgnoredFileRegexes()))
                    {
                        continue;
                    }

                    if (subFile.isFile() && ZipUtil.endsWithZipExtension(subFileModel.getFilePath()))
                    {
                        File newZipFile = subFileModel.asFile();
                        ArchiveModel newArchiveModel = GraphService
                                    .addTypeToModel(context, subFileModel,
                                                ArchiveModel.class);
                        newArchiveModel.setParentArchive(archiveModel);
                        newArchiveModel.setArchiveName(newZipFile.getName());
                        archiveModel.addChildArchive(newArchiveModel);

                        /*
                         * New archive must be reloaded in case the archive should be ignored
                         */
                        newArchiveModel = GraphService.refresh(context, newArchiveModel);
                        if (!(newArchiveModel instanceof IgnoredArchiveModel))
                            unzip(context, tempFolder, newZipFile,
                                        newArchiveModel);

                    }

                    if (subFile.isDirectory())
                    {
                        recurseAndAddFiles(context, tempFolder, fileService, archiveModel, subFileModel);
                    }
                }
            }
        }
    }

    /**
     * Checks if the {@link ResourceModel#getFilePath()} + {@link ResourceModel#getFileName()} is ignored by any of the specified regular expressions.
     */
    private boolean checkIfIgnored(final GraphContext context, ResourceModel file, List<String> patterns)
    {
        boolean ignored = false;
        if (patterns != null && patterns.size() != 0)
        {
            for (String pattern : patterns)
            {
                if (file.getFilePath().matches(pattern))
                {
                    IgnoredFileModel ignoredResourceModel = GraphService.addTypeToModel(context, file, IgnoredFileModel.class);
                    ignoredResourceModel.setIgnoredRegex(pattern);
                    LOG.info("File/Directory placed in " + file.getFilePath() + " was ignored, because matched [" + pattern + "].");
                    ignored = true;
                    break;
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
