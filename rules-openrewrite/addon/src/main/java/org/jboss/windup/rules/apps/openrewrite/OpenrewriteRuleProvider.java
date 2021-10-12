package org.jboss.windup.rules.apps.openrewrite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.config.Environment;
import org.openrewrite.config.ResourceLoader;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.java.Java8Parser;
import org.openrewrite.java.tree.J;

/**
 * Runs Openrewrite on the Windup's input.
 */
@RuleMetadata(phase = InitialAnalysisPhase.class)
public class OpenrewriteRuleProvider extends AbstractRuleProvider
{

    public OpenrewriteRuleProvider()
    {
        super(MetadataBuilder.forProvider(OpenrewriteRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new OpenrewriteOperation());
    }

    private class OpenrewriteOperation extends GraphOperation
    {

        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            if (!isOpenrewriteEnabled(event))
                return;

            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            for (FileModel input : configuration.getInputPaths())
            {
                String inputFilePath = input.getFilePath();


                try
                {
                    //Load openrewrite environment
                    ClassLoader classLoader = getClass().getClassLoader();
                    File rewriteYml = new File(classLoader.getResource("rewrite.yml").getFile());
                    try(InputStream rewriteInputStream = new FileInputStream(rewriteYml)) {
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


                        Recipe recipe = env.activateRecipes("org.konveyor.tackle.JavaxToJakarta");


                        Java8Parser parser = new Java8Parser.Builder().build();
                        Path homePath = Paths.get("~");
                        Path inputPath = Paths.get(new URI(inputFilePath));
                        List<J.CompilationUnit> fileList =  parser.parse(inputPath, homePath , new InMemoryExecutionContext());

                        recipe.run(fileList);
                    }
                }
                catch (Exception e)
                {
                    throw new WindupException("Failed to run openrewrite due to: " + e.getMessage());
                }
            }
        }



        private boolean isOpenrewriteEnabled(GraphRewrite event)
        {
            Boolean enableOpenrewrite = (Boolean) event.getGraphContext().getOptionMap().getOrDefault(EnableOpenrewriteOption.NAME, Boolean.FALSE);
            Boolean enableSourceMode = (Boolean) event.getGraphContext().getOptionMap().getOrDefault(SourceModeOption.NAME, Boolean.FALSE);
            if (enableOpenrewrite && enableSourceMode)
                return true;
            else
                return false;
        }
    }
}
