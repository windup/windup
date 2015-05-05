package org.jboss.windup.rules.apps.java.scan.provider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.windup.ast.java.ASTProcessor;
import org.jboss.windup.ast.java.BatchASTListener;
import org.jboss.windup.ast.java.BatchASTProcessor;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.ast.java.data.annotations.AnnotationArrayValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationClassReference;
import org.jboss.windup.ast.java.data.annotations.AnnotationLiteralValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationValue;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
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
    public static final int COMMIT_INTERVAL = 750;
    public static final int LOG_INTERVAL = 250;
    private static Logger LOG = Logging.get(AnalyzeJavaFilesRuleProvider.class);

    @Inject
    private WindupWildcardImportResolver importResolver;

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


                                    //.and(IterationProgress.monitoring("Analyzed Java File: ", 250))
                                    //.and(Commit.every(10))
    }
    // @formatter:on

    private final class ParseSourceOperation extends GraphOperation
    {
        public void perform(final GraphRewrite event, EvaluationContext context)
        {
            ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.analyzeFile");
            try
            {
                WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(
                            event.getGraphContext());

                GraphService<JavaSourceFileModel> service = new GraphService<>(event.getGraphContext(), JavaSourceFileModel.class);
                Iterable<JavaSourceFileModel> allJavaSourceModels = service.findAll();

                final Set<Path> allSourceFiles = new TreeSet<>();
                final Map<Path, JavaSourceFileModel> sourcePathToFileModel = new TreeMap<>();

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

                GraphService<JarArchiveModel> libraryService = new GraphService<JarArchiveModel>(event.getGraphContext(), JarArchiveModel.class);

                Iterable<JarArchiveModel> libraries = libraryService.findAll();
                Set<String> libraryPaths = new HashSet<>();
                for (JarArchiveModel library : libraries)
                {
                    if (library.getUnzippedDirectory() != null)
                    {
                        libraryPaths.add(library.getUnzippedDirectory().getFilePath());
                    }
                    else
                    {
                        libraryPaths.add(library.getFilePath());
                    }
                }
                ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.parseFiles");
                try
                {
                    WindupWildcardImportResolver.setGraphContext(event.getGraphContext());
                    final int totalToProcess = allSourceFiles.size();
                    final AtomicInteger numberProcessed = new AtomicInteger(0);

                    final Set<Path> processedPaths = new HashSet<>(allSourceFiles.size());
                    BatchASTListener listener = new BatchASTListener()
                    {
                        @Override
                        public void processed(Path filePath, List<ClassReference> references)
                        {
                            processedPaths.add(filePath);
                            processReferences(event.getGraphContext(), sourcePathToFileModel, filePath, references);

                            numberProcessed.incrementAndGet();
                            if (numberProcessed.get() % LOG_INTERVAL == 0)
                            {
                                LOG.info("Analyzed Java File: " + numberProcessed.get() + " / " + totalToProcess);
                            }

                            if (numberProcessed.get() % COMMIT_INTERVAL == 0)
                            {
                                event.getGraphContext().getGraph().getBaseGraph().commit();
                            }
                        }

                        @Override
                        public void failed(Path filePath, Throwable cause)
                        {
                            LOG.log(Level.WARNING, "Failed to process: " + filePath + " due to: " + cause.getMessage(), cause);
                            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                            JavaSourceFileModel sourceFileModel = sourcePathToFileModel.get(filePath);
                            classificationService.attachClassification(sourceFileModel, JavaSourceFileModel.UNPARSEABLE_JAVA_CLASSIFICATION,
                                        JavaSourceFileModel.UNPARSEABLE_JAVA_DESCRIPTION);
                        }
                    };

                    Set<Path> filesToProcess = new TreeSet<>(allSourceFiles);
                    BatchASTProcessor.analyze(listener, importResolver, libraryPaths, sourcePaths, filesToProcess);
                    filesToProcess.removeAll(processedPaths);
                    processedPaths.clear();

                    if (!filesToProcess.isEmpty())
                    {
                        // try these one file at a time
                        for (Path unprocessed : filesToProcess)
                        {
                            try
                            {
                                List<ClassReference> references = ASTProcessor.analyze(importResolver, libraryPaths, sourcePaths, unprocessed);
                                processReferences(event.getGraphContext(), sourcePathToFileModel, unprocessed, references);
                                processedPaths.add(unprocessed);
                            }
                            catch (Exception e)
                            {
                                LOG.log(Level.WARNING, "Failed to process: " + unprocessed + " due to: " + e.getMessage(), e);
                                ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                                JavaSourceFileModel sourceFileModel = sourcePathToFileModel.get(unprocessed);
                                classificationService.attachClassification(sourceFileModel, JavaSourceFileModel.UNPARSEABLE_JAVA_CLASSIFICATION,
                                            JavaSourceFileModel.UNPARSEABLE_JAVA_DESCRIPTION);
                            }
                        }
                    }
                    filesToProcess.removeAll(processedPaths);

                    if (!filesToProcess.isEmpty())
                    {
                        ClassificationService classificationService = new ClassificationService(event.getGraphContext());

                        StringBuilder message = new StringBuilder();
                        message.append("Failed to process " + filesToProcess.size() + " files:\n");
                        for (Path unprocessed : filesToProcess)
                        {
                            JavaSourceFileModel sourceFileModel = sourcePathToFileModel.get(unprocessed);
                            message.append("\tFailed to process: " + unprocessed + "\n");
                            classificationService.attachClassification(sourceFileModel, JavaSourceFileModel.UNPARSEABLE_JAVA_CLASSIFICATION,
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
                    WindupWildcardImportResolver.setGraphContext(null);
                }
            }
            finally
            {
                ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.analyzeFile");
            }
        }

        private void processReferences(GraphContext context, Map<Path, JavaSourceFileModel> sourcePathToFileModel, Path filePath,
                    List<ClassReference> references)
        {
            TypeReferenceService typeReferenceService = new TypeReferenceService(context);
            for (ClassReference reference : references)
            {
                // we are always interested in types + anything that the TypeInterestFactory has registered
                if (reference.getLocation() == TypeReferenceLocation.TYPE
                            || TypeInterestFactory.matchesAny(reference.getQualifiedName(), reference.getLocation()))
                {
                    JavaSourceFileModel javaSourceModel = sourcePathToFileModel.get(filePath);
                    JavaTypeReferenceModel typeReference = typeReferenceService.createTypeReference(javaSourceModel,
                                reference.getLocation(),
                                reference.getLineNumber(), reference.getColumn(), reference.getLength(),
                                reference.getQualifiedName(),
                                reference.getLine());
                    if (reference instanceof AnnotationClassReference)
                    {
                        Map<String, AnnotationValue> annotationValues = ((AnnotationClassReference) reference).getAnnotationValues();
                        addAnnotationValues(context, typeReference, annotationValues);
                    }
                }
            }
        }

        /**
         * Adds parameters contained in the annotation into the annotation type reference
         */
        private void addAnnotationValues(GraphContext context, JavaTypeReferenceModel typeReference, Map<String, AnnotationValue> annotationValues)
        {
            GraphService<JavaAnnotationTypeReferenceModel> annotationTypeReferenceService = new GraphService<>(context,
                        JavaAnnotationTypeReferenceModel.class);

            JavaAnnotationTypeReferenceModel javaAnnotationTypeReferenceModel = annotationTypeReferenceService.addTypeToModel(typeReference);

            Map<String, JavaAnnotationTypeValueModel> valueModels = new HashMap<>();
            for (Map.Entry<String, AnnotationValue> entry : annotationValues.entrySet())
            {
                valueModels.put(entry.getKey(), getValueModelForAnnotationValue(context, entry.getValue()));
            }

            javaAnnotationTypeReferenceModel.setAnnotationValues(valueModels);
        }

        private JavaAnnotationTypeValueModel getValueModelForAnnotationValue(GraphContext context, AnnotationValue value)
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
                    listModel.addItem(getValueModelForAnnotationValue(context, arrayValue));
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
                    valueModels.put(entry.getKey(), getValueModelForAnnotationValue(context, entry.getValue()));
                }
                JavaAnnotationTypeReferenceModel annotationTypeReferenceModel = annotationTypeReferenceService.create();
                annotationTypeReferenceModel.setAnnotationValues(valueModels);

                result = annotationTypeReferenceModel;
            }
            else
            {
                throw new WindupException("Unrecognized AnnotationValue subtype: " + value.getClass().getCanonicalName());
            }
            return result;
        }

        @Override
        public String toString()
        {
            return "ParseJavaSource";
        }
    }
}
