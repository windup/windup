package org.jboss.windup.rules.apps.condition;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.files.condition.FileContent;
import org.jboss.windup.rules.files.condition.ToFileModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Tests the toFileModel condition
 */
@RunWith(Arquillian.class)
public class ToFileModelTest {
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;
    @Inject
    private ToFileModelTestRuleProvider provider;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
        return archive;
    }

    @Test
    public void testToFileModelTransformation() throws Exception {
        try (GraphContext context = factory.create(true)) {
            Path inputPath = Paths.get("src/test/resources/");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                    + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            Predicate<RuleProvider> predicate = new NotPredicate(new RuleProviderPhasePredicate(ReportGenerationPhase.class,
                    MigrationRulesPhase.class));

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setRuleProviderFilter(predicate)
                    .setGraphContext(context);
            windupConfiguration.addInputPath(inputPath);
            windupConfiguration.setOutputDirectory(outputPath);
            windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
            processor.execute(windupConfiguration);

            Assert.assertEquals(2, provider.count);
        }
    }

    @Singleton
    public static class ToFileModelTestRuleProvider extends AbstractRuleProvider {
        public int count = 0;


        public ToFileModelTestRuleProvider() {
            super(MetadataBuilder.forProvider(ToFileModelTestRuleProvider.class)
                    .setPhase(PostMigrationRulesPhase.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {

            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(ToFileModel.withWrappedCondition((FileContent) FileContent.matches("file {*}.").inFileNamed("{*}.txt")))
                    .perform(new AbstractIterationOperation<FileModel>() {

                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, FileModel payload) {
                            count++;
                        }
                    });
        }
        // @formatter:on
    }

}
