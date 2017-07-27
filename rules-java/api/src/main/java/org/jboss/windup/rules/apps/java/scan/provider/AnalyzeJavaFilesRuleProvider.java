package org.jboss.windup.rules.apps.java.scan.provider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.windup.ast.java.ASTProcessor;
import org.jboss.windup.ast.java.BatchASTFuture;
import org.jboss.windup.ast.java.BatchASTListener;
import org.jboss.windup.ast.java.BatchASTProcessor;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.ast.java.data.annotations.AnnotationArrayValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationClassReference;
import org.jboss.windup.ast.java.data.annotations.AnnotationLiteralValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationValue;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.TechnologyMetadata;
import org.jboss.windup.config.metadata.TechnologyMetadataProvider;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.TechnologyReferenceModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.JavaTechnologyMetadata;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.scan.ast.WindupWildcardImportResolver;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationListTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.service.TypeReferenceService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.ProgressEstimate;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.exception.WindupStopException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scan the Java Source code files and store the used type information from them.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, haltOnException = true)
public class AnalyzeJavaFilesRuleProvider extends AbstractRuleProvider
{
    static final String UNPARSEABLE_JAVA_CLASSIFICATION = "Unparseable Java File";
    static final String UNPARSEABLE_JAVA_DESCRIPTION = "This Java file could not be parsed";

    public static final int COMMIT_INTERVAL = 500;
    public static final int LOG_INTERVAL = 250;
    private static final Logger LOG = Logging.get(AnalyzeJavaFilesRuleProvider.class);

    @Inject
    private WindupWildcardImportResolver importResolver;

    @Inject
    private TechnologyMetadataProvider technologyMetadataProvider;

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .perform(new ParseSourceOperation());
    }
    // @formatter:on

    final AtomicInteger ticks = new AtomicInteger();

    private final class ParseSourceOperation extends GraphOperation
    {
        private static final int ANALYSIS_QUEUE_SIZE = 5000;

        final Map<Path, JavaSourceFileModel> sourcePathToFileModel = new TreeMap<>();

        public void perform(final GraphRewrite event, EvaluationContext context)
        {

            ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.analyzeFile");
            try
            {
                final GraphContext graphContext = event.getGraphContext();
                WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(graphContext);

                Iterable<JavaSourceFileModel> allJavaSourceModels = graphContext.service(JavaSourceFileModel.class).findAll();

                final Set<Path> allSourceFiles = new TreeSet<>();

                Set<String> sourcePaths = new HashSet<>();
                for (JavaSourceFileModel javaFile : allJavaSourceModels)
                {
                    FileModel rootSourceFolder = javaFile.getRootSourceFolder();
                    if (rootSourceFolder != null)
                    {
                        sourcePaths.add(rootSourceFolder.getFilePath());
                    }

                    if (windupJavaConfigurationService.shouldScanPackage(javaFile.getPackageName()))
                    {
                        Path path = Paths.get(javaFile.getFilePath());
                        allSourceFiles.add(path);
                        sourcePathToFileModel.put(path, javaFile);
                    }
                }

                LOG.log(Level.INFO, "Analyzing {0} Java source files.", allSourceFiles.size());

                WindupJavaConfigurationModel javaConfiguration = WindupJavaConfigurationService.getJavaConfigurationModel(graphContext);

                Set<String> libraryPaths = collectLibraryPaths(graphContext, javaConfiguration);

                ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.parseFiles");
                final boolean classNotFoundAnalysisEnabled = javaConfiguration.isClassNotFoundAnalysisEnabled();
                try
                {
                    WindupWildcardImportResolver.setContext(graphContext);
                    parseJavaFiles(event, classNotFoundAnalysisEnabled, allSourceFiles, libraryPaths, sourcePaths, graphContext, context);
                }
                catch (WindupStopException ex)
                {
                    throw new WindupStopException(ex); // To get a sane stack trace
                }
                catch (Exception e)
                {
                    LOG.log(Level.SEVERE, "Could not analyze java files: " + e.getMessage(), e);
                }
                finally
                {
                    WindupWildcardImportResolver.setContext(null);
                    ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.parseFiles");
                }
            }
            finally
            {
                sourcePathToFileModel.clear();
                ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.analyzeFile");
            }
        }

        private void parseJavaFiles(final GraphRewrite event, final boolean classNotFoundAnalysisEnabled, final Set<Path> allSourceFiles, Set<String> libraryPaths, Set<String> sourcePaths, final GraphContext graphContext, EvaluationContext context) throws InterruptedException
        {
            final BlockingQueue<Pair<Path, List<ClassReference>>> processedPaths = new ArrayBlockingQueue<>(ANALYSIS_QUEUE_SIZE);
            final ConcurrentMap<Path, String> failures = new ConcurrentHashMap<>();
            BatchASTListener listener = new BatchASTListener()
            {
                @Override
                public void processed(Path filePath, List<ClassReference> references)
                {
                    checkExecutionStopRequest(event);
                    try
                    {
                        processedPaths.put(new ImmutablePair<>(filePath, filterClassReferences(references, classNotFoundAnalysisEnabled)));
                    }
                    catch (InterruptedException e)
                    {
                        throw new WindupException(e.getMessage(), e);
                    }
                }

                @Override
                public void failed(Path filePath, Throwable cause)
                {
                    checkExecutionStopRequest(event);
                    final String message = "Failed to process: " + filePath + " due to: " + cause.getMessage();
                    LOG.log(Level.WARNING, message, cause);
                    failures.put(filePath, message);
                }
            };

            Set<Path> filesToProcess = new TreeSet<>(allSourceFiles);

            BatchASTFuture future = BatchASTProcessor.analyze(listener, importResolver, libraryPaths, sourcePaths, filesToProcess);
            ProgressEstimate estimate = new ProgressEstimate(filesToProcess.size());

            // This tracks the number of items added to the graph
            AtomicInteger referenceCount = new AtomicInteger(0);

            while (!future.isDone() || !processedPaths.isEmpty())
            {
                checkExecutionStopRequest(event);

                if (processedPaths.size() > (ANALYSIS_QUEUE_SIZE / 2))
                    LOG.info("Queue size: " + processedPaths.size() + " / " + ANALYSIS_QUEUE_SIZE);
                Pair<Path, List<ClassReference>> pair = processedPaths.poll(250, TimeUnit.MILLISECONDS);
                if (pair == null)
                    continue;

                processReferences(graphContext, referenceCount, pair.getKey(), pair.getValue());

                estimate.addWork(1);
                printProgressEstimate(event, estimate);

                filesToProcess.remove(pair.getKey());
            }

            // Store failures to the graph
            final ClassificationService classificationService = new ClassificationService(graphContext);
            for (Map.Entry<Path, String> failure : failures.entrySet())
            {
                markJavaFileModelAsUnprocessed(graphContext, failure.getKey(), classificationService, event, context, failure.getValue());
            }

            if (!filesToProcess.isEmpty())
            {
                /*
                * These were rejected by the batch, so try them one file at a time because the one-at-a-time ASTParser usually succeeds where
                * the batch failed.
                */
                for (Path unprocessed : new ArrayList<>(filesToProcess))
                {
                    checkExecutionStopRequest(event);

                    try
                    {
                        List<ClassReference> references = ASTProcessor.analyze(importResolver, libraryPaths, sourcePaths, unprocessed);
                        processReferences(graphContext, referenceCount, unprocessed, filterClassReferences(references, classNotFoundAnalysisEnabled));
                        filesToProcess.remove(unprocessed);
                    }
                    catch (Exception e)
                    {
                        final String msg = "Failed to process: " + unprocessed + " due to: " + e.getMessage();
                        LOG.log(Level.WARNING, msg, e);
                        markJavaFileModelAsUnprocessed(graphContext, unprocessed, classificationService, event, context, msg);
                    }
                    estimate.addWork(1);
                    printProgressEstimate(event, estimate);
                }
            }

            if (!filesToProcess.isEmpty())
            {
                StringBuilder message = new StringBuilder();
                message.append("Failed to process " + filesToProcess.size() + " files:\n");
                for (Path unprocessed : filesToProcess)
                {
                    message.append("\tFailed to process: " + unprocessed + "\n");
                    String msg = "Could not process neither in batch or individually.";
                    markJavaFileModelAsUnprocessed(graphContext, unprocessed, classificationService, event, context, msg);
                    // Is the classification attached 2nd time here?
                }
                LOG.warning(message.toString());
            }
        }

        private void checkExecutionStopRequest(GraphRewrite event)
        {
            if (ticks.incrementAndGet() % 20 == 0)
                if (event.shouldWindupStop())
                    throw new WindupStopException("Stop requested while analyzing Java files.");
        }

        private void markJavaFileModelAsUnprocessed(GraphContext graphContext, Path unprocessed, ClassificationService classificationService, GraphRewrite event, EvaluationContext context, String msg)
        {
            JavaSourceFileModel sourceFileModel = getJavaSourceFileModel(graphContext, unprocessed);
            classificationService.attachClassification(event, context, sourceFileModel, UNPARSEABLE_JAVA_CLASSIFICATION, UNPARSEABLE_JAVA_DESCRIPTION);
            sourceFileModel.setParseError(msg);
        }

        private Set<String> collectLibraryPaths(final GraphContext graphContext, WindupJavaConfigurationModel javaConfiguration)
        {
            Set<String> libraryPaths;
            libraryPaths = new HashSet<>();
            WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(graphContext);
            for (TechnologyReferenceModel target : configurationModel.getTargetTechnologies())
            {
                TechnologyMetadata technologyMetadata = technologyMetadataProvider.getMetadata(graphContext, new TechnologyReference(target));
                if (technologyMetadata != null && technologyMetadata instanceof JavaTechnologyMetadata)
                {
                    JavaTechnologyMetadata javaMetadata = (JavaTechnologyMetadata) technologyMetadata;
                    libraryPaths.addAll(
                            javaMetadata
                                    .getAdditionalClasspaths()
                                    .stream()
                                    .map(Path::toString)
                                    .collect(Collectors.toList()));
                }
            }
            for (FileModel additionalClasspath : javaConfiguration.getAdditionalClasspaths())
            {
                libraryPaths.add(additionalClasspath.getFilePath());
            }
            Iterable<JarArchiveModel> libraries = graphContext.service(JarArchiveModel.class).findAll();
            for (JarArchiveModel library : libraries)
            {
                libraryPaths.add(library.getFilePath());
            }
            return libraryPaths;
        }

        private void commitIfNeeded(GraphContext context, int numberAddedToGraph)
        {
            if (numberAddedToGraph % COMMIT_INTERVAL == 0)
                context.getGraph().getBaseGraph().commit();
        }

        private void printProgressEstimate(GraphRewrite event, ProgressEstimate estimate)
        {
            if (estimate.getWorked() % LOG_INTERVAL != 0)
                return;

            int timeRemainingInMillis = (int) estimate.getTimeRemainingInMillis();
            if (timeRemainingInMillis > 0)
            {
                boolean windupStopRequested = event.ruleEvaluationProgress("Analyze Java", estimate.getWorked(), estimate.getTotal(), timeRemainingInMillis / 1000);
                if (windupStopRequested)
                {
                    throw new WindupStopException(Util.WINDUP_BRAND_NAME_ACRONYM + " stop requested through ruleEvaluationProgress() during " + AnalyzeJavaFilesRuleProvider.class.getName());
                }
            }

            LOG.info("Analyzed Java File: " + estimate.getWorked() + " / " + estimate.getTotal());
        }

        private List<ClassReference> filterClassReferences(List<ClassReference> references, boolean classNotFoundAnalysisEnabled)
        {
            List<ClassReference> results = new ArrayList<>(references.size());
            for (ClassReference reference : references)
            {
                boolean shouldKeep = reference.getLocation() == TypeReferenceLocation.TYPE;
                shouldKeep |= classNotFoundAnalysisEnabled && reference.getResolutionStatus() != ResolutionStatus.RESOLVED;
                shouldKeep |= TypeInterestFactory.matchesAny(reference.getQualifiedName(), reference.getLocation());

                // we are always interested in types + anything that the TypeInterestFactory has registered
                if (shouldKeep)
                {
                    results.add(reference);
                }
            }
            return results;
        }

        private void processReferences(GraphContext context, AtomicInteger referenceCount, Path filePath, List<ClassReference> references)
        {
            TypeReferenceService typeReferenceService = new TypeReferenceService(context);

            Map<ClassReference, JavaTypeReferenceModel> added = new IdentityHashMap<>(references.size());

            for (ClassReference reference : references)
            {
                if (added.containsKey(reference))
                    continue;

                JavaSourceFileModel javaSourceModel = getJavaSourceFileModel(context, filePath);
                JavaTypeReferenceModel typeReference = typeReferenceService.createTypeReference(javaSourceModel,
                            reference.getLocation(),
                            reference.getResolutionStatus(),
                            reference.getLineNumber(), reference.getColumn(), reference.getLength(),
                            reference.getQualifiedName(),
                            reference.getLine());
                added.put(reference, typeReference);
                if (reference instanceof AnnotationClassReference)
                {
                    AnnotationClassReference annotationClassReference = (AnnotationClassReference) reference;
                    Map<String, AnnotationValue> annotationValues = annotationClassReference.getAnnotationValues();
                    JavaAnnotationTypeReferenceModel annotationTypeReferenceModel = addAnnotationValues(context, javaSourceModel, typeReference, annotationValues);

                    // Link the annotation to the thing that it is annotating (method, type, etc).
                    ClassReference originalReference = annotationClassReference.getOriginalReference();
                    if (originalReference == null)
                    {
                        LOG.warning("No original reference set for annotation: " + annotationClassReference);
                    }
                    else
                    {
                        JavaTypeReferenceModel originalReferenceModel = added.get(originalReference);
                        if (originalReferenceModel == null)
                        {
                            originalReferenceModel = typeReferenceService.createTypeReference(javaSourceModel,
                                    originalReference.getLocation(),
                                    originalReference.getResolutionStatus(),
                                    originalReference.getLineNumber(), originalReference.getColumn(), originalReference.getLength(),
                                    originalReference.getQualifiedName(),
                                    originalReference.getLine());
                            added.put(originalReference, originalReferenceModel);
                        }
                        annotationTypeReferenceModel.setAnnotatedType(originalReferenceModel);
                    }
                }
                referenceCount.incrementAndGet();
                commitIfNeeded(context, referenceCount.get());
            }
        }

        private JavaSourceFileModel getJavaSourceFileModel(GraphContext context, Path filePath)
        {
            return GraphService.refresh(context, sourcePathToFileModel.get(filePath));
        }

        /**
         * Adds parameters contained in the annotation into the annotation type reference
         */
        private JavaAnnotationTypeReferenceModel addAnnotationValues(GraphContext context, JavaSourceFileModel javaSourceFileModel, JavaTypeReferenceModel typeReference,
                    Map<String, AnnotationValue> annotationValues)
        {
            GraphService<JavaAnnotationTypeReferenceModel> annotationTypeReferenceService = new GraphService<>(context,
                        JavaAnnotationTypeReferenceModel.class);

            JavaAnnotationTypeReferenceModel javaAnnotationTypeReferenceModel = annotationTypeReferenceService.addTypeToModel(typeReference);

            Map<String, JavaAnnotationTypeValueModel> valueModels = new HashMap<>();
            for (Map.Entry<String, AnnotationValue> entry : annotationValues.entrySet())
            {
                valueModels.put(entry.getKey(), getValueModelForAnnotationValue(context, javaSourceFileModel, entry.getValue()));
            }

            javaAnnotationTypeReferenceModel.setAnnotationValues(valueModels);

            return javaAnnotationTypeReferenceModel;
        }

        private JavaAnnotationTypeValueModel getValueModelForAnnotationValue(GraphContext context, JavaSourceFileModel javaSourceFileModel,
                    AnnotationValue value)
        {
            JavaAnnotationTypeValueModel result;

            if (value instanceof AnnotationLiteralValue)
            {
                GraphService<JavaAnnotationLiteralTypeValueModel> literalValueService = new GraphService<>(context,
                            JavaAnnotationLiteralTypeValueModel.class);

                AnnotationLiteralValue literal = (AnnotationLiteralValue) value;
                JavaAnnotationLiteralTypeValueModel literalValueModel = literalValueService.create();
                literalValueModel.setLiteralType(literal.getLiteralType().getSimpleName());
                literalValueModel.setLiteralValue(literal.getLiteralValue() == null ? null : literal.getLiteralValue().toString());

                result = literalValueModel;
            }
            else if (value instanceof AnnotationArrayValue)
            {
                GraphService<JavaAnnotationListTypeValueModel> listValueService = new GraphService<>(context, JavaAnnotationListTypeValueModel.class);

                AnnotationArrayValue arrayValues = (AnnotationArrayValue) value;

                JavaAnnotationListTypeValueModel listModel = listValueService.create();
                for (AnnotationValue arrayValue : arrayValues.getValues())
                {
                    listModel.addItem(getValueModelForAnnotationValue(context, javaSourceFileModel, arrayValue));
                }

                result = listModel;
            }
            else if (value instanceof AnnotationClassReference)
            {
                GraphService<JavaAnnotationTypeReferenceModel> annotationTypeReferenceService = new GraphService<>(context,
                            JavaAnnotationTypeReferenceModel.class);

                AnnotationClassReference annotationClassReference = (AnnotationClassReference) value;
                Map<String, JavaAnnotationTypeValueModel> valueModels = new HashMap<>();
                for (Map.Entry<String, AnnotationValue> entry : annotationClassReference.getAnnotationValues().entrySet())
                {
                    valueModels.put(entry.getKey(), getValueModelForAnnotationValue(context, javaSourceFileModel, entry.getValue()));
                }
                JavaAnnotationTypeReferenceModel annotationTypeReferenceModel = annotationTypeReferenceService.create();
                annotationTypeReferenceModel.setAnnotationValues(valueModels);
                attachLocationMetadata(annotationTypeReferenceModel, annotationClassReference, javaSourceFileModel);

                result = annotationTypeReferenceModel;
            }
            else
            {
                throw new WindupException("Unrecognized AnnotationValue subtype: " + value.getClass().getCanonicalName());
            }
            return result;
        }

        private void attachLocationMetadata(JavaTypeReferenceModel javaTypeReferenceModel, AnnotationClassReference annotationClassReference,
                    JavaSourceFileModel javaSourceFileModel)
        {
            javaTypeReferenceModel.setResolutionStatus(annotationClassReference.getResolutionStatus());
            javaTypeReferenceModel.setResolvedSourceSnippit(annotationClassReference.getQualifiedName());
            javaTypeReferenceModel.setSourceSnippit(annotationClassReference.getLine());
            javaTypeReferenceModel.setReferenceLocation(annotationClassReference.getLocation());
            javaTypeReferenceModel.setColumnNumber(annotationClassReference.getColumn());
            javaTypeReferenceModel.setLineNumber(annotationClassReference.getLineNumber());
            javaTypeReferenceModel.setLength(annotationClassReference.getLength());
            javaTypeReferenceModel.setFile(javaSourceFileModel);
        }

        @Override
        public String toString()
        {
            return "ParseJavaSource";
        }
    }
}
