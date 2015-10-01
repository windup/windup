package org.jboss.windup.rules.apps.java.scan.provider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.forge.furnace.util.Sets;
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
import org.jboss.windup.config.metadata.MetadataBuilder;
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
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scan the Java Source code files and store the used type information from them.
 * 
 */
public class AnalyzeJavaFilesRuleProvider extends AbstractRuleProvider
{
    private static Object lockObject = new Object();

    public static final int COMMIT_INTERVAL = 500;
    public static final int LOG_INTERVAL = 250;
    private static Logger LOG = Logging.get(AnalyzeJavaFilesRuleProvider.class);

    @Inject
    private WindupWildcardImportResolver importResolver;

    @Inject
    private TechnologyMetadataProvider technologyMetadataProvider;

    public AnalyzeJavaFilesRuleProvider()
    {
        super(MetadataBuilder.forProvider(AnalyzeJavaFilesRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .setHaltOnException(true));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .perform(new ParseSourceOperation());
    }
    // @formatter:on

    private final class ParseSourceOperation extends GraphOperation
    {
        private static final int ANALYSIS_QUEUE_SIZE = 5000;

        final Map<Path, JavaSourceFileModel> sourcePathToFileModel = new TreeMap<>();

        public void perform(final GraphRewrite event, EvaluationContext context)
        {
            ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.analyzeFile");
            try
            {
                WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(
                            event.getGraphContext());
                final WindupJavaConfigurationModel javaConfiguration = WindupJavaConfigurationService
                            .getJavaConfigurationModel(event.getGraphContext());
                final boolean classNotFoundAnalysisEnabled = javaConfiguration.isClassNotFoundAnalysisEnabled();

                GraphService<JavaSourceFileModel> service = new GraphService<>(event.getGraphContext(), JavaSourceFileModel.class);
                Iterable<JavaSourceFileModel> allJavaSourceModels = service.findAll();

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

                GraphService<JarArchiveModel> libraryService = new GraphService<>(event.getGraphContext(), JarArchiveModel.class);

                Iterable<JarArchiveModel> libraries = libraryService.findAll();
                Set<String> libraryPaths = new HashSet<>();

                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                for (TechnologyReferenceModel target : configurationModel.getTargetTechnologies())
                {
                    TechnologyMetadata technologyMetadata = technologyMetadataProvider.getMetadata(event.getGraphContext(),
                                new TechnologyReference(target));
                    if (technologyMetadata != null && technologyMetadata instanceof JavaTechnologyMetadata)
                    {
                        JavaTechnologyMetadata javaMetadata = (JavaTechnologyMetadata) technologyMetadata;
                        for (Path additionalClasspath : javaMetadata.getAdditionalClasspaths())
                            libraryPaths.add(additionalClasspath.toString());
                    }
                }

                for (FileModel additionalClasspath : javaConfiguration.getAdditionalClasspaths())
                {
                    libraryPaths.add(additionalClasspath.getFilePath());
                }

                for (JarArchiveModel library : libraries)
                {
                    libraryPaths.add(library.getFilePath());
                }

                ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.parseFiles");
                try
                {
                    WindupWildcardImportResolver.setContext(event.getGraphContext());

                    final BlockingQueue<Pair<Path, List<ClassReference>>> processedPaths = new ArrayBlockingQueue<>(ANALYSIS_QUEUE_SIZE);
                    final Set<Path> failedPaths = Sets.getConcurrentSet();
                    BatchASTListener listener = new BatchASTListener()
                    {
                        @Override
                        public void processed(Path filePath, List<ClassReference> references)
                        {
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
                            LOG.log(Level.WARNING, "Failed to process: " + filePath + " due to: " + cause.getMessage(), cause);
                            failedPaths.add(filePath);
                        }
                    };

                    Set<Path> filesToProcess = new TreeSet<>(allSourceFiles);

                    BatchASTFuture future = BatchASTProcessor.analyze(listener, importResolver, libraryPaths, sourcePaths, filesToProcess);
                    ProgressEstimate estimate = new ProgressEstimate(filesToProcess.size());

                    // This tracks the number of items added to the graph
                    AtomicInteger referenceCount = new AtomicInteger(0);

                    while (!future.isDone() || !processedPaths.isEmpty())
                    {
                        if (processedPaths.size() > (ANALYSIS_QUEUE_SIZE / 2))
                            LOG.info("Queue size: " + processedPaths.size() + " / " + ANALYSIS_QUEUE_SIZE);
                        Pair<Path, List<ClassReference>> pair = processedPaths.poll(250, TimeUnit.MILLISECONDS);
                        if (pair == null)
                            continue;

                        processReferences(event.getGraphContext(), referenceCount, pair.getKey(), pair.getValue());

                        estimate.addWork(1);
                        printProgressEstimate(event, estimate);

                        filesToProcess.remove(pair.getKey());
                    }

                    for (Path path : failedPaths)
                    {
                        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                        JavaSourceFileModel sourceFileModel = getJavaSourceFileModel(event.getGraphContext(), path);
                        classificationService.attachClassification(context, sourceFileModel, JavaSourceFileModel.UNPARSEABLE_JAVA_CLASSIFICATION,
                                    JavaSourceFileModel.UNPARSEABLE_JAVA_DESCRIPTION);
                    }

                    if (!filesToProcess.isEmpty())
                    {
                        /*
                         * These were rejected by the batch, so try them one file at a time because the one-at-a-time ASTParser usually succeeds where
                         * the batch failed.
                         */
                        for (Path unprocessed : new ArrayList<>(filesToProcess))
                        {
                            try
                            {
                                List<ClassReference> references = ASTProcessor.analyze(importResolver, libraryPaths, sourcePaths, unprocessed);
                                processReferences(event.getGraphContext(), referenceCount, unprocessed,
                                            filterClassReferences(references, classNotFoundAnalysisEnabled));
                                filesToProcess.remove(unprocessed);
                            }
                            catch (Exception e)
                            {
                                LOG.log(Level.WARNING, "Failed to process: " + unprocessed + " due to: " + e.getMessage(), e);
                                ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                                JavaSourceFileModel sourceFileModel = getJavaSourceFileModel(event.getGraphContext(), unprocessed);
                                classificationService.attachClassification(context, sourceFileModel,
                                            JavaSourceFileModel.UNPARSEABLE_JAVA_CLASSIFICATION,
                                            JavaSourceFileModel.UNPARSEABLE_JAVA_DESCRIPTION);
                            }
                            estimate.addWork(1);
                            printProgressEstimate(event, estimate);
                        }
                    }

                    if (!filesToProcess.isEmpty())
                    {
                        ClassificationService classificationService = new ClassificationService(event.getGraphContext());

                        StringBuilder message = new StringBuilder();
                        message.append("Failed to process " + filesToProcess.size() + " files:\n");
                        for (Path unprocessed : filesToProcess)
                        {
                            JavaSourceFileModel sourceFileModel = getJavaSourceFileModel(event.getGraphContext(), unprocessed);
                            message.append("\tFailed to process: " + unprocessed + "\n");
                            classificationService.attachClassification(context, sourceFileModel, JavaSourceFileModel.UNPARSEABLE_JAVA_CLASSIFICATION,
                                        JavaSourceFileModel.UNPARSEABLE_JAVA_DESCRIPTION);
                        }
                        LOG.warning(message.toString());
                    }

                    ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.parseFiles");
                }
                catch (Exception e)
                {
                    LOG.log(Level.SEVERE, "Could not analyze java files: " + e.getMessage(), e);
                }
                finally
                {
                    WindupWildcardImportResolver.setContext(null);
                }
            }
            finally
            {
                ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.analyzeFile");
            }
        }

        private void commitIfNeeded(GraphContext context, int numberAddedToGraph)
        {
            if (numberAddedToGraph % COMMIT_INTERVAL == 0)
                context.getGraph().getBaseGraph().commit();
        }

        private void printProgressEstimate(GraphRewrite event, ProgressEstimate estimate)
        {
            if (estimate.getWorked() % LOG_INTERVAL == 0)
            {
                int timeRemainingInMillis = (int) estimate.getTimeRemainingInMillis();
                if (timeRemainingInMillis > 0)
                {

                    event.ruleEvaluationProgress("Analyze Java", estimate.getWorked(), estimate.getTotal(), timeRemainingInMillis / 1000);
                }

                LOG.info("Analyzed Java File: " + estimate.getWorked() + " / " + estimate.getTotal());
            }
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
            for (ClassReference reference : references)
            {
                JavaSourceFileModel javaSourceModel = getJavaSourceFileModel(context, filePath);
                JavaTypeReferenceModel typeReference = typeReferenceService.createTypeReference(javaSourceModel,
                            reference.getLocation(),
                            reference.getResolutionStatus(),
                            reference.getLineNumber(), reference.getColumn(), reference.getLength(),
                            reference.getQualifiedName(),
                            reference.getLine());
                if (reference instanceof AnnotationClassReference)
                {
                    Map<String, AnnotationValue> annotationValues = ((AnnotationClassReference) reference).getAnnotationValues();
                    addAnnotationValues(context, javaSourceModel, typeReference, annotationValues);
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
        private void addAnnotationValues(GraphContext context, JavaSourceFileModel javaSourceFileModel, JavaTypeReferenceModel typeReference,
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
