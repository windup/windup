package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
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

                ASTParser parser = ASTParser.newParser(AST.JLS8);
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
                List<String> librariesPaths = new ArrayList<String>();
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
                parser.setEnvironment(librariesPaths.toArray(new String[librariesPaths.size()]),
                            javaFilePaths.toArray(new String[javaFilePaths.size()]), null, true);
                parser.setBindingsRecovery(false);
                parser.setResolveBindings(true);
                parser.setUnitName(payload.getFileName());
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
