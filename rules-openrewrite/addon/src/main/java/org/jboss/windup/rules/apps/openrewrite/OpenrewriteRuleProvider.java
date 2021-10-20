package org.jboss.windup.rules.apps.openrewrite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.condition.SourceMode;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.openrewrite.*;
import org.openrewrite.config.Environment;
import org.openrewrite.config.ResourceLoader;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;

import static java.util.stream.Collectors.toList;

/**
 * Runs Openrewrite on the Windup's input.
 */
@RuleMetadata(phase = InitialAnalysisPhase.class)
public class OpenrewriteRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logging.get(OpenrewriteRuleProvider.class);

    public OpenrewriteRuleProvider()
    {
        super(MetadataBuilder.forProvider(OpenrewriteRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(SourceMode.isEnabled(),
                            new GraphCondition() {
                                @Override
                                public boolean evaluate(GraphRewrite event, EvaluationContext context) {
                                    return (Boolean) event.getGraphContext().getOptionMap().getOrDefault(EnableOpenrewriteOption.NAME, Boolean.FALSE);
                                }
                            }
                    )
                    .perform(new OpenrewriteOperation());
    }

    private class OpenrewriteOperation extends GraphOperation
    {

        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            for (FileModel input : configuration.getInputPaths())
            {
                String inputFilePath = input.getFilePath();

                try
                {
                    //Load openrewrite environment
                    ClassLoader classLoader = getClass().getClassLoader();
                    File rewriteYml = new File(classLoader.getResource("rewrite.yml").getFile());
                    try(InputStream rewriteInputStream = classLoader.getResourceAsStream("rewrite.yml")) {

                        ResourceLoader rewriteYmlLoader = new YamlResourceLoader(
                                rewriteInputStream,
                                // wouldn't have to exist on disk necessarily (just used in logging)
                                rewriteYml.toURI(),
                                System.getProperties()
                        );

                        //String... classpath = null;

                        Environment env = Environment.builder()
                                .load(rewriteYmlLoader) // can be called more than once for multiple files
                                //.scanRuntimeClasspath() // classpath scans for META-INF/rewrite/*.yml
                                .scanUserHome() // looks for `~/.rewrite/rewrite.yml
                                .build();

                        Recipe recipe = env.activateAll();

                        JavaParser javaParser = WindupJava8Parser.builder().build();
                        // TODO change this temporary solution (just for running the test) with something that
                        // retrieves all the ".java" files available within the input path
                        Path sourceRoot = Paths.get(inputFilePath);
                        List<Path> inputPaths = new ArrayList<>();
                        try {
                            inputPaths =  Files.walk(sourceRoot)
                                    .filter(f -> !Files.isDirectory(f) && f.toFile().getName().endsWith(".java"))
                                    .collect(toList());
                        } catch (IOException e) {
                            throw new Exception("Unable to list Java source files", e);
                        }
                        ExecutionContext ctx = new InMemoryExecutionContext();
                        List<J.CompilationUnit> fileList =  javaParser.parse(inputPaths, null, ctx);
                        recipe.validateAll(ctx);

                        List<Result> results = recipe.run(fileList, ctx);

                        LOG.info(String.format("%d files changed", results.size()));
                        results.forEach(result -> LOG.info(result.toString()));
                        // TODO should results be stored into the graph in some way?
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new WindupException("Failed to run openrewrite due to: " + e.getMessage());
                }
            }
        }
    }
}
