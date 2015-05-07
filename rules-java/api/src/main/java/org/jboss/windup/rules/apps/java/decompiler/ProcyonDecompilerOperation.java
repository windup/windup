package org.jboss.windup.rules.apps.java.decompiler;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationFailure;
import org.jboss.windup.decompiler.api.DecompilationListener;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.procyon.ProcyonConfiguration;
import org.jboss.windup.decompiler.procyon.ProcyonDecompiler;
import org.jboss.windup.decompiler.util.Filter;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.DirectoryModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.PathModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.graph.service.PathService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.model.WarArchiveModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Decompile the provided archive and place metadata regarding which files were decompiled into the graph.
 */
public class ProcyonDecompilerOperation extends AbstractIterationOperation<ArchiveModel>
{
    private static Logger LOG = Logging.get(ProcyonDecompilerOperation.class);

    private static final String TECH_TAG = "Decompiled Java File";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    public ProcyonDecompilerOperation(String variableName)
    {
        super(variableName);
    }

    /**
     * Let the variable name to be set by the current Iteration.
     */
    public ProcyonDecompilerOperation()
    {
        super();
    }

    @Override
    public void perform(final GraphRewrite event, final EvaluationContext context, final ArchiveModel payload)
    {
        ExecutionStatistics.get().begin("ProcyonDecompilationOperation.perform");
        if (payload.getUnzippedDirectory() != null)
        {
            ProcyonDecompiler decompiler = new ProcyonDecompiler(new ProcyonConfiguration().setIncludeNested(false));
            int totalCores = Runtime.getRuntime().availableProcessors();
            int threads = totalCores;
            if (threads == 0)
            {
                threads = 1;
            }

            LOG.info("Decompiling with " + threads + " threads");

            decompiler.setExecutorService(Executors.newFixedThreadPool(threads), threads);
            String archivePath = ((FileModel) payload).getFullPath();
            File archive = new File(archivePath);
            File outputDir = new File(payload.getUnzippedDirectory().getFullPath());
            if (payload instanceof WarArchiveModel)
            {
                outputDir = outputDir.toPath().resolve("WEB-INF").resolve("classes").toFile();
            }

            Filter<ZipEntry> filter = new ZipEntryPackageFilter(event.getGraphContext());
            try
            {
                AddDecompiledItemsToGraph addDecompiledItemsToGraph = new AddDecompiledItemsToGraph(payload, event.getGraphContext());
                DecompilationResult result = decompiler.decompileArchive(archive, outputDir, filter, addDecompiledItemsToGraph);
                for (DecompilationFailure failure : result.getFailures())
                {
                    LOG.log(Level.WARNING, "Failed to decompile.", failure);
                }
                decompiler.close();
            }
            catch (final DecompilationException exc)
            {
                throw new WindupException("Error decompiling archive " + archivePath + " due to: " + exc.getMessage(),
                            exc);
            }
        }
        ExecutionStatistics.get().end("ProcyonDecompilationOperation.perform");
    }

    /**
     * This listens for decompiled files and writes the results to disk in a background thread.
     *
     * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
     *
     */
    private class AddDecompiledItemsToGraph implements DecompilationListener
    {
        private final ExecutorService executorService = Executors.newSingleThreadExecutor();
        private final GraphContext context;
        private final Object archiveModelID;
        private final PathService fileService;
        private final AtomicInteger atomicInteger = new AtomicInteger(0);

        private AddDecompiledItemsToGraph(ArchiveModel archiveModel, GraphContext context)
        {
            this.context = context;
            this.archiveModelID = archiveModel.asVertex().getId();
            this.fileService = new PathService(context);
        }

        @Override
        public void decompilationProcessComplete()
        {
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    LOG.info("Performing final commit for archive vertex: " + archiveModelID);
                    context.getGraph().getBaseGraph().commit();
                }
            });
            executorService.shutdown();
            try
            {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Thread pool failed to complete.");
            }
        }

        @Override
        public synchronized void fileDecompiled(final String inputPath, final String decompiledOutputFile)
        {
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    ArchiveService archiveService = new ArchiveService(context);
                    ArchiveModel archiveModel = archiveService.getById(archiveModelID);
                    // original source is a path inside the archive... split it up by separator, and append
                    // it to the unzipped directory
                    String[] classFilePathTokens = inputPath.split("\\\\|/");

                    Path classFilePath = Paths.get(archiveModel.getUnzippedDirectory().getFullPath());
                    for (String pathToken : classFilePathTokens)
                    {
                        classFilePath = classFilePath.resolve(pathToken);
                    }

                    FileModel decompiledFileModel = fileService.getUniqueByProperty(PathModel.FULL_PATH,
                                decompiledOutputFile).asFileModel();

                    if (decompiledFileModel == null)
                    {
                        DirectoryModel parentDirModel = fileService.findByPath(Paths.get(decompiledOutputFile)
                                    .getParent()
                                    .toString()).asDirectoryModel();

                        // make sure parent files already exist
                        // (it can happen that it does not if procyon puts the decompiled .java file in an unexpected
                        // place, for example in the case
                        // of war files)
                        if (parentDirModel == null)
                        {
                            List<Path> lineage = new LinkedList<>();
                            Path parentPath = Paths.get(decompiledOutputFile).getParent();
                            DirectoryModel existingParentDM = parentDirModel;
                            while (existingParentDM == null)
                            {
                                lineage.add(0, parentPath);
                                parentPath = parentPath.getParent();
                                existingParentDM = fileService.findByPath(parentPath.toString()).asDirectoryModel();
                            }

                            DirectoryModel currentParent = existingParentDM;
                            for (Path p : lineage)
                            {
                                currentParent = fileService.createByFilePath(currentParent, p.toString()).asDirectoryModel();
                            }
                            parentDirModel = currentParent;
                        }

                        decompiledFileModel = fileService.createByFilePath(parentDirModel, decompiledOutputFile).asFileModel();
                        decompiledFileModel.setParentArchive(archiveModel);
                    }
                    ProjectModel projectModel = archiveModel.getProjectModel();
                    decompiledFileModel.setProjectModel(projectModel);
                    projectModel.addFileModel(decompiledFileModel);

                    if (decompiledOutputFile.endsWith(".java"))
                    {
                        if (!(decompiledFileModel instanceof JavaSourceFileModel))
                        {
                            decompiledFileModel = new GraphService<JavaSourceFileModel>(context, JavaSourceFileModel.class)
                                        .addTypeToModel(decompiledFileModel);
                        }
                        JavaSourceFileModel decompiledSourceFileModel = (JavaSourceFileModel) decompiledFileModel;
                        TechnologyTagService techTagService = new TechnologyTagService(context);
                        techTagService.addTagToFileModel(decompiledSourceFileModel, TECH_TAG, TECH_TAG_LEVEL);

                        FileModel classFileModel = fileService.getUniqueByProperty(
                                    PathModel.FULL_PATH, classFilePath.toAbsolutePath().toString()).asFileModel();
                        if (classFileModel != null && classFileModel instanceof JavaClassFileModel)
                        {
                            JavaClassFileModel classModel = (JavaClassFileModel) classFileModel;
                            classModel.getJavaClass().setDecompiledSource(decompiledSourceFileModel);
                            decompiledSourceFileModel.setPackageName(classModel.getPackageName());

                            // Set the root path of this source file (if possible). Procyon should always be placing the file
                            // into a location that is appropriate for the package name, so this should always yield
                            // a non-null root path.
                            Path rootSourcePath = PathUtil.getRootFolderForSource(decompiledSourceFileModel.asFile().toPath(),
                                        classModel.getPackageName());
                            if (rootSourcePath != null)
                            {
                                FileModel rootSourceFileModel = fileService.createByPath(rootSourcePath.toString()).asFileModel();
                                decompiledSourceFileModel.setRootSourceFolder(rootSourceFileModel);
                            }
                            if (classModel.getJavaClass() != null)
                                decompiledSourceFileModel.addJavaClass(classModel.getJavaClass());
                        }
                        else
                        {
                            throw new WindupException(
                                        "Failed to find original JavaClassFileModel for decompiled Java file: "
                                                    + decompiledOutputFile + " at: " + classFilePath.toString());
                        }
                    }
                    archiveModel.addDecompiledFileModel(decompiledFileModel);
                    if (atomicInteger.incrementAndGet() % 100 == 0)
                    {
                        LOG.info("Performing periodic commit (" + atomicInteger.get() + ") for archive vertex: " + archiveModelID);
                        context.getGraph().getBaseGraph().commit();
                    }
                }
            });

        }

        @Override
        public String toString()
        {
            return "DecompileWithProcyon";
        }
    }
}