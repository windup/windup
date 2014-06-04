package org.jboss.windup.engine.provider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.GraphSubset;
import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.operation.ruleelement.AbstractIterationOperator;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.engine.util.exception.WindupException;
import org.jboss.windup.engine.visitor.inspector.JavaASTVariableResolvingVisitor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.jboss.windup.graph.service.GraphService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class DiscoverJavaFilesConfigurationProvider extends WindupConfigurationProvider
{
    @Inject
    private WindupContext windupContext;

    @Inject
    private JavaClassDao javaClassDao;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupConfigurationProvider>> getDependencies()
    {
        return generateDependencies(IndexClassFilesConfigurationProvider.class);
    }

    private CompilationUnit parseCompilationUnit(FileResourceModel fileResourceModel)
    {
        ASTParser parser = ASTParser
                    .newParser(AST.JLS3);
        parser.setBindingsRecovery(true);
        parser.setResolveBindings(true);
        try
        {
            File sourceFile = fileResourceModel.asFile();
            parser.setSource(FileUtils.readFileToString(sourceFile).toCharArray());
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to get source for file: " + fileResourceModel.getFilePath() + " due to: "
                        + e.getMessage(), e);
        }
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        return (CompilationUnit) parser.createAST(null);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(
                                GraphSearchConditionBuilder
                                            .create("inputConfigurations")
                                            .ofType(WindupConfigurationModel.class)
                                            .withProperty(WindupConfigurationModel.PROPERTY_SOURCE_MODE, true)
                    )
                    .perform(
                                Iteration.over("inputConfigurations")
                                            .var("configuration")
                                            .perform(
                                                        GraphSubset.evaluate(
                                                                    ConfigurationBuilder
                                                                                .begin()
                                                                                .addRule()
                                                                                .when(
                                                                                            GraphSearchConditionBuilder
                                                                                                        .create("javaSourceFiles")
                                                                                                        .ofType(FileResourceModel.class)
                                                                                                        .withProperty(
                                                                                                                    FileResourceModel.PROPERTY_IS_DIRECTORY,
                                                                                                                    false)
                                                                                                        .withProperty(
                                                                                                                    FileResourceModel.PROPERTY_FILE_PATH,
                                                                                                                    GraphSearchPropertyComparisonType.REGEX,
                                                                                                                    ".*\\.java$")
                                                                                )
                                                                                .perform(
                                                                                            Iteration.over(
                                                                                                        "javaSourceFiles")
                                                                                                        .var(FileResourceModel.class,
                                                                                                                    "javaSourceFile")
                                                                                                        .perform(new AbstractIterationOperator<FileResourceModel>(
                                                                                                                    FileResourceModel.class,
                                                                                                                    "javaSourceFile")
                                                                                                        {
                                                                                                            @SuppressWarnings("unchecked")
                                                                                                            @Override
                                                                                                            public void perform(
                                                                                                                        GraphRewrite event,
                                                                                                                        EvaluationContext context,
                                                                                                                        FileResourceModel payload)
                                                                                                            {
                                                                                                                SelectionFactory selectionFactory = SelectionFactory
                                                                                                                            .instance(event);
                                                                                                                WindupConfigurationModel windupCfg = selectionFactory
                                                                                                                            .getCurrentPayload(
                                                                                                                                        WindupConfigurationModel.class,
                                                                                                                                        "configuration");
                                                                                                                indexJavaFile(
                                                                                                                            event.getGraphContext(),
                                                                                                                            payload,
                                                                                                                            windupCfg);
                                                                                                            }

                                                                                                        }
                                                                                                        )
                                                                                                        .endIteration()
                                                                                                        .and(
                                                                                                                    Iteration.over(
                                                                                                                                "javaSourceFiles")
                                                                                                                                .var(FileResourceModel.class,
                                                                                                                                            "javaSourceFile")
                                                                                                                                .perform(
                                                                                                                                            new AbstractIterationOperator<FileResourceModel>(
                                                                                                                                                        FileResourceModel.class,
                                                                                                                                                        "javaSourceFile")
                                                                                                                                            {
                                                                                                                                                public void perform(
                                                                                                                                                            GraphRewrite event,
                                                                                                                                                            EvaluationContext context,
                                                                                                                                                            FileResourceModel payload)
                                                                                                                                                {
                                                                                                                                                    final CompilationUnit cu = parseCompilationUnit(payload);
                                                                                                                                                    cu.accept(new JavaASTVariableResolvingVisitor(
                                                                                                                                                                cu,
                                                                                                                                                                javaClassDao,
                                                                                                                                                                windupContext));
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                )
                                                                                                                                .endIteration()
                                                                                                        )
                                                                                ))).endIteration());

    }

    private void indexJavaFile(GraphContext graphCtx, FileResourceModel payload, WindupConfigurationModel windupCfg)
    {
        String inputDir = windupCfg.getInputPath();
        inputDir = Paths.get(inputDir).toAbsolutePath().toString();

        String filepath = payload.getFilePath();
        filepath = Paths.get(filepath).toAbsolutePath().toString();

        if (filepath.startsWith(inputDir))
        {
            String classFilePath = filepath.substring(inputDir.length() + 1);
            String qualifiedName = classFilePath.replace(File.separatorChar, '.').substring(0,
                        classFilePath.length() - 5);
            String typeName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1, qualifiedName.length());

            String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));

            GraphService<JavaClassModel> graphService = new GraphService<>(graphCtx,
                        JavaClassModel.class);

            JavaClassModel javaClassModel = graphService.getByUniqueProperty(JavaClassModel.PROPERTY_QUALIFIED_NAME,
                        qualifiedName);
            if (javaClassModel == null)
            {
                javaClassModel = graphCtx.getFramed().addVertex(null, JavaClassModel.class);
                javaClassModel.setClassName(typeName);
                javaClassModel.setPackageName(packageName);
                javaClassModel.setQualifiedName(qualifiedName);
            }
        }
    }

}
