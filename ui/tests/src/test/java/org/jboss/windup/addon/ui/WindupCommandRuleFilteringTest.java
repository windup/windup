package org.jboss.windup.addon.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.exec.configuration.options.ExcludeTagsOption;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.SourceOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.ui.WindupCommand;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class WindupCommandRuleFilteringTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.ui:windup-ui"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML()
                .addAsResource(WindupCommandTest.class.getResource("/test.jar"), "/test.jar");
    }

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private Tag1SourceGlassfishRuleProvider tag1SourceGlassfishRuleProvider;

    @Inject
    private Tag1SourceGlassfishTargetFooRuleProvider tag1SourceGlassfishTargetFooRuleProvider;

    @Inject
    private Tag2SourceGlassfishTargetJBossRuleProvider tag2SourceGlassfishTargetJBossRuleProvider;

    @Inject
    private Tag3SourceOrionServerRuleProvider tag3SourceOrionServerRuleProvider;

    @Before
    public void beforeTest() {
        if (System.getProperty("forge.home") == null) {
            String defaultForgeHomePath = Paths.get(OperatingSystemUtils.getTempDirectory().getAbsolutePath())
                    .resolve("Windup")
                    .resolve("fakeforgehome_" + RandomStringUtils.randomAlphanumeric(6)).toString();
            System.setProperty("forge.home", defaultForgeHomePath);
        }
    }

    @Test
    public void testRuleFilteringSourceGlassfish() throws Exception {
        setupAndRun(null, null, Collections.singleton("glassfish"), Collections.singleton("jboss"));
        Assert.assertTrue(this.tag1SourceGlassfishRuleProvider.executed);
        Assert.assertFalse(this.tag1SourceGlassfishTargetFooRuleProvider.executed);
        Assert.assertTrue(this.tag2SourceGlassfishTargetJBossRuleProvider.executed);
        Assert.assertFalse(this.tag3SourceOrionServerRuleProvider.executed);
    }

    @Test
    public void testRuleFilteringSourceGlassfishTargetJBoss() throws Exception {
        setupAndRun(null, null, Collections.singleton("glassfish"), Collections.singleton("jboss"));
        Assert.assertTrue(this.tag1SourceGlassfishRuleProvider.executed);
        Assert.assertFalse(this.tag1SourceGlassfishTargetFooRuleProvider.executed);
        Assert.assertTrue(this.tag2SourceGlassfishTargetJBossRuleProvider.executed);
        Assert.assertFalse(this.tag3SourceOrionServerRuleProvider.executed);
    }

    @Test
    public void testRuleFilteringExcludeTag3() throws Exception {
        setupAndRun(null, Collections.singleton("tag3"), Collections.singleton("glassfish"), Collections.singleton("foo"));
        Assert.assertFalse(this.tag1SourceGlassfishRuleProvider.executed);
        Assert.assertTrue(this.tag1SourceGlassfishTargetFooRuleProvider.executed);
        Assert.assertFalse(this.tag2SourceGlassfishTargetJBossRuleProvider.executed);
        Assert.assertFalse(this.tag3SourceOrionServerRuleProvider.executed);
    }

    @Test
    public void testRuleFilteringIncludeTag2() throws Exception {
        setupAndRun(Collections.singleton("tag2"), null, null, Collections.singleton("jboss"));
        Assert.assertFalse(this.tag1SourceGlassfishRuleProvider.executed);
        Assert.assertFalse(this.tag1SourceGlassfishTargetFooRuleProvider.executed);
        Assert.assertTrue(this.tag2SourceGlassfishTargetJBossRuleProvider.executed);
        Assert.assertFalse(this.tag3SourceOrionServerRuleProvider.executed);
    }

    private void setupAndRun(Set<String> includeTags, Set<String> excludeTags, Set<String> sources, Set<String> targets) throws Exception {
        Assert.assertNotNull(uiTestHarness);

        this.tag1SourceGlassfishRuleProvider.executed = false;
        this.tag1SourceGlassfishTargetFooRuleProvider.executed = false;
        this.tag2SourceGlassfishTargetJBossRuleProvider.executed = false;
        this.tag3SourceOrionServerRuleProvider.executed = false;

        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class)) {
            File outputFile = File.createTempFile("windupwizardtest", ".jar");
            outputFile.deleteOnExit();
            File inputFile = File.createTempFile("windupwizardtest", ".jar");
            inputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar")) {
                try (OutputStream oStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(inputFile.getAbsoluteFile() + "_output");
            try {
                reportPath.mkdirs();

                controller.initialize();
                Assert.assertTrue(controller.isEnabled());
                //controller.setValueFor(InputPathOption.NAME, inputFile);
                controller.setValueFor(InputPathOption.NAME, Collections.singletonList(inputFile)); // FORGE-2524
                Object valueFor = controller.getValueFor(InputPathOption.NAME);///

                controller.setValueFor(OutputPathOption.NAME, outputFile);

                if (includeTags != null) {
                    controller.setValueFor(IncludeTagsOption.NAME, includeTags);
                }

                if (excludeTags != null) {
                    controller.setValueFor(ExcludeTagsOption.NAME, excludeTags);
                }

                if (sources != null) {
                    controller.setValueFor(SourceOption.NAME, sources);
                }

                if (targets != null) {
                    controller.setValueFor(TargetOption.NAME, targets);
                }
                Assert.assertTrue(controller.canExecute());

                Result result = controller.execute();
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);
            } finally {
                inputFile.delete();
                FileUtils.deleteQuietly(reportPath);
            }
        }
    }

    @Singleton
    public static class Tag1SourceGlassfishRuleProvider extends AbstractRuleProvider {
        private boolean executed = false;

        public Tag1SourceGlassfishRuleProvider() {
            super(MetadataBuilder.forProvider(Tag1SourceGlassfishRuleProvider.class)
                    .addTag("tag1")
                    .addSourceTechnology(new TechnologyReference("glassfish", "[1.0,)"))
                    .addTargetTechnology(new TechnologyReference("jboss", "[1.0,)")));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin().addRule()
                    .perform(
                            new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    executed = true;
                                }
                            }
                    );
        }
    }

    @Singleton
    public static class Tag1SourceGlassfishTargetFooRuleProvider extends AbstractRuleProvider {
        private boolean executed = false;

        public Tag1SourceGlassfishTargetFooRuleProvider() {
            super(MetadataBuilder.forProvider(Tag1SourceGlassfishTargetFooRuleProvider.class)
                    .addTag("tag1")
                    .addSourceTechnology(new TechnologyReference("glassfish", "[1.0,)"))
                    .addTargetTechnology(new TechnologyReference("foo", "[1.0,)")));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin().addRule()
                    .perform(
                            new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    executed = true;
                                }
                            }
                    );
        }
    }

    @Singleton
    public static class Tag2SourceGlassfishTargetJBossRuleProvider extends AbstractRuleProvider {
        private boolean executed = false;

        public Tag2SourceGlassfishTargetJBossRuleProvider() {
            super(MetadataBuilder.forProvider(Tag2SourceGlassfishTargetJBossRuleProvider.class)
                    .addTag("tag2")
                    .addSourceTechnology(new TechnologyReference("glassfish", "[1.0,)"))
                    .addTargetTechnology(new TechnologyReference("jboss", "[1.0,)")));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin().addRule()
                    .perform(
                            new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    executed = true;
                                }
                            }
                    );
        }
    }

    @Singleton
    public static class Tag3SourceOrionServerRuleProvider extends AbstractRuleProvider {
        private boolean executed = false;

        public Tag3SourceOrionServerRuleProvider() {
            super(MetadataBuilder.forProvider(Tag3SourceOrionServerRuleProvider.class)
                    .addTag("tag3")
                    .addSourceTechnology(new TechnologyReference("orion", "[1.0,)"))
                    .addTargetTechnology(new TechnologyReference("foo", "[1.0,)")));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin().addRule()
                    .perform(
                            new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    executed = true;
                                }
                            }
                    );
        }
    }
}
