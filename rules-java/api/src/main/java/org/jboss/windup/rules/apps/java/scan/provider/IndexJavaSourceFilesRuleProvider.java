package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.forge.furnace.util.Strings;
import org.jboss.forge.roaster.ParserException;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Extendable;
import org.jboss.forge.roaster.model.InterfaceCapable;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.condition.SourceMode;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.WindupWildcardImportResolver;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider.UNPARSEABLE_JAVA_CLASSIFICATION;
import static org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider.UNPARSEABLE_JAVA_DESCRIPTION;

/**
 * Discovers .java files from the applications being analyzed.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = ClassifyFileTypesPhase.class)
public class IndexJavaSourceFilesRuleProvider extends AbstractRuleProvider {
    public static final String TECH_TAG = "Java Source";
    private static final Logger LOG = Logging.get(IndexJavaSourceFilesRuleProvider.class);
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(JavaSourceFileModel.class))
                .perform(new IndexJavaFileIterationOperator()
                        .and(Commit.every(100))
                        .and(IterationProgress.monitoring("Index Java Source Files", 250)));
    }

    private final class IndexJavaFileIterationOperator extends AbstractIterationOperation<JavaSourceFileModel> {
        private static final int JAVA_SUFFIX_LEN = 5;

        private IndexJavaFileIterationOperator() {
            super();
        }

        /**
         * If this is in the input directory, return the input directory. Otherwise, return null.
         */
        private String getInputPathForSource(WindupConfigurationModel configuration, String javaPath) {
            for (FileModel input : configuration.getInputPaths()) {
                if (javaPath.startsWith(input.getFilePath()))
                    return input.getFilePath();
            }
            return null;
        }

        @Override
        public void perform(GraphRewrite event, EvaluationContext context, JavaSourceFileModel payload) {
            // If we are in binary mode, then we ignore java sources. Just remove them from the graph
            if (SourceMode.isDisabled().evaluate(event, context)) {
                payload.remove();
                return;
            }

            WindupWildcardImportResolver.setContext(event.getGraphContext());
            try {
                TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
                GraphContext graphContext = event.getGraphContext();
                WindupConfigurationModel configuration = new GraphService<>(graphContext,
                        WindupConfigurationModel.class)
                        .getUnique();

                String filepath = payload.getFilePath();
                filepath = Paths.get(filepath).toAbsolutePath().toString();

                String classFilePath;
                String inputDir = getInputPathForSource(configuration, filepath);
                if (inputDir != null) {
                    classFilePath = filepath.substring(inputDir.length() + 1);
                } else {
                    classFilePath = payload.getPrettyPathWithinProject();
                }
                String qualifiedName = classFilePath.replace(File.separatorChar, '.').substring(0,
                        classFilePath.length() - JAVA_SUFFIX_LEN);

                String packageName = "";
                if (qualifiedName.contains("."))
                    packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));

                if (packageName.startsWith("src.main.java.")) {
                    packageName = packageName.substring("src.main.java.".length());
                }

                // make sure we mark this as a Java file
                technologyTagService.addTagToFileModel(payload, TECH_TAG, TECH_TAG_LEVEL);
                payload.setPackageName(packageName);
                try (FileInputStream fis = new FileInputStream(payload.getFilePath())) {
                    addParsedClassToFile(event, context, payload, fis);
                } catch (FileNotFoundException e) {
                    throw new WindupException("File in " + payload.getFilePath() + " was not found.", e);
                } catch (IOException e) {
                    throw new WindupException("IOException thrown when parsing file located in "
                            + payload.getFilePath(), e);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Could not parse java file: " + payload.getFilePath() + " due to: " + e.getMessage(), e);
                    ClassificationService classificationService = new ClassificationService(graphContext);
                    classificationService.attachClassification(event, context, payload, UNPARSEABLE_JAVA_CLASSIFICATION, UNPARSEABLE_JAVA_DESCRIPTION);
                    payload.setParseError(UNPARSEABLE_JAVA_CLASSIFICATION + ": " + e.getMessage());
                    return;
                }
            } finally {
                WindupWildcardImportResolver.setContext(null);
            }
        }

        private void addParsedClassToFile(GraphRewrite event, EvaluationContext context, JavaSourceFileModel sourceFileModel, FileInputStream fis) {
            JavaSource<?> javaSource;
            try {
                javaSource = Roaster.parse(JavaSource.class, fis);
            } catch (ParserException e) {
                ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                classificationService.attachClassification(event, context, sourceFileModel, UNPARSEABLE_JAVA_CLASSIFICATION, UNPARSEABLE_JAVA_DESCRIPTION);
                sourceFileModel.setParseError(UNPARSEABLE_JAVA_CLASSIFICATION + ": " + e.getMessage());
                return;
            }

            String packageName = javaSource.getPackage();
            // set the package name to the parsed value
            sourceFileModel.setPackageName(packageName);

            // Set the root path of this source file (if possible). As this could be coming from user-provided source, it
            // is possible that the path will not match the package name. In this case, we will likely end up with a null
            // root path.
            Path rootSourcePath = PathUtil.getRootFolderForSource(sourceFileModel.asFile().toPath(), packageName);
            if (rootSourcePath != null) {
                FileModel rootSourceFileModel = new FileService(event.getGraphContext()).createByFilePath(rootSourcePath.toString());
                sourceFileModel.setRootSourceFolder(rootSourceFileModel);
            }

            String qualifiedName = javaSource.getQualifiedName();

            String simpleName = qualifiedName;
            if (packageName != null && !packageName.isEmpty() && simpleName != null) {
                simpleName = simpleName.substring(packageName.length() + 1);
            }

            JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
            JavaClassModel javaClassModel = javaClassService.create(qualifiedName);
            javaClassModel.setOriginalSource(sourceFileModel);
            javaClassModel.setSimpleName(simpleName);
            javaClassModel.setPackageName(packageName);
            javaClassModel.setQualifiedName(qualifiedName);
            javaClassModel.setClassFile(sourceFileModel);
            javaClassModel.setPublic(javaSource.isPublic());
            javaClassModel.setInterface(javaSource.isInterface());

            if (javaSource instanceof InterfaceCapable) {
                InterfaceCapable interfaceCapable = (InterfaceCapable) javaSource;
                List<String> interfaceNames = interfaceCapable.getInterfaces();
                if (interfaceNames != null) {
                    for (String iface : interfaceNames) {
                        JavaClassModel interfaceModel = javaClassService.getOrCreatePhantom(iface);
                        javaClassService.addInterface(javaClassModel, interfaceModel);
                    }
                }
            }

            if (!javaSource.isInterface() && javaSource instanceof Extendable) {
                Extendable<?> extendable = (Extendable<?>) javaSource;
                String superclassName = extendable.getSuperType();
                if (!Strings.isNullOrEmpty(superclassName))
                    javaClassModel.setExtends(javaClassService.getOrCreatePhantom(superclassName));
            }

            sourceFileModel.addJavaClass(javaClassModel);
        }

        @Override
        public String toString() {
            return "AttachJavaSourceInformationToGraph";
        }
    }
}
