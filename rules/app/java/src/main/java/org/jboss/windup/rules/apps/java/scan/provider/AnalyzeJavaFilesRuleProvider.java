package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import java.io.IOException;

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
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.VariableResolvingASTVisitor;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class AnalyzeJavaFilesRuleProvider extends WindupRuleProvider
{

    @Inject
    private VariableResolvingASTVisitor variableResolvingASTVisitor;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.MIGRATION_RULES;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder javaSourceCanBeLocated = Query.find(JavaSourceFileModel.class);

        return ConfigurationBuilder.begin()
            .addRule()
            .when(javaSourceCanBeLocated)
            .perform(
                Iteration.over()
                .perform(
                    new FireASTTypeNameEventsIterationOperator(JavaSourceFileModel.class)
                )
                .endIteration()
            );
    }
    // @formatter:on

    private final class FireASTTypeNameEventsIterationOperator extends AbstractIterationOperation<JavaSourceFileModel>
    {
        private FireASTTypeNameEventsIterationOperator(Class<JavaSourceFileModel> clazz, String variableName)
        {
            super(clazz, variableName);
        }
        
        private FireASTTypeNameEventsIterationOperator(Class<JavaSourceFileModel> clazz)
        {
            super(clazz);
        }

        public void perform(GraphRewrite event, EvaluationContext context, JavaSourceFileModel payload)
        {
            ASTParser parser = ASTParser.newParser(AST.JLS3);
            parser.setBindingsRecovery(true);
            parser.setResolveBindings(true);
            File sourceFile = payload.asFile();
            try
            {
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
}
