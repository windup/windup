package org.jboss.windup.rules.apps.java.decompiler;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.decompiler.api.ClassDecompileRequest;
import org.jboss.windup.decompiler.api.DecompilationListener;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.fernflower.FernflowerDecompiler;
import org.jboss.windup.decompiler.procyon.ProcyonDecompiler;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.model.LineMappingModel;
import org.jboss.windup.rules.apps.java.scan.provider.IndexJavaSourceFilesRuleProvider;
import org.jboss.windup.rules.apps.java.service.LineMappingService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.ProgressEstimate;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.exception.WindupStopException;
import org.jboss.windup.util.threading.WindupExecutors;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FernflowerDecompilerOperation extends AbstractDecompilerOperation
{
    private static final Logger LOG = Logging.get(FernflowerDecompilerOperation.class);

    private static final String TECH_TAG = "Decompiled Java File";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    /**
     * Let the variable name to be set by the current Iteration.
     */
    public FernflowerDecompilerOperation()
    {
        super();
    }

    @Override
    public void perform(final GraphRewrite event, final EvaluationContext context)
    {
        ExecutionStatistics.get().begin("FernflowerDecompilationOperation.perform");
        int threads = WindupExecutors.getDefaultThreadCount();
        LOG.info("Decompiling with " + threads + " threads");

        WindupJavaConfigurationService configurationService = new WindupJavaConfigurationService(event.getGraphContext());
        Iterable<JavaClassFileModel> filesToDecompile = getFilesToDecompile(event.getGraphContext());

        int totalWork = 0;
        List<ClassDecompileRequest> classesToDecompile = new ArrayList<>(10000); // Just a guess as to the average size
        for (JavaClassFileModel classFileModel : filesToDecompile)
        {
            File outputDir = DecompilerUtil.getOutputDirectoryForClass(event.getGraphContext(), classFileModel);
            if (configurationService.shouldScanPackage(classFileModel.getPackageName()))
            {
                classesToDecompile.add(new ClassDecompileRequest(outputDir.toPath(), classFileModel.asFile().toPath(), outputDir.toPath()));
                if (!classFileModel.getFilePath().contains("$"))
                {
                    // it only counts as a work unit if it is not an inner class (as inner classes are grouped together)
                    totalWork++;
                }
            }
        }
        Collections.sort(classesToDecompile, (ClassDecompileRequest o1, ClassDecompileRequest o2)
                -> o1.getOutputDirectory().toAbsolutePath().toString().compareTo(o2.getOutputDirectory().toString()));

        ProgressEstimate progressEstimate = new ProgressEstimate(totalWork);

        AddDecompiledItemsToGraph addDecompiledItemsToGraph = new AddDecompiledItemsToGraph(classesToDecompile, progressEstimate, event);
        FernflowerDecompiler decompiler = new FernflowerDecompiler();
        decompiler.setExecutorService(WindupExecutors.newFixedThreadPool(threads), threads);
        decompiler.decompileClassFiles(classesToDecompile, addDecompiledItemsToGraph);
        decompiler.close();

        ExecutionStatistics.get().end("FernflowerDecompilationOperation.perform");
    }

    /**
     * This listens for decompiled files and writes the results to disk in a background thread.
     *
     * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
     */
    private class AddDecompiledItemsToGraph implements DecompilationListener
    {
        private final ExecutorService executorService = WindupExecutors.newSingleThreadExecutor();
        private final AtomicInteger queueSize = new AtomicInteger(0);

        private final Map<String, ClassDecompileRequest> requestMap;
        private final GraphRewrite event;
        private final ProgressEstimate progressEstimate;
        private final AtomicInteger atomicInteger = new AtomicInteger(0);

        private AddDecompiledItemsToGraph(List<ClassDecompileRequest> requests, ProgressEstimate progressEstimate, GraphRewrite event)
        {
            this.requestMap = new HashMap<>(requests.size());
            for (ClassDecompileRequest request : requests)
                requestMap.put(request.getClassFile().toString(), request);
            this.progressEstimate = progressEstimate;
            this.event = event;
        }

        @Override
        public void decompilationProcessComplete()
        {
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    LOG.info("Performing final commit for decompilation process!");
                    event.getGraphContext().commit();
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
        public void decompilationFailed(List<String> inputPath, String message)
        {
            ClassDecompileRequest request = requestMap.get(inputPath);
            ProcyonDecompiler procyon = new ProcyonDecompiler();

            DecompilationResult result = procyon.decompileClassFile(request.getRootDirectory(), request.getClassFile(), request.getOutputDirectory());
            if (!result.getFailures().isEmpty())
            {
                LOG.warning("Failsafe Procyon decompilation of " + inputPath + " failed!");
                progressEstimate.addWork(1);
            }
            else
            {
                for (Map.Entry<String, String> decompiledFile : result.getDecompiledFiles().entrySet())
                {
                    LOG.info("Failsafe Procyon decompilation of " + inputPath + " successful!");
                    fileDecompiled(Collections.singletonList(decompiledFile.getKey()), decompiledFile.getValue());
                }
            }

        }

        @Override
        public boolean isCancelled()
        {
            return this.event.shouldWindupStop();
        }

        @Override
        public synchronized void fileDecompiled(final List<String> inputPath, final String decompiledOutputFile) {
            fileDecompiled(inputPath, decompiledOutputFile, null);
        }

        @Override
        public synchronized void fileDecompiled(final List<String> inputPath, final String decompiledOutputFile, int[] lineMapping)
        {
            if (event.shouldWindupStop())
            {
                LOG.warning("Request to stop received.");
                throw new WindupStopException("Request to stop received.");
            }

            Runnable saveDecompiledRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    queueSize.decrementAndGet();
                    progressEstimate.addWork(1);
                    if (progressEstimate.getWorked() % 250 == 0)
                    {
                        long remainingTimeMillis = progressEstimate.getTimeRemainingInMillis();
                        if (remainingTimeMillis > 1000)
                            event.ruleEvaluationProgress("Decompilation", progressEstimate.getWorked(), progressEstimate.getTotal(),
                                        (int) remainingTimeMillis / 1000);
                    }

                    String mainClassFile = inputPath.get(0);

                    FileService fileService = new FileService(event.getGraphContext());
                    Path classFilePath = Paths.get(mainClassFile);

                    FileModel decompiledFileModel = fileService.getUniqueByProperty(FileModel.FILE_PATH,
                                decompiledOutputFile);

                    if (decompiledFileModel == null)
                    {
                        decompiledFileModel = createDecompiledFileModel(fileService);
                    }

                    if (decompiledOutputFile.endsWith(".java"))
                    {
                        JavaSourceFileModel sourceFileModel = handleDecompiledJavaFile(decompiledFileModel, fileService, classFilePath);

                        if (lineMapping != null && lineMapping.length > 0)
                        {
                            LineMappingService mappingService = new LineMappingService(event.getGraphContext());
                            LineMappingModel mappingModel = mappingService.create(lineMapping);
                            sourceFileModel.setLineMapping(mappingModel);
                        }
                    }


                    if (atomicInteger.incrementAndGet() % 100 == 0)
                    {
                        LOG.info("Performing periodic commit (" + atomicInteger.get() + ")");
                        event.getGraphContext().commit();
                    }
                }

                private FileModel createDecompiledFileModel(FileService fileService)
                {
                    FileModel decompiledFileModel;
                    FileModel parentFileModel = fileService.findByPath(Paths.get(decompiledOutputFile)
                            .getParent()
                            .toString());
                    // make sure parent files already exist
                    // (it can happen that it does not if PROCYON puts the decompiled .java file in an unexpected
                    // place, for example in the case
                    // of war files)
                    if (parentFileModel == null)
                    {
                        List<Path> lineage = new LinkedList<>();
                        Path parentPath = Paths.get(decompiledOutputFile).getParent();
                        FileModel existingParentFM = parentFileModel;
                        while (existingParentFM == null)
                        {
                            lineage.add(0, parentPath);
                            parentPath = parentPath.getParent();
                            existingParentFM = fileService.findByPath(parentPath.toString());
                        }

                        FileModel currentParent = existingParentFM;
                        for (Path p : lineage)
                        {
                            currentParent = fileService.createByFilePath(currentParent, p.toString());
                        }
                        parentFileModel = currentParent;
                    }
                    decompiledFileModel = fileService.createByFilePath(parentFileModel, decompiledOutputFile);
                    return decompiledFileModel;
                }

                private JavaSourceFileModel handleDecompiledJavaFile(FileModel decompiledFileModel, FileService fileService, Path classFilePath) throws RuntimeException, NonUniqueResultException
                {
                    if (!(decompiledFileModel instanceof JavaSourceFileModel))
                    {
                        decompiledFileModel = new GraphService<>(event.getGraphContext(), JavaSourceFileModel.class)
                                .addTypeToModel(decompiledFileModel);
                    }
                    JavaSourceFileModel decompiledSourceFileModel = (JavaSourceFileModel) decompiledFileModel;
                    TechnologyTagService techTagService = new TechnologyTagService(event.getGraphContext());
                    techTagService.addTagToFileModel(decompiledSourceFileModel, TECH_TAG, TECH_TAG_LEVEL);

                    // Don't also tag it as a regular source model (only tag it as decompiled).
                    // This can happen if the source file was already there, but tagged as something else before.
                    techTagService.removeTagFromFileModel(decompiledSourceFileModel, IndexJavaSourceFilesRuleProvider.TECH_TAG);

                    FileModel classFileModel = fileService.getUniqueByProperty(
                            FileModel.FILE_PATH, classFilePath.toAbsolutePath().toString());
                    if (classFileModel != null && classFileModel instanceof JavaClassFileModel)
                    {
                        ProjectModel projectModel = classFileModel.getProjectModel();

                        // only add it to the project model if it is not already there
                        if (decompiledFileModel.getProjectModel() == null || !decompiledFileModel.getProjectModel().equals(projectModel))
                        {
                            projectModel.addFileModel(decompiledFileModel);
                        }

                        JavaClassFileModel classModel = (JavaClassFileModel) classFileModel;
                        classModel.getJavaClass().setDecompiledSource(decompiledSourceFileModel);
                        decompiledSourceFileModel.setPackageName(classModel.getPackageName());
                        decompiledSourceFileModel.setWindupGenerated(true);
                        setupClassToJavaConnections(event.getGraphContext(),inputPath,decompiledSourceFileModel);

                        // Set the root path of this source file (if possible). Procyon should always be placing the file
                        // into a location that is appropriate for the package name, so this should always yield
                        // a non-null root path.
                        Path rootSourcePath = PathUtil.getRootFolderForSource(decompiledSourceFileModel.asFile().toPath(),
                                classModel.getPackageName());
                        if (rootSourcePath != null)
                        {
                            FileModel rootSourceFileModel = fileService.createByFilePath(rootSourcePath.toString());
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
                    return decompiledSourceFileModel;
                }

            };

            queueSize.incrementAndGet();
            executorService.submit(saveDecompiledRunnable);

            while (queueSize.get() > 100000)
            {
                try
                {
                    LOG.warning("Decompiler queue size got over 1000, waiting for 250 ms.");
                    Thread.sleep(250L);
                }
                catch (InterruptedException e)
                {
                    // noop
                }
            }
        }

        @Override
        public String toString()
        {
            return "DecompileWithProcyon";
        }
    }


}
