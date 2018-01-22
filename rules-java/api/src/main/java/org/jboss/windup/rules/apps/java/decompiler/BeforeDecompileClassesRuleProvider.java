package org.jboss.windup.rules.apps.java.decompiler;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.DecompilationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Predecompilation scan - performance purposes.
 */
@RuleMetadata(phase = DecompilationPhase.class)
public class BeforeDecompileClassesRuleProvider extends AbstractRuleProvider
{
    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
        .addRule()
        .when(Query.fromType(JavaClassFileModel.class)
                .withoutProperty(FileModel.PARSE_ERROR)
        )
        .perform(new ClassFilePreDecompilationScan())
        .addRule()
        .perform(new GraphOperation() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context) {
                Iterable<JavaClassFileModel> allClasses = new GraphService<>(event.getGraphContext(), JavaClassFileModel.class).findAll();
                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                Path fileStatusPath = Paths.get(configurationModel.getOutputPath().getFilePath()).resolve("javaclasses.txt");
                try (FileWriter fw = new FileWriter(fileStatusPath.toFile()))
                {
                    for (JavaClassFileModel javaClassFileModel : allClasses)
                    {
                        JavaClassModel javaClassModel = javaClassFileModel.getJavaClass();
                        String name = javaClassModel == null ? javaClassFileModel.getPrettyPathWithinProject() : javaClassModel.getClassName();
                        fw.write(javaClassFileModel.getPrettyPath());
                        fw.write(",");
                        fw.write(name);
                        fw.write(",");
                        fw.write(""+javaClassFileModel.getSkipDecompilation());
                        fw.write(Util.NL);
                    }
                } catch (IOException e) {
                    throw new WindupException(e);
                }
            }
        });
    }
    // @formatter:on
}
