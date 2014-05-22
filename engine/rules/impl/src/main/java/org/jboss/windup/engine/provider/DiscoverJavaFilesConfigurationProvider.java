package org.jboss.windup.engine.provider;

import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.operation.ruleelement.AbstractIterationOperator;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.FileResourceModel;
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
        return generateDependencies(DecompileArchivesConfigurationProvider.class);
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
                                            .perform(
                                                        new AbstractIterationOperator<FileResourceModel>(
                                                                    FileResourceModel.class, "javaSourceFile")
                                                        {
                                                            public void perform(GraphRewrite event,
                                                                        EvaluationContext context,
                                                                        FileResourceModel payload)
                                                            {
                                                                // ASTParser parser = ASTParser.newParser(AST.JLS3);
                                                                // parser.setBindingsRecovery(true);
                                                                // parser.setResolveBindings(true);
                                                                // try
                                                                // {
                                                                // File sourceFile = payload.asFile();
                                                                // parser.setSource(FileUtils.readFileToString(
                                                                // sourceFile).toCharArray());
                                                                // }
                                                                // catch (IOException e)
                                                                // {
                                                                // throw new WindupException(
                                                                // "Failed to get source for file: "
                                                                // + payload.getFilePath()
                                                                // + " due to: "
                                                                // + e.getMessage(), e);
                                                                // }
                                                                // parser.setKind(ASTParser.K_COMPILATION_UNIT);
                                                                // \
                                                                // final CompilationUnit cu = (CompilationUnit) parser
                                                                // .createAST(null);
                                                                // cu.accept(new JavaASTVariableResolvingVisitor(cu,
                                                                // javaClassDao, windupContext));
                                                            }
                                                        }
                                            ).endIteration()
                    );

    }
}
