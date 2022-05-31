package org.jboss.windup.rules.java;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.rules.apps.java.condition.SourceMode;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Arquillian.class)
public class SourceModeTest {
    private static int enabledCount = 0;
    private static int disabledCount = 0;
    @Inject
    SourceModeTestRuleProvider provider;
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Test
    public void testSourceModePositive() throws IOException, InstantiationException, IllegalAccessException {
        final Path inputDir = Paths.get("src/test/resources/org/jboss/windup/rules/java");
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                "windup_" + RandomStringUtils.randomAlphanumeric(6));

        try (GraphContext context = factory.create(getDefaultPath(), true)) {
            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                    SourceModeTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(inputDir);
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            Assert.assertEquals(1, provider.getEnabledCount());
            Assert.assertEquals(0, provider.getDisabledCount());
        }
    }

    @Test
    public void testSourceModeNegative() throws IOException, InstantiationException, IllegalAccessException {
        final Path inputDir = Paths.get("src/test/resources/org/jboss/windup/rules/java");
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                "windup_" + RandomStringUtils.randomAlphanumeric(6));

        try (GraphContext context = factory.create(getDefaultPath(), true)) {
            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                    SourceModeTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(inputDir);
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(SourceModeOption.NAME, false);

            processor.execute(processorConfig);

            Assert.assertEquals(0, provider.getEnabledCount());
            Assert.assertEquals(1, provider.getDisabledCount());
        }
    }

    @Test
    public void testSourceModeDefault() throws IOException, InstantiationException, IllegalAccessException {
        final Path inputDir = Paths.get("src/test/resources/org/jboss/windup/rules/java");
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                "windup_" + RandomStringUtils.randomAlphanumeric(6));

        try (GraphContext context = factory.create(getDefaultPath(), true)) {
            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                    SourceModeTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(inputDir);
            processorConfig.setOutputDirectory(outputPath);

            processor.execute(processorConfig);

            Assert.assertEquals(0, provider.getEnabledCount());
            Assert.assertEquals(1, provider.getDisabledCount());
        }
    }

    private Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupgraph_sourcemodetest" + RandomStringUtils.randomAlphanumeric(6));
    }

    @After
    public void after() {
        enabledCount = 0;
        disabledCount = 0;
    }

    @Singleton
    public static class SourceModeTestRuleProvider extends AbstractRuleProvider {
        public SourceModeTestRuleProvider() {
            super(MetadataBuilder.forProvider(SourceModeTestRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule().when(
                            SourceMode.isEnabled()
                    ).perform(new GraphOperation() {
                                  @Override
                                  public void perform(GraphRewrite event, EvaluationContext context) {
                                      enabledCount++;
                                  }
                              }
                    )

                    .addRule().when(
                            SourceMode.isDisabled()
                    ).perform(new GraphOperation() {
                                  @Override
                                  public void perform(GraphRewrite event, EvaluationContext context) {
                                      disabledCount++;
                                  }
                              }
                    );
        }
        // @formatter:on

        public int getEnabledCount() {
            return enabledCount;
        }

        public int getDisabledCount() {
            return disabledCount;
        }
    }
}
