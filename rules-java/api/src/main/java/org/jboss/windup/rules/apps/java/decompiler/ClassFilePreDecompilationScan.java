package org.jboss.windup.rules.apps.java.decompiler;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.windup.ast.java.ClassFileScanner;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.scan.ast.ignore.JavaClassIgnoreResolver;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.ProgressEstimate;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.threading.WindupExecutors;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * An operation doing a pre-scan of the .class file in order to check if it is possible to tell in advance if it is worth decompiling the class.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * @author Ondrej Zizka
 */
public class ClassFilePreDecompilationScan extends GraphOperation
{
    private static final Logger LOG = Logging.get(ClassFilePreDecompilationScan.class);

    String UNPARSEABLE_CLASS_CLASSIFICATION = "Unparseable Class File";
    String UNPARSEABLE_CLASS_DESCRIPTION = "This Class file could not be parsed";

    private void addClassFileMetadata(GraphRewrite event, EvaluationContext context, JavaClassFileModel javaClassFileModel,
                ClassFileScanner classFileScanner)
    {
        try
        {
            final ClassFileScanner.ClassInfo classInfo = classFileScanner.getClassInfo(javaClassFileModel.getFilePath());
            if (classInfo == null || StringUtils.isNotBlank(classInfo.getError()))
            {
                String errorMessage = classInfo != null ? classInfo.getError() : "-- N/A --";
                ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                classificationService.attachClassification(event, context, javaClassFileModel, UNPARSEABLE_CLASS_CLASSIFICATION,
                            UNPARSEABLE_CLASS_DESCRIPTION);
                javaClassFileModel.setParseError(errorMessage);
                javaClassFileModel.setSkipDecompilation(true);
                return;
            }

            final String packageName = classInfo.getPackageName();

            final String qualifiedName = classInfo.getFqdn();

            final JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
            final JavaClassModel javaClassModel = javaClassService.create(qualifiedName);
            int majorVersion = classInfo.getMajorVersion();

            String simpleName = qualifiedName;
            if (packageName != null && !packageName.isEmpty() && simpleName != null)
            {
                simpleName = StringUtils.substringAfterLast(simpleName, ".");
            }

            javaClassFileModel.setMajorVersion(majorVersion);
            javaClassFileModel.setPackageName(packageName);

            javaClassModel.setSimpleName(simpleName);
            javaClassModel.setPackageName(packageName);
            javaClassModel.setQualifiedName(qualifiedName);
            javaClassModel.setClassFile(javaClassFileModel);
            javaClassModel.setPublic(classInfo.isPublic());
            javaClassModel.setInterface(classInfo.isInterface());

            final Set<String> interfaceNames = classInfo.getImplementedInterfaces();
            if (interfaceNames != null)
            {
                for (final String interfaceName : interfaceNames)
                {
                    JavaClassModel interfaceModel = javaClassService.getOrCreatePhantom(interfaceName);
                    javaClassService.addInterface(javaClassModel, interfaceModel);
                }
            }

            String superclassName = classInfo.getSuperclass();
            if (!classInfo.isInterface() && !StringUtils.isBlank(superclassName) && !superclassName.equals("java.lang.Object"))
                javaClassModel.setExtends(javaClassService.getOrCreatePhantom(superclassName));

            javaClassFileModel.setJavaClass(javaClassModel);
        }
        catch (Exception ex)
        {
            String nl = ex.getMessage() != null ? Util.NL + "\t" : " ";
            final String message = "ASM was unable to parse class file '" + javaClassFileModel.getFilePath() + "':" + nl + ex.toString();
            LOG.log(Level.WARNING, message);
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(event, context, javaClassFileModel, UNPARSEABLE_CLASS_CLASSIFICATION,
                        UNPARSEABLE_CLASS_DESCRIPTION);
            javaClassFileModel.setParseError(message);
            javaClassFileModel.setSkipDecompilation(true);
        }
    }

    /**
     * Returns true if this should skip the decompilation.
     */
    private boolean filterClassesToDecompile(GraphRewrite event, JavaClassFileModel fileModel, Collection<ClassReference> references)
    {
        if (fileModel.getSkipDecompilation() != null && fileModel.getSkipDecompilation())
            return true;

        WindupJavaConfigurationService configurationService = new WindupJavaConfigurationService(event.getGraphContext());
        boolean shouldScan;
        if (fileModel.getPackageName() != null)
        {
            shouldScan = configurationService.shouldScanPackage(fileModel.getPackageName());
            LOG.info("Skipping decompilation for (pkgname): " + fileModel.getFilePath() + " due to package configuration? " + !shouldScan + " pkg: " + fileModel.getPackageName());
        }
        else
        {
            shouldScan = configurationService.shouldScanFile(fileModel.getFilePath());
            LOG.info("Skipping decompilation for (filepath): " + fileModel.getFilePath() + " due to package configuration? " + !shouldScan);
        }

        if (!shouldScan)
        {
            LOG.info("Skipping decompilation for: " + fileModel.getFilePath() + " due to configuration!");
            return true;
        }

        // keep inner classes (we may need them for decompilation purposes)
        if (fileModel.getFileName().contains("$"))
            return false;

        // If we should ignore any of the contained classes, skip decompilation of the whole file.
        for (ClassReference typeReference : references)
        {
            if (shouldIgnore(typeReference.getFullyQualifiedClassName()))
            {
                LOG.info("Skipping decompilation for: " + fileModel.getFilePath() + " due javaclass-ignore: " + typeReference.getFullyQualifiedClassName() + "!");
                return true;
            }
        }

        return false;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        ExecutionStatistics.get().begin("ClassFilePreDecompilationScan.perform()");
        try
        {
            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            Set<String> classpaths = new HashSet<>();
            for (FileModel inputPath : configuration.getInputPaths())
            {
                if (inputPath.isDirectory())
                    classpaths.add(inputPath.getFilePath());
            }
            ArchiveService archiveService = new ArchiveService(event.getGraphContext());
            for (ArchiveModel archiveModel : archiveService.findAll())
            {
                /*
                 * If we have unzipped the zip file, then use the unzipped directory.
                 *
                 * Otherwise, use the zip file itself.
                 */
                if (StringUtils.isBlank(archiveModel.getUnzippedDirectory()))
                    classpaths.add(archiveModel.getFilePath());
                else
                    classpaths.add(archiveModel.getUnzippedDirectory());
            }
            ClassFileScanner classFileScanner = new ClassFileScanner(classpaths);

            GraphService<JavaClassFileModel> javaClassFileService = new GraphService<>(event.getGraphContext(), JavaClassFileModel.class);
            ClassFileMap classFileMap = new ClassFileMap();

            int totalWorkCounter = 0;
            for (JavaClassFileModel javaClassFileModel : javaClassFileService.findAllWithoutProperty(FileModel.PARSE_ERROR))
            {
                classFileMap.addClass(javaClassFileModel);
                totalWorkCounter++;
            }
            // Include both the scan and the metadata work
            final int totalWork = totalWorkCounter * 2;
            ProgressEstimate progressEstimate = new ProgressEstimate(totalWork);

            // Collect all class metadata
            for (List<JavaClassFileModel> classBatch : classFileMap.getClasses())
            {
                classBatch.sort(Comparator.comparingInt(o -> o.getFileName().length()));

                for (JavaClassFileModel fileModel : classBatch)
                {
                    addClassFileMetadata(event, context, fileModel, classFileScanner);
                }
                progressEstimate.addWork(classBatch.size());
                printProgress(event, progressEstimate, totalWork);
                if (progressEstimate.getWorked() % 100 == 0)
                    event.getGraphContext().commit();
            }
            event.getGraphContext().commit();

            ExecutorService executorService = WindupExecutors.newFixedThreadPool(WindupExecutors.getDefaultThreadCount());

            /*
             * Doing graph work with the same vertices across multiple threads is a little difficult due
             * to the way GraphContext is structured.
             *
             * So instead... just use accumulate the results in Futures and then process them at the end.
             */
            List<Pair<List<JavaClassFileModel>, Future<Boolean>>> shouldSkipFutures = new ArrayList<>();
            for (List<JavaClassFileModel> classBatch : classFileMap.getClasses())
            {
                classBatch.sort(Comparator.comparingInt(o -> o.getFileName().length()));

                Future<Boolean> shouldSkipFuture = executorService.submit(() -> {
                    try
                    {
                        boolean shouldSkip = true;
                        for (JavaClassFileModel fileModel : classBatch)
                        {
                            Collection<ClassReference> references;
                            try
                            {
                                references = classFileScanner.scanClass(Paths.get(fileModel.getFilePath()));
                            }
                            catch (Exception ex)
                            {
                                // this is dealt with elsewhere in the code
                                continue;
                            }
                            if (filterClassesToDecompile(event, fileModel, references))
                            {
                                LOG.info("filterClassesToDecompile Setting should skip for: " + fileModel.getFilePath() + " id: " + fileModel.getId() + " to " + shouldSkip);
                                shouldSkip = shouldSkip && true;
                                continue;
                            }

                            Map<String, ClassReference> deduplicatedReferences = new HashMap<>();
                            for (ClassReference classReference : references)
                            {
                                String key = classReference.getLocation() + "_" + classReference.getQualifiedName();
                                if (!deduplicatedReferences.containsKey(key))
                                    deduplicatedReferences.put(key, classReference);

                                // Also, include an import line for each qualified name
                                String importQualifiedName = StringUtils.isNotBlank(classReference.getPackageName())
                                            ? classReference.getPackageName() + "."
                                            : "";

                                // For import purposes, do not include the array markers
                                importQualifiedName += classReference.getClassName().replace("[", "").replace("]", "");
                                key = TypeReferenceLocation.IMPORT + "_" + importQualifiedName;

                                if (!deduplicatedReferences.containsKey(key))
                                    deduplicatedReferences.put(key,
                                                new ClassReference(importQualifiedName, classReference.getPackageName(),
                                                            classReference.getClassName(),
                                                            null, classReference.getResolutionStatus(), TypeReferenceLocation.IMPORT,
                                                            classReference.getLineNumber(), classReference.getColumn(),
                                                            classReference.getLength(),
                                                            classReference.getLine()));
                            }

                            LOG.info("typeinterestfactory references search: " + fileModel.getFilePath() + " id: " + fileModel.getId() + " to " + deduplicatedReferences.values());
                            for (ClassReference reference : deduplicatedReferences.values())
                            {
                                if (TypeInterestFactory.matchesAny(reference.getQualifiedName(), reference.getLocation()))
                                {
                                    LOG.info("typeinterestfactory Setting should skip for: " + fileModel.getFilePath() + " id: " + fileModel.getId() + " to " + false);
                                    shouldSkip = false;
                                }
                            }
                        }
                        return shouldSkip;
                    }
                    finally
                    {
                        progressEstimate.addWork(classBatch.size());
                        printProgress(event, progressEstimate, totalWork);
                    }
                });

                /*
                 * Create the Future and save it for later.
                 */
                Pair<List<JavaClassFileModel>, Future<Boolean>> shouldSkipPair = ImmutablePair.of(classBatch, shouldSkipFuture);
                shouldSkipFutures.add(shouldSkipPair);
            }
            executorService.shutdown();
            try
            {
                executorService.awaitTermination(10, TimeUnit.DAYS);

                /*
                 * Process all of the completed futures.
                 */
                for (Pair<List<JavaClassFileModel>, Future<Boolean>> shouldSkipPair : shouldSkipFutures)
                {
                    for (JavaClassFileModel javaClassFileModel : shouldSkipPair.getLeft())
                    {
                        boolean shouldSkip = shouldSkipPair.getRight().get(1, TimeUnit.NANOSECONDS);
                        LOG.info("Setting should skip for: " + javaClassFileModel.getFilePath() + " id: " + javaClassFileModel.getId() + " to " + shouldSkip);
                        javaClassFileModel.setSkipDecompilation(shouldSkipPair.getRight().get(1, TimeUnit.NANOSECONDS)); // Short timeout as the executor is already shut down.
                    }
                }
            }
            catch (Throwable t)
            {
                throw new WindupException(t);
            }

            event.getGraphContext().commit();
        }
        finally
        {
            ExecutionStatistics.get().end("ClassFilePreDecompilationScan.perform()");
        }
    }

    private void printProgress(GraphRewrite event, ProgressEstimate progressEstimate, int totalWork)
    {
        if (progressEstimate.getWorked() % 1000 == 0)
        {
            long remainingTimeMillis = progressEstimate.getTimeRemainingInMillis();
            if (remainingTimeMillis > 1000)
                event.ruleEvaluationProgress(ClassFilePreDecompilationScan.class.getSimpleName(), progressEstimate.getWorked(),
                            totalWork, (int) remainingTimeMillis / 1000);
            LOG.info(progressEstimate.getWorked() + " / " + totalWork);
        }
    }

    /**
     * This method is called on every reference that is in the .class file.
     *
     * @param typeReference
     * @return
     */
    private boolean shouldIgnore(String typeReference)
    {
        typeReference = typeReference.replace('/', '.').replace('\\', '.');
        return JavaClassIgnoreResolver.singletonInstance().matches(typeReference);
    }

    @Override
    public String toString()
    {
        return ClassFilePreDecompilationScan.class.getSimpleName();
    }

    private class ClassFileMap
    {
        /*
         * This maps from outer class filepaths, without inner classes) to a list of {@link JavaClassFileModel}s that includes any inner classes.
         *
         * For example, take the following files as an example: - /path/to/MyClass.class - /path/to/MyClass$1.class - /path/to/MyClass$1$1.class
         *
         * In this case, this would create a map that looks like this: - Key: /path/to/MyClass - Values: - /path/to/MyClass.class -
         * /path/to/MyClass$1.class - /path/to/MyClass$1$1.class
         */
        Map<String, List<JavaClassFileModel>> basePathToClassFiles = new HashMap<>();

        public Collection<List<JavaClassFileModel>> getClasses()
        {
            return basePathToClassFiles.values();
        }

        public void addClass(JavaClassFileModel javaClassFileModel)
        {
            String baseFilename = getBaseClassPath(javaClassFileModel);
            List<JavaClassFileModel> classFileList = basePathToClassFiles.get(baseFilename);
            if (classFileList == null)
            {
                classFileList = new ArrayList<>();
                basePathToClassFiles.put(baseFilename, classFileList);
            }
            classFileList.add(javaClassFileModel);
        }

        private String getBaseClassPath(JavaClassFileModel javaClassFileModel)
        {
            String filePath = javaClassFileModel.getFilePath();
            String filename = javaClassFileModel.getFileName();

            // This is not an inner class, so just return it without the ".class" at the end
            if (!StringUtils.contains(filename, "$"))
                return filePath.substring(0, filePath.length() - 6);

            String baseFilename = filePath.substring(0, filePath.indexOf("$"));

            /*
             * It looks a bit strange to reconstitute the full path like this instead of just searching filename itself, but this deals cleanly with
             * the edge case where a "$" is in the directory instead of the filename.
             */
            return Paths.get(filePath).getParent().resolve(baseFilename).toString();
        }
    }
}
