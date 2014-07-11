package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.util.GraphUtil;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.VariableResolvingASTVisitor;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 */
public class DiscoverJavaFilesRuleProvider extends WindupRuleProvider
{
    @Inject
    private VariableResolvingASTVisitor variableResolvingASTVisitor;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getClassDependencies()
    {
        return generateDependencies(IndexClassFilesRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder javaSourceCanBeLocated = Query
                    .find(FileModel.class)
                    .withProperty(FileModel.PROPERTY_IS_DIRECTORY, false)
                    .withProperty(FileModel.PROPERTY_FILE_PATH, QueryPropertyComparisonType.REGEX, ".*\\.java$")
                    .as("javaSourceFiles");

        ConditionBuilder sourceModeEnabled = Query
                    .find(WindupConfigurationModel.class)
                    .withProperty(WindupConfigurationModel.PROPERTY_SOURCE_MODE, true)
                    .as("inputConfigurations");

        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(javaSourceCanBeLocated.and(sourceModeEnabled))
                    .perform(Iteration
                                .over("javaSourceFiles")
                                .as(FileModel.class, "javaSourceFile")

                                .perform(
                                            new IndexJavaFileIterationOperator(FileModel.class, "javaSourceFile")
                                )
                                .endIteration()

                                .and(
                                            Iteration.over("javaSourceFiles")
                                                        .as(FileModel.class, "javaSourceFile")
                                                        .perform(
                                                                    new FireASTTypeNameEventsIterationOperator(
                                                                                FileModel.class, "javaSourceFile")
                                                        )
                                                        .endIteration()
                                )
                    );
    }

    private final class FireASTTypeNameEventsIterationOperator extends AbstractIterationOperation<FileModel>
    {
        private FireASTTypeNameEventsIterationOperator(Class<FileModel> clazz, String variableName)
        {
            super(clazz, variableName);
        }

        public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
        {
            ASTParser parser = ASTParser.newParser(AST.JLS3);
            parser.setBindingsRecovery(true);
            parser.setResolveBindings(true);
            try
            {
                File sourceFile = payload.asFile();
                parser.setSource(FileUtils.readFileToString(sourceFile).toCharArray());
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to get source for file: " + payload.getFilePath() + " due to: "
                            + e.getMessage(), e);
            }
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            variableResolvingASTVisitor.init(cu, payload);
            cu.accept(variableResolvingASTVisitor);
        }
    }

    private final class IndexJavaFileIterationOperator extends AbstractIterationOperation<FileModel>
    {
        private static final int JAVA_SUFFIX_LEN = 5;

        private IndexJavaFileIterationOperator(Class<FileModel> clazz, String variableName)
        {
            super(clazz, variableName);
        }

        @Override
        public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
        {
            GraphContext graphContext = event.getGraphContext();
            WindupConfigurationModel configuration = new GraphService<>(graphContext, WindupConfigurationModel.class)
                        .getUnique();

            String inputDir = configuration.getInputPath().getFilePath();
            inputDir = Paths.get(inputDir).toAbsolutePath().toString();

            String filepath = payload.getFilePath();
            filepath = Paths.get(filepath).toAbsolutePath().toString();

            if (!filepath.startsWith(inputDir))
            {
                return;
            }

            // make sure we mark this as a Java file
            JavaFileModel javaFileModel = GraphUtil.addTypeToModel(graphContext, payload, JavaFileModel.class);

            String classFilePath = filepath.substring(inputDir.length() + 1);
            String qualifiedName = classFilePath.replace(File.separatorChar, '.').substring(0,
                        classFilePath.length() - JAVA_SUFFIX_LEN);
            String typeName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1, qualifiedName.length());

            String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));

            GraphService<JavaClassModel> graphService = new GraphService<>(graphContext, JavaClassModel.class);

            JavaClassModel javaClassModel = graphService.getUniqueByProperty(JavaClassModel.PROPERTY_QUALIFIED_NAME,
                        qualifiedName);
            if (javaClassModel == null)
            {
                javaClassModel = graphContext.getFramed().addVertex(null, JavaClassModel.class);
                javaClassModel.setClassName(typeName);
                javaClassModel.setPackageName(packageName);
                javaClassModel.setQualifiedName(qualifiedName);
            }
        }

    }
}
