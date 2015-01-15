package org.jboss.windup.rules.java;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
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

@RunWith(Arquillian.class)
public class SourceModeTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(SourceModeTestRuleProvider.class)
                    .addClass(SourceModeTest.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    SourceModeTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testSourceModePositive() throws IOException, InstantiationException, IllegalAccessException
    {
        final Path inputDir = Paths.get("src/test/resources/org/jboss/windup/rules/java");
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));

        try (GraphContext context = factory.create(getDefaultPath()))
        {
            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                        SourceModeTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.setInputPath(inputDir);
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            Assert.assertEquals(1, provider.getEnabledCount());
            Assert.assertEquals(0, provider.getDisabledCount());
        }
    }

    @Test
    public void testSourceModeNegative() throws IOException, InstantiationException, IllegalAccessException
    {
        final Path inputDir = Paths.get("src/test/resources/org/jboss/windup/rules/java");
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));

        try (GraphContext context = factory.create(getDefaultPath()))
        {
            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                        SourceModeTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.setInputPath(inputDir);
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(SourceModeOption.NAME, false);

            processor.execute(processorConfig);

            Assert.assertEquals(0, provider.getEnabledCount());
            Assert.assertEquals(1, provider.getDisabledCount());
        }
    }

    @Test
    public void testSourceModeDefault() throws IOException, InstantiationException, IllegalAccessException
    {
        final Path inputDir = Paths.get("src/test/resources/org/jboss/windup/rules/java");
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));

        try (GraphContext context = factory.create(getDefaultPath()))
        {
            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                        SourceModeTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.setInputPath(inputDir);
            processorConfig.setOutputDirectory(outputPath);

            processor.execute(processorConfig);

            Assert.assertEquals(0, provider.getEnabledCount());
            Assert.assertEquals(1, provider.getDisabledCount());
        }
    }

    private Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                    .resolve("windupgraph_sourcemodetest" + RandomStringUtils.randomAlphanumeric(6));
    }

    private static int enabledCount = 0;
    private static int disabledCount = 0;

    @After
    public void after()
    {
        enabledCount = 0;
        disabledCount = 0;
    }

    @Singleton
    public static class SourceModeTestRuleProvider extends WindupRuleProvider
    {

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
            .addRule().when(
                SourceMode.isEnabled()
            ).perform(new GraphOperation()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context)
                    {
                        enabledCount++;
                    }
                }
            )
               
            .addRule().when(
                SourceMode.isDisabled()
            ).perform(new GraphOperation()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context)
                    {
                        disabledCount++;
                    }
                }
            );
        }
        // @formatter:on

        public int getEnabledCount()
        {
            return enabledCount;
        }

        public int getDisabledCount()
        {
            return disabledCount;
        }
    }
}
