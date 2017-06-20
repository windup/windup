package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.exception.WindupStopException;
import org.ocpsoft.rewrite.context.EvaluationContext;



/**
 * This Operation unzips the file pointed to by payload (ArchiveModel) to a temporary folder.
 */
public class UnzipArchiveToOutputFolder extends AbstractIterationOperation<ArchiveModel>
{
    private static final String MALFORMED_ARCHIVE = "Malformed archive";
    private static final String ARCHIVES = "archives";
    private static final String KEY_BAD_ARCHIVES = "unparsableArchives";
    private static final Logger LOG = Logging.get(UnzipArchiveToOutputFolder.class);

    public UnzipArchiveToOutputFolder()
    {
        super();
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

        if (zipFile == null || !zipFile.isFile())
        {
            throw new WindupException("Input path doesn't point to a file: " + (zipFile == null ? "null" : zipFile.getAbsolutePath()));
        }
        final GraphContext graphContext = event.getGraphContext();
        checkCancelled(event);

        // Create a folder for all archive contents.
        Path unzippedArchiveDir = getArchivesDirLocation(graphContext);
        ensureDirIsCreated(unzippedArchiveDir);

        // Collect the malformed archives here.
        Object badArchives = event.getRewriteContext().get(KEY_BAD_ARCHIVES);
        if (null == badArchives)
            event.getRewriteContext().put(KEY_BAD_ARCHIVES, new ArrayList<String>());

        unzipToTempDirectory(event, context, unzippedArchiveDir, zipFile, payload, false);
    }

    private void checkCancelled(GraphRewrite event)
    {
        if (event.shouldWindupStop())
        {
            throw new WindupStopException("Request to stop detected during unzipping of archives");
        }
    }

    public static Path getArchivesDirLocation(final GraphContext graphContext)
    {
        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(graphContext);
        String windupOutputFolder = cfg.getOutputPath().getFilePath();
        return Paths.get(windupOutputFolder, ARCHIVES);
    }


    private void unzipToTempDirectory(final GraphRewrite event, EvaluationContext context,
                final Path tempFolder, final File inputZipFile,
                final ArchiveModel archiveModel, boolean subArchivesOnly)
    {
        checkCancelled(event);

        final FileService fileService = new FileService(event.getGraphContext());

        // Setup a temp folder for the archive
        String appArchiveName = archiveModel.getArchiveName();
        if (null == appArchiveName)
            throw new IllegalStateException("Archive model doesn't have an archiveName: " + archiveModel.getFilePath());

        final Path appArchiveFolder = getNonexistentDirForAppArchive(tempFolder, appArchiveName);
        ensureDirIsCreated(appArchiveFolder);

        // Unzip to the temp folder.
        LOG.info("Unzipping " + inputZipFile.getPath() + " to " + appArchiveFolder.toString());
        try
        {
            ZipUtil.unzipToFolder(inputZipFile, appArchiveFolder.toFile());
        }
        catch (Throwable e)
        {
            // only mark the canonical archive, as we only need to add this classification once
            ArchiveModel canonicalArchive = archiveModel;
            if (canonicalArchive instanceof DuplicateArchiveModel)
                canonicalArchive = ((DuplicateArchiveModel)canonicalArchive).getCanonicalArchive();

            ClassificationService classificationService = new ClassificationService(event.getGraphContext());

            // Collect the path so we can create an aggregated Markdown description. This will get regenerated each time, that's fine.
            List<String> badArchives = (List<String>) event.getRewriteContext().get(KEY_BAD_ARCHIVES);
            badArchives.add(inputZipFile.getPath());
            String badArchivesString = badArchives.stream().map(s -> " * `" + s + "`\n").collect(Collectors.joining());

            classificationService.attachClassification(event, context, canonicalArchive,
                    MALFORMED_ARCHIVE, "Cannot unzip these file(s): \n\n" + badArchivesString);
            archiveModel.setParseError("Cannot unzip the file: " + e.getMessage());
            LOG.warning("Cannot unzip the file " + inputZipFile.getPath() + " to " + appArchiveFolder.toString()
                        + ". The ArchiveModel was classified as malformed.");
            return;
        }

        // mark the path to the archive
        archiveModel.setUnzippedDirectory(appArchiveFolder.toString());

        // add all unzipped files, and make sure their parent archive is set
        recurseAndAddFiles(event, context, tempFolder, fileService, archiveModel, archiveModel, subArchivesOnly);
    }

    /**
     * Recurses the given folder and adds references to these files to the graph as FileModels.
     *
     * We don't set the parent file model in the case of the initial children, as the direct parent is really the archive itself. For example for file
     * "root.zip/pom.xml" - the parent for pom.xml is root.zip, not the directory temporary directory that happens to hold it.
     */
    private void recurseAndAddFiles(GraphRewrite event, EvaluationContext context,
                Path tempFolder,
                FileService fileService, ArchiveModel archiveModel,
                FileModel parentFileModel, boolean subArchivesOnly)
    {
        checkCancelled(event);

        int numberAdded = 0;

        FileFilter filter = TrueFileFilter.TRUE;
        if (archiveModel instanceof IdentifiedArchiveModel)
        {
            filter = new IdentifiedArchiveFileFilter(archiveModel);
        }

        File fileReference;
        if (parentFileModel instanceof ArchiveModel)
            fileReference = new File(((ArchiveModel) parentFileModel).getUnzippedDirectory());
        else
            fileReference = parentFileModel.asFile();

        WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(event.getGraphContext());
        File[] subFiles = fileReference.listFiles();
        if (subFiles == null)
            return;

        for (File subFile : subFiles)
        {
            if (!filter.accept(subFile))
                continue;

            if (subArchivesOnly && !ZipUtil.endsWithZipExtension(subFile.getAbsolutePath()))
                continue;

            FileModel subFileModel = fileService.createByFilePath(parentFileModel, subFile.getAbsolutePath());

            // check if this file should be ignored
            if (checkIfIgnored(event, subFileModel, windupJavaConfigurationService.getIgnoredFileRegexes()))
                continue;

            numberAdded++;
            if (numberAdded % 250 == 0)
                event.getGraphContext().getGraph().getBaseGraph().commit();

            if (subFile.isFile() && ZipUtil.endsWithZipExtension(subFileModel.getFilePath()))
            {
                File newZipFile = subFileModel.asFile();
                ArchiveModel newArchiveModel = GraphService.addTypeToModel(event.getGraphContext(), subFileModel, ArchiveModel.class);
                newArchiveModel.setParentArchive(archiveModel);
                newArchiveModel.setArchiveName(newZipFile.getName());

                /*
                 * New archive must be reloaded in case the archive should be ignored
                 */
                newArchiveModel = GraphService.refresh(event.getGraphContext(), newArchiveModel);

                ArchiveModel canonicalArchiveModel = null;
                for (FileModel otherMatches : fileService.findAllByProperty(FileModel.SHA1_HASH, newArchiveModel.getSHA1Hash()))
                {
                    if (otherMatches instanceof ArchiveModel && !otherMatches.equals(newArchiveModel) && !(otherMatches instanceof DuplicateArchiveModel))
                    {
                        canonicalArchiveModel = (ArchiveModel)otherMatches;
                        break;
                    }
                }

                if (canonicalArchiveModel != null)
                {
                    // handle as duplicate
                    DuplicateArchiveModel duplicateArchive = GraphService.addTypeToModel(event.getGraphContext(), newArchiveModel, DuplicateArchiveModel.class);
                    duplicateArchive.setCanonicalArchive(canonicalArchiveModel);

                    // create dupes for child archives
                    unzipToTempDirectory(event, context, tempFolder, newZipFile, duplicateArchive, true);
                } else
                {
                    unzipToTempDirectory(event, context, tempFolder, newZipFile, newArchiveModel, false);
                }
            } else if (subFile.isDirectory())
            {
                recurseAndAddFiles(event, context, tempFolder, fileService, archiveModel, subFileModel, false);
            }
        }
    }

    /**
     * Checks if the {@link FileModel#getFilePath()} + {@link FileModel#getFileName()} is ignored by any of the specified regular expressions.
     */
    private boolean checkIfIgnored(final GraphRewrite event, FileModel file, List<String> patterns)
    {
        boolean ignored = false;
        if (patterns != null && !patterns.isEmpty())
        {
            for (String pattern : patterns)
            {
                if (file.getFilePath().matches(pattern))
                {
                    IgnoredFileModel ignoredFileModel = GraphService.addTypeToModel(event.getGraphContext(), file, IgnoredFileModel.class);
                    ignoredFileModel.setIgnoredRegex(pattern);
                    LOG.info("File/Directory placed in " + file.getFilePath() + " was ignored, because matched [" + pattern + "].");
                    ignored = true;
                    break;
                }
            }
        }

        return ignored;
    }



    private static Path getNonexistentDirForAppArchive(Path tempFolder, String appArchiveName)
    {
        Path appArchiveFolder = Paths.get(tempFolder.toString(), appArchiveName);

        int fileIdx = 1;
        // If it is already created, try another folder name.
        while (Files.exists(appArchiveFolder))
        {
            appArchiveFolder = Paths.get(tempFolder.toString(), appArchiveName + "." + fileIdx);
            fileIdx++;
        }
        return appArchiveFolder;
    }

    private static void ensureDirIsCreated(Path windupTempUnzippedArchiveFolder) throws WindupException
    {
        if (!Files.isDirectory(windupTempUnzippedArchiveFolder))
        {
            try
            {
                Files.createDirectories(windupTempUnzippedArchiveFolder);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to create temporary folder for archives: " + windupTempUnzippedArchiveFolder
                        + "\n\tdue to: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public String toString()
    {
        return UnzipArchiveToOutputFolder.class.getSimpleName();
    }
}
