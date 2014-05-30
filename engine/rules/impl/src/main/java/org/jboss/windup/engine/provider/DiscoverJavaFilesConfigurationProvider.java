package org.jboss.windup.engine.provider;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.operation.ruleelement.AbstractIterationOperator;
import org.jboss.windup.engine.util.exception.WindupException;
import org.jboss.windup.engine.visitor.inspector.JavaASTVariableResolvingVisitor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.JavaClassDao;
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
        return generateDependencies(DecompileArchivesConfigurationProvider.class,
                    IndexClassFilesConfigurationProvider.class);
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
                                            .create("javaSourceFiles")
                                            .ofType(FileResourceModel.class)
                                            .withProperty(FileResourceModel.PROPERTY_IS_DIRECTORY, false)
                                            .withProperty(FileResourceModel.PROPERTY_FILE_PATH,
                                                        GraphSearchPropertyComparisonType.REGEX, ".*\\.java$")
                    )
                    .perform(
                                Iteration.over("javaSourceFiles")
                                            .var(FileResourceModel.class, "javaSourceFile")
                                            .perform(new AbstractIterationOperator<FileResourceModel>(
                                                        FileResourceModel.class,
                                                        "javaSourceFile")
                                            {
                                                @SuppressWarnings("unchecked")
                                                @Override
                                                public void perform(GraphRewrite event, EvaluationContext context,
                                                            FileResourceModel payload)
                                                {
                                                    final CompilationUnit cu = parseCompilationUnit(payload);
                                                    String packageName = cu.getPackage().getName().toString();
                                                    for (AbstractTypeDeclaration decl : (List<AbstractTypeDeclaration>) cu
                                                                .types())
                                                    {
                                                        String typeName = decl.getName().toString();
                                                        String qualifiedName = packageName + "." + typeName;
                                                        GraphService<JavaClassModel> graphService = new GraphService<>(
                                                                    event.getGraphContext(), JavaClassModel.class);

                                                        JavaClassModel javaClassModel = graphService
                                                                    .getByUniqueProperty(
                                                                                JavaClassModel.PROPERTY_QUALIFIED_NAME,
                                                                                qualifiedName);
                                                        if (javaClassModel == null)
                                                        {
                                                            javaClassModel = event.getGraphContext().getFramed()
                                                                        .addVertex(null, JavaClassModel.class);
                                                            javaClassModel.setClassName(typeName);
                                                            javaClassModel.setPackageName(packageName);
                                                            javaClassModel.setQualifiedName(qualifiedName);
                                                        }

                                                    }
                                                }
                                            }
                                            )
                                            .endIteration()
                                            .and(
                                                        Iteration.over("javaSourceFiles")
                                                                    .var(FileResourceModel.class, "javaSourceFile")
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
                                                                                                    cu, javaClassDao,
                                                                                                    windupContext));
                                                                                    }
                                                                                }
                                                                    ).endIteration()
                                            )
                    );

    }
}
