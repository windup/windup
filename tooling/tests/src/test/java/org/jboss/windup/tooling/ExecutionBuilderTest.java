package org.jboss.windup.tooling;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import javax.inject.Inject;
import javax.inject.Singleton;

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
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.options.OnlineModeOption;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Quickfix;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.model.QuickfixType;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.google.common.collect.Iterables;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class ExecutionBuilderTest
{
    @Inject
    private ExecutionBuilder builder;
    @Inject
    private GraphLoader graphLoader;
    @Inject
    private TestProvider testProvider;

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup:windup-tooling"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap
                    .create(AddonArchive.class)
                    .addBeansXML();
    }

    @Test
    public void testExecutionBuilder()
    {
        Assert.assertNotNull(builder);
        Assert.assertNotNull(testProvider);

        Path input = Paths.get("../../test-files/src_example");
        Path output = getDefaultPath();

        ExecutionResults results = executeWindup(input, output, new TestProgressMonitor());
        Assert.assertNotNull(results.getClassifications());
        Assert.assertNotNull(results.getHints());
        Assert.assertTrue(results.getHints().iterator().hasNext());
        checkQuickfixInHints(results.getHints());
        Assert.assertTrue(results.getClassifications().iterator().hasNext());
        Assert.assertTrue(testProvider.sourceMode);
        Assert.assertFalse(testProvider.onlineMode);
    }

    @Test
    public void testExecutionBuilderWithLogging()
    {
        Assert.assertNotNull(builder);
        Assert.assertNotNull(testProvider);

        Path input = Paths.get("../../test-files/src_example");
        Path output = getDefaultPath();

        TestProgressWithLogging progressWithLogging = new TestProgressWithLogging();
        ExecutionResults results = executeWindup(input, output, progressWithLogging);
        Assert.assertNotNull(results.getClassifications());
        Assert.assertNotNull(results.getHints());
        Assert.assertTrue(results.getHints().iterator().hasNext());
        checkQuickfixInHints(results.getHints());
        Assert.assertTrue(results.getClassifications().iterator().hasNext());
        Assert.assertTrue(testProvider.sourceMode);
        Assert.assertFalse(testProvider.onlineMode);
        Assert.assertTrue(progressWithLogging.logRecords.size() > 10);
    }

    private void checkQuickfixInHints(Iterable<org.jboss.windup.tooling.data.Hint> hints)
    {
        int quickfixCount = 0;
        for (org.jboss.windup.tooling.data.Hint hintDTO : hints)
        {
            Iterable<org.jboss.windup.tooling.data.Quickfix> quickfixes = hintDTO.getQuickfixes();
            for (org.jboss.windup.tooling.data.Quickfix quickfix : quickfixes)
            {
                Assert.assertEquals("quickfix1", quickfix.getName());
                Assert.assertEquals(QuickfixType.DELETE_LINE, quickfix.getType());
                quickfixCount++;
            }
        }
        Assert.assertTrue(quickfixCount > 0);
    }

    @Test
    public void testReloadGraph() throws IOException
    {
        Path input = Paths.get("../../test-files/src_example");
        Path output = getDefaultPath();

        ExecutionResults resultsOriginal = executeWindup(input, output, new TestProgressMonitor());

        ExecutionResults resultsLater = graphLoader.loadResults(output);
        Assert.assertTrue(resultsLater.getClassifications().iterator().hasNext());
        Assert.assertTrue(resultsLater.getHints().iterator().hasNext());

        Assert.assertEquals(Iterables.size(resultsOriginal.getClassifications()), Iterables.size(resultsLater.getClassifications()));
        Assert.assertEquals(Iterables.size(resultsOriginal.getHints()), Iterables.size(resultsLater.getHints()));
        Assert.assertEquals(Iterables.size(resultsOriginal.getReportLinks()), Iterables.size(resultsLater.getReportLinks()));
    }

    private ExecutionResults executeWindup(Path input, Path output, WindupProgressMonitor progressMonitor)
    {
        return builder.begin(Paths.get("."))
                    .setInput(input)
                    .setOutput(output)
                    .setProgressMonitor(progressMonitor)
                    .includePackage("org.windup.examples.ejb.messagedriven")
                    .ignore("\\.class$")
                    .setOption(SourceModeOption.NAME, true)
                    .setOption(OnlineModeOption.NAME, false)
                    .execute();
    }

    private Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup").resolve("execbuildertest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Singleton
    public static class TestProvider extends AbstractRuleProvider
    {
        private boolean sourceMode = false;
        private boolean onlineMode = false;

        public TestProvider()
        {
            super(MetadataBuilder.forProvider(TestProvider.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
        {
            return ConfigurationBuilder.begin()
                        .addRule()
                        .when(JavaClass.references("javax.{*}"))
                        .perform(Hint.withText("References javax.*").withQuickfix(createTestQuickfix()).withEffort(43)
                                    .and(Classification.as("References some javax stuff")))
                        .addRule()
                        .perform(new GraphOperation()
                        {
                            @Override
                            public void perform(GraphRewrite event, EvaluationContext context)
                            {
                                WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                                onlineMode = configuration.isOnlineMode();

                                WindupJavaConfigurationModel javaConfiguration = WindupJavaConfigurationService.getJavaConfigurationModel(event
                                            .getGraphContext());
                                sourceMode = javaConfiguration.isSourceMode();
                            }
                        });
        }

        /**
         * Create a delete quickfix type for test
         */
        private Quickfix createTestQuickfix()
        {
            Quickfix quickfix = new Quickfix();
            quickfix.setName("quickfix1");
            quickfix.setType(QuickfixType.DELETE_LINE);
            return quickfix;
        }
    }

    private class TestProgressMonitor implements WindupProgressMonitor
    {
        private int totalWork;
        private int completed;
        private boolean done;

        @Override
        public void beginTask(String name, int totalWork)
        {
            this.totalWork = totalWork;
        }

        @Override
        public void done()
        {
            this.done = true;
        }

        @Override
        public boolean isCancelled()
        {
            return false;
        }

        @Override
        public void setCancelled(boolean value)
        {

        }

        @Override
        public void setTaskName(String name)
        {

        }

        @Override
        public void subTask(String name)
        {

        }

        @Override
        public void worked(int work)
        {
            this.completed = work;
        }
    }

    private class TestProgressWithLogging extends TestProgressMonitor implements WindupToolingProgressMonitor {
        private final List<LogRecord> logRecords = new ArrayList<>();

        @Override
        public void logMessage(LogRecord logRecord) {
            logRecords.add(logRecord);
        }
    }
}
