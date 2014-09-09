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
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.VariableResolvingASTVisitor;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scan the Java Source code files and store the used type information from them.
 * 
 */
public class AnalyzeJavaFilesRuleProvider extends WindupRuleProvider
{

    @Inject
    private WindupConfigurationService windupConfigurationService;

    @Inject
    private VariableResolvingASTVisitor variableResolvingASTVisitor;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.INITIAL_ANALYSIS;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder javaSourceAvailable = Query.find(JavaSourceFileModel.class);
        
        return ConfigurationBuilder.begin()
            .addRule()
            .when(javaSourceAvailable)
            .perform(new ParseSourceOperation());

    }
    // @formatter:on

    private final class ParseSourceOperation extends AbstractIterationOperation<JavaSourceFileModel>
    {
        public void perform(GraphRewrite event, EvaluationContext context, JavaSourceFileModel payload)
        {
            if (!windupConfigurationService.shouldScanPackage(payload.getPackageName()))
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
            final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            variableResolvingASTVisitor.init(cu, payload);
            cu.accept(variableResolvingASTVisitor);
        }
    }
}
