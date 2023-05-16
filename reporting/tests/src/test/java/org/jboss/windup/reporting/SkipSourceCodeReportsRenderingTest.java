package org.jboss.windup.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.LegacyReportsRenderingOption;
import org.jboss.windup.config.SkipSourceCodeReportsRenderingOption;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Test for skipping source code reports rendering.
 */
@RunWith(Arquillian.class)
public class SkipSourceCodeReportsRenderingTest {

    public static final String HELLO_WORLD_JAVA_HTML = "HelloWorld_java.html";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    private final Path inputPath = Paths.get("src/test/resources/skipSourceCodeReports");
    @Test
    public void testSourceCodeReportsNotGenerated() {
        try (final GraphContext context = contextFactory.create(true)) {
            final WindupConfiguration configuration = getWindupConfiguration(context, "WindupReportWithoutSourceCode");
            processor.execute(configuration.setOptionValue(SkipSourceCodeReportsRenderingOption.NAME, true));
            processor.execute(configuration.setOptionValue(LegacyReportsRenderingOption.NAME, true));
            // no SourceReportModel instances in the graph
            final SourceReportService sourceReportService = new SourceReportService(context);
            Assert.assertTrue("SourceReportModel must not be created", sourceReportService.findAll().isEmpty());
            // check there's no source code file for HelloWorld.java in the report directory
            Assert.assertFalse(String.format("File %s must not be generated", HELLO_WORLD_JAVA_HTML), findHelloWorldSourceCodeReportFile(configuration));
        } catch (Exception ex) {
            throw new WindupException(ex.getMessage(), ex);
        }
    }

    @Test
    public void testSourceCodeReportsStillGenerated() {
        try (final GraphContext context = contextFactory.create(true)) {
            final WindupConfiguration configuration = getWindupConfiguration(context, "WindupReportWithSourceCode");
            processor.execute(configuration.setOptionValue(LegacyReportsRenderingOption.NAME, true));
            processor.execute(configuration);
            // no SourceReportModel instances in the graph
            final SourceReportService sourceReportService = new SourceReportService(context);
            Assert.assertFalse("SourceReportModel must have been created", sourceReportService.findAll().isEmpty());
            // check there's the source code file for HelloWorld.java in the report directory
            Assert.assertTrue(String.format("File %s must be generated", HELLO_WORLD_JAVA_HTML), findHelloWorldSourceCodeReportFile(configuration));
        } catch (Exception ex) {
            throw new WindupException(ex.getMessage(), ex);
        }
    }

    private boolean findHelloWorldSourceCodeReportFile(WindupConfiguration configuration) {
        try (Stream<Path> stream = Files.walk(configuration.getOutputDirectory(), Integer.MAX_VALUE)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .anyMatch(HELLO_WORLD_JAVA_HTML::equals);
        } catch (IOException e) {
            throw new WindupException(e.getMessage(), e);
        }
    }

    private WindupConfiguration getWindupConfiguration(GraphContext context, String outputFolder) {
        final WindupConfiguration configuration = new WindupConfiguration();
        configuration.setGraphContext(context);
        configuration.addInputPath(inputPath);
        configuration.setOutputDirectory(Paths.get("target", outputFolder));
        return configuration;
    }

    @RuleMetadata
    public static class TestRule extends AbstractRuleProvider {
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(JavaClass.references("testclasses.HelloWorld"))
                    .perform(Hint.titled("Test Rule").withText("This is a test message").withEffort(1));
        }
    }
}
