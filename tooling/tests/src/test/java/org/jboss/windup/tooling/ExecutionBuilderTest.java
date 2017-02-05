package org.jboss.windup.tooling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.logmanager.Level;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.exec.configuration.options.OnlineModeOption;
import org.jboss.windup.exec.configuration.options.SourceOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Quickfix;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.tooling.data.QuickfixType;
import org.jboss.windup.tooling.rules.RuleProviderRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class ExecutionBuilderTest
{
    private static final int PORT = 9874;

    private static Logger LOG = Logger.getLogger(ExecutionBuilderTest.class.getName());

    @Inject
    private ExecutionBuilder builder;
    @Inject
    private GraphLoader graphLoader;
    @Inject
    private TestProvider testProvider;
    @Inject
    private ToolingXMLService toolingXMLService;
    @Inject
    private ToolingRMIServer rmiServer;

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

    public static Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup").resolve("execbuildertest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    private static ExecutionBuilder getExecutionBuilderFromRMIRegistry()
    {
        try
        {
            Registry registry = LocateRegistry.getRegistry(PORT);
            ExecutionBuilder executionBuilder = (ExecutionBuilder) registry.lookup(ExecutionBuilder.LOOKUP_NAME);
            executionBuilder.clear();
            return executionBuilder;
        }
        catch (RemoteException | NotBoundException e)
        {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
    
    @Test
    public void testRuleProviderRegistry() throws RemoteException {
    	  rmiServer.startServer(PORT, "");

          ExecutionBuilder builder = getExecutionBuilderFromRMIRegistry();
          Assert.assertNotNull(builder);
          
          RuleProviderRegistry registry = builder.getRuleProviderRegistry();
          Assert.assertNotNull(registry);
          
          Assert.assertFalse(registry.getRuleProviders().isEmpty());
    }

    @Test
    public void testSchemaGeneration() throws Exception
    {
        Path outputDirectory = getDefaultPath();
        Files.createDirectories(outputDirectory);
        Path output = outputDirectory.resolve("sample.xsd");
        try
        {
            LOG.info("Generating test schema at: " + output);
            toolingXMLService.generateSchema(output);
            Assert.assertTrue(Files.isRegularFile(output));
        }
        finally
        {
            FileUtils.deleteDirectory(outputDirectory.toFile());
        }
    }

    @Test
    public void testExecutionBuilder() throws Exception
    {
        Assert.assertNotNull(builder);
        Assert.assertNotNull(testProvider);

        Path input = Paths.get("../../test-files/src_example");
        Path output = getDefaultPath();

        TestProgressMonitor progressWithLogging = new TestProgressMonitor();
        ExecutionResults results = executeWindup(input, output, progressWithLogging);
        Assert.assertNotNull(results.getClassifications());
        Assert.assertNotNull(results.getHints());
        Assert.assertTrue(results.getHints().iterator().hasNext());
        checkQuickfixInHints(results.getHints());
        Assert.assertTrue(results.getClassifications().iterator().hasNext());
        Assert.assertTrue(testProvider.sourceMode);
        Assert.assertFalse(testProvider.onlineMode);

        Path xmlResultsFile = output.resolve("sample_results.xml");
        LOG.info("Serializing results to: " + xmlResultsFile);
        results.serializeToXML(xmlResultsFile);
        Assert.assertTrue(Files.isRegularFile(xmlResultsFile));
        Assert.assertTrue(Files.size(xmlResultsFile) > 100);
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

    private ExecutionResults executeWindup(Path input, Path output, WindupToolingProgressMonitor progressMonitor) throws RemoteException
    {
        builder.setWindupHome(Paths.get(".").toString());
        builder.setInput(input.toString());
        builder.setOutput(output.toString());
        builder.setProgressMonitor(progressMonitor);
        builder.includePackage("org.windup.examples.ejb.messagedriven");
        builder.ignore("\\.class$");
        builder.setOption(SourceModeOption.NAME, true);
        builder.setOption(OnlineModeOption.NAME, false);
        return builder.execute();
    }

    @Test
    public void testExecutionBuilderRegistered() throws Exception
    {
        rmiServer.startServer(PORT, "");

        Path input = Paths.get("../../test-files/src_example");
        Path output = ExecutionBuilderTest.getDefaultPath();

        ExecutionBuilder builder = getExecutionBuilderFromRMIRegistry();
        Assert.assertNotNull(builder);

        builder.setWindupHome(Paths.get(".").toString());
        builder.setInput(input.toString());
        builder.setOutput(output.toString());
        builder.setProgressMonitor(new TestProgressMonitor());
        builder.setOption(SourceModeOption.NAME, true);
        builder.setOption(TargetOption.NAME, Lists.newArrayList("eap"));
        builder.setOption(SourceOption.NAME, Lists.newArrayList("eap"));
        builder.setOption(OnlineModeOption.NAME, false);
        builder.includePackage("org.windup.examples.ejb.messagedriven");
        builder.ignore("\\.class$");

        ExecutionResults results = builder.execute();

        Assert.assertNotNull(results);
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
            quickfix.setType(org.jboss.windup.reporting.model.QuickfixType.DELETE_LINE);
            return quickfix;
        }
    }

    class TestProgressMonitor extends UnicastRemoteObject implements WindupToolingProgressMonitor, Remote
    {
        private static final long serialVersionUID = 1L;
        final List<LogRecord> logRecords = new ArrayList<>();
        private int totalWork;
        private int completed;
        private boolean done;

        protected TestProgressMonitor() throws RemoteException
        {
            super();
        }

        @Override
        public void logMessage(LogRecord logRecord)
        {
            logRecords.add(logRecord);
        }

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
}
