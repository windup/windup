package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.ast.java.JavaASTProcessor;
import org.jboss.windup.ast.java.data.JavaClassReference;
import org.jboss.windup.ast.java.data.JavaClassReferences;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.scan.ast.WindupWildcardImportResolver;
import org.jboss.windup.rules.apps.java.service.TypeReferenceService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scan the Java Source code files and store the used type information from them.
 * 
 */
public class AnalyzeJavaFilesRuleProvider extends AbstractRuleProvider
{

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return InitialAnalysisPhase.class;
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
                    JavaClassReferences references = JavaASTProcessor.analyzeJavaFile(importResolver, librariesPaths, javaFilePaths,
                                sourceFile.toPath());
                    TypeReferenceService typeReferenceService = new TypeReferenceService(event.getGraphContext());
                    for (JavaClassReference reference : references.getReferences())
                    {
                        // we are always interested in types + anything that the TypeInterestFactory has registered
                        if (reference.getLocation() == TypeReferenceLocation.TYPE
                                    || TypeInterestFactory.matchesAny(reference.getQualifiedName(), reference.getLocation()))
                        {
                            JavaTypeReferenceModel typeReference = typeReferenceService.createTypeReference(payload, reference.getLocation(),
                                        reference.getLineNumber(), reference.getColumn(), reference.getLength(), reference.getQualifiedName());
                            if (reference.getLocation() == TypeReferenceLocation.ANNOTATION)
                            {
                                addAnnotationValues(event.getGraphContext(), typeReference, reference.getAnnotationValues());
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
        private void addAnnotationValues(GraphContext context, JavaTypeReferenceModel typeReference, Map<String, String> annotationValues)
        {
            GraphService<JavaAnnotationTypeReferenceModel> annotationTypeReferenceService = new GraphService<>(context,
                        JavaAnnotationTypeReferenceModel.class);
            JavaAnnotationTypeReferenceModel javaAnnotationTypeReferenceModel = annotationTypeReferenceService.addTypeToModel(typeReference);
            javaAnnotationTypeReferenceModel.setAnnotationValues(annotationValues);
        }

        @Override
        public String toString()
        {
            return "ParseJavaSource";
        }
    }
}
