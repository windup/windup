package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.ast.java.JavaASTProcessor;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.ClassReferences;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.ast.java.data.annotations.AnnotationArrayValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationClassReference;
import org.jboss.windup.ast.java.data.annotations.AnnotationLiteralValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationValue;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
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
    public AnalyzeJavaFilesRuleProvider()
    {
        super(MetadataBuilder.forProvider(AnalyzeJavaFilesRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when(Query.fromType(JavaSourceFileModel.class))
            .perform(new ParseSourceOperation()
            .and(IterationProgress.monitoring("Analyzed Java File: ", 250))
            .and(Commit.every(10)));
    }
    // @formatter:on

    private final class ParseSourceOperation extends AbstractIterationOperation<JavaSourceFileModel>
    {
        public void perform(GraphRewrite event, EvaluationContext context, JavaSourceFileModel payload)
        {
            ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.analyzeFile");
            try
            {
                WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(
                            event.getGraphContext());
                if (!windupJavaConfigurationService.shouldScanPackage(payload.getPackageName()))
                {
                    // should not analyze this one, skip it
                    return;
                }

                GraphService<JavaSourceFileModel> service = new GraphService<JavaSourceFileModel>(event.getGraphContext(), JavaSourceFileModel.class);
                Iterable<JavaSourceFileModel> findAll = service.findAll();
                Set<String> javaFilePaths = new HashSet<String>();
                for (JavaSourceFileModel javaFile : findAll)
                {
                    FileModel rootSourceFolder = javaFile.getRootSourceFolder();
                    if (rootSourceFolder != null)
                    {
                        javaFilePaths.add(rootSourceFolder.getFilePath());
                    }
                }
                // TODO: May be adding even the project
                GraphService<JarArchiveModel> libraryService = new GraphService<JarArchiveModel>(event.getGraphContext(), JarArchiveModel.class);
                Iterable<JarArchiveModel> libraries = libraryService.findAll();
                Set<String> librariesPaths = new HashSet<>();
                for (JarArchiveModel library : libraries)
                {
                    if (library.getUnzippedDirectory() != null)
                    {
                        librariesPaths.add(library.getUnzippedDirectory().getFilePath());
                    }
                    else
                    {
                        librariesPaths.add(library.getFilePath());
                    }
                }

                File sourceFile = payload.asFile();

                ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.parseFile");
                WindupWildcardImportResolver importResolver = new WindupWildcardImportResolver();
                WindupWildcardImportResolver.setGraphContext(event.getGraphContext());
                try
                {
                    ClassReferences references = JavaASTProcessor.analyzeJavaFile(importResolver, librariesPaths, javaFilePaths,
                                sourceFile.toPath());
                    TypeReferenceService typeReferenceService = new TypeReferenceService(event.getGraphContext());
                    for (ClassReference reference : references.getReferences())
                    {
                        // we are always interested in types + anything that the TypeInterestFactory has registered
                        if (reference.getLocation() == TypeReferenceLocation.TYPE
                                    || TypeInterestFactory.matchesAny(reference.getQualifiedName(), reference.getLocation()))
                        {
                            JavaTypeReferenceModel typeReference = typeReferenceService.createTypeReference(payload, reference.getLocation(),
                                        reference.getLineNumber(), reference.getColumn(), reference.getLength(), reference.getQualifiedName(),
                                        reference.getLine());
                            if (reference instanceof AnnotationClassReference)
                            {
                                Map<String, AnnotationValue> annotationValues = ((AnnotationClassReference) reference).getAnnotationValues();
                                addAnnotationValues(event.getGraphContext(), typeReference, annotationValues);
                            }
                        }
                    }
                    ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.parseFile");
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
