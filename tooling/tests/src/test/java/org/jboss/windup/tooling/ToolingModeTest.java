package org.jboss.windup.tooling;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.jboss.forge.furnace.Furnace;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.quickfix.Quickfix;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.tooling.data.QuickfixType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author <a href="mailto:josteele@redhat.com">John Steele</a>
 */
@RunWith(Arquillian.class)
public class ToolingModeTest {

    @Inject
    private TestProvider testProvider;

    @Inject
    private Furnace furnace;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup:windup-tooling"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-project"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap
                .create(AddonArchive.class)
                .addBeansXML();
    }

    private void checkQuickfixInHints(Iterable<org.jboss.windup.tooling.data.Hint> hints) {
        int quickfixCount = 0;
        for (org.jboss.windup.tooling.data.Hint hintDTO : hints) {
            Iterable<org.jboss.windup.tooling.data.Quickfix> quickfixes = hintDTO.getQuickfixes();
            for (org.jboss.windup.tooling.data.Quickfix quickfix : quickfixes) {
                Assert.assertEquals("quickfix1", quickfix.getName());
                Assert.assertEquals(QuickfixType.DELETE_LINE, quickfix.getType());
                quickfixCount++;
            }
        }
        Assert.assertTrue(quickfixCount > 0);
    }

    public static Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup").resolve("toolingmodetest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Test
    public void testTooingModeResults() throws Exception {
        Assert.assertNotNull(furnace);

        Set<String> input = Sets.newHashSet(Paths.get("../../test-files/src_example").toString());
        Path output = ToolingModeTest.getDefaultPath();
        boolean sourceMode = true;
        boolean ignoreReport = false;
        List<String> ignorePatterns = Lists.newArrayList();
        String windupHome = "";
        List<String> source = Lists.newArrayList("eap");
        List<String> target = Lists.newArrayList("eap");
        List<File> rulesDir = Lists.newArrayList();
        List<String> packages = Lists.newArrayList();
        List<String> excludePackages = Lists.newArrayList();
        Map<String, Object> options = Maps.newHashMap();

        ToolingModeRunner runner = furnace.getAddonRegistry().getServices(ToolingModeRunner.class).get();
        Assert.assertNotNull(runner);

        TestProgressMonitor progressWithLogging = new TestProgressMonitor();
        runner.setProgressMonitor(progressWithLogging);

        ExecutionResults results = runner.run(input, output.toString(), sourceMode, ignoreReport, ignorePatterns,
                windupHome, source, target, rulesDir, packages, excludePackages, options);

        Assert.assertNotNull(results);

        Assert.assertNotNull(results.getClassifications());
        Assert.assertNotNull(results.getHints());
        Assert.assertTrue(results.getHints().iterator().hasNext());
        checkQuickfixInHints(results.getHints());
        Assert.assertTrue(results.getClassifications().iterator().hasNext());
        Assert.assertTrue(testProvider.sourceMode);
        Assert.assertFalse(testProvider.onlineMode);

        Path xmlResultsFile = output.resolve("results.xml");
        Assert.assertTrue(Files.isRegularFile(xmlResultsFile));
        Assert.assertTrue(Files.size(xmlResultsFile) > 100);
        Assert.assertTrue(progressWithLogging.logRecords.size() > 10);
    }

    @Singleton
    public static class TestProvider extends AbstractRuleProvider {
        private boolean sourceMode = false;
        private boolean onlineMode = false;

        public TestProvider() {
            super(MetadataBuilder.forProvider(TestProvider.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(JavaClass.references("javax.{*}"))
                    .perform(Hint.withText("References javax.*").withQuickfix(createTestQuickfix()).withEffort(43)
                            .and(Classification.as("References some javax stuff")))
                    .addRule()
                    .perform(new GraphOperation() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context) {
                            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                            onlineMode = configuration.isOnlineMode();

                            WindupJavaConfigurationModel javaConfiguration = WindupJavaConfigurationService.getJavaConfigurationModel(event
                                    .getGraphContext());
                            sourceMode = javaConfiguration.isSourceMode();
                        }
                    });
        }

        private Quickfix createTestQuickfix() {
            Quickfix quickfix = new Quickfix();
            quickfix.setName("quickfix1");
            quickfix.setType(org.jboss.windup.reporting.model.QuickfixType.DELETE_LINE);
            return quickfix;
        }
    }

    class TestProgressMonitor extends UnicastRemoteObject implements WindupToolingProgressMonitor, Remote {
        private static final long serialVersionUID = 1L;
        final List<LogRecord> logRecords = new ArrayList<>();
        private int totalWork;
        private int completed;
        private boolean done;

        protected TestProgressMonitor() throws RemoteException {
            super();
        }

        @Override
        public void logMessage(LogRecord logRecord) {
            logRecords.add(logRecord);
        }

        @Override
        public void beginTask(String name, int totalWork) {
            this.totalWork = totalWork;
        }

        public boolean isDone() {
            return this.done;
        }

        @Override
        public void done() {
            this.done = true;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void setCancelled(boolean value) {

        }

        @Override
        public void setTaskName(String name) {

        }

        @Override
        public void subTask(String name) {

        }

        @Override
        public void worked(int work) {
            this.completed = work;
        }
    }
}
