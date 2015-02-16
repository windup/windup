package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysis;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.VariableResolvingASTVisitor;
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
public class AnalyzeJavaFilesRuleProvider extends WindupRuleProvider
{

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return InitialAnalysis.class;
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
                    throw new WindupException("Failed to get source for file: " + payload.getFilePath()
                                + " due to: "
                                + e.getMessage(), e);
                }
                parser.setKind(ASTParser.K_COMPILATION_UNIT);

                ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.parseFile");
                final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.parseFile");

                ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.visitorConstruct");
                VariableResolvingASTVisitor visitor = new VariableResolvingASTVisitor(event.getGraphContext());
                ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.visitorConstruct");

                ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.visitorInit");
                visitor.init(cu, payload);
                ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.visitorInit");

                ExecutionStatistics.get().begin("AnalyzeJavaFilesRuleProvider.accept");
                cu.accept(visitor);
                ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.accept");
            }
            finally
            {
                ExecutionStatistics.get().end("AnalyzeJavaFilesRuleProvider.analyzeFile");
            }
        }

        @Override
        public String toString()
        {
            return "ParseJavaSource";
        }
    }
}
