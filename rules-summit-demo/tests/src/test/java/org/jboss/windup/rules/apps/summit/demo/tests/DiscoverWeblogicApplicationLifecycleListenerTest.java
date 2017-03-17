package org.jboss.windup.rules.apps.summit.demo.tests;

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

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.configuration.options.OnlineModeOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.tooling.ExecutionBuilder;
import org.jboss.windup.tooling.ExecutionResults;
import org.jboss.windup.tooling.ToolingRMIServer;
import org.jboss.windup.tooling.WindupToolingProgressMonitor;
import org.jboss.windup.tooling.data.Hint;
import org.jboss.windup.tooling.data.Quickfix;
import org.jboss.windup.tooling.data.TransformationQuickfixChange;
import org.jboss.windup.tooling.data.QuickfixTransformation;
import org.jboss.windup.tooling.data.TransformationQuickfixDTOImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

@RunWith(Arquillian.class)
public class DiscoverWeblogicApplicationLifecycleListenerTest
{
    private static final int PORT = 9874;
	
	//@Inject
    //private ExecutionBuilder builder;

    @Inject
    private ToolingRMIServer rmiServer;
    
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
        		@AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-summit-demo")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap
                    .create(AddonArchive.class)
                    .addBeansXML();
    }

    public static Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup").resolve("discoverweblogicapplicationlifecycleListenertest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Test
    public void testWeblogicApplicationLifecycleListener() throws Exception
    {
        rmiServer.startServer(PORT, "");
    	
        Path input = Paths.get("../../test-files/summit-demo-test");
        Path output = getDefaultPath();

        TestProgressMonitor progressWithLogging = new TestProgressMonitor();
        ExecutionResults results = executeWindup(input, output, progressWithLogging);
        List<Hint> hints = Lists.newArrayList(results.getHints());
        Assert.assertTrue(hints.size() == 1);
        List<Quickfix> quickfixes = Lists.newArrayList(hints.get(0).getQuickfixes());
        Assert.assertTrue(quickfixes.size() == 1);
        Quickfix quickfix = quickfixes.get(0);
        Assert.assertTrue(quickfix instanceof TransformationQuickfixDTOImpl);
        TransformationQuickfixDTOImpl transformationFix = (TransformationQuickfixDTOImpl)quickfix;
        List<TransformationQuickfixChange> changes = transformationFix.getChanges();
        Assert.assertTrue(changes.size() == 1);
        TransformationQuickfixChange change = changes.get(0);
        String preview = change.preview();
        Assert.assertTrue(preview.equals("Preview..."));
    }

     private ExecutionResults executeWindup(Path input, Path output, WindupToolingProgressMonitor progressMonitor) throws RemoteException
    {
    	ExecutionBuilder builder = getExecutionBuilderFromRMIRegistry();
        builder.setWindupHome(Paths.get(".").toString());
        builder.setInput(input.toString());
        builder.setOutput(output.toString());
        builder.setProgressMonitor(progressMonitor);
        builder.setOption(SourceModeOption.NAME, true);
        builder.setOption(OnlineModeOption.NAME, false);
        return builder.execute();
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
             e.printStackTrace();
         }
         return null;
     }


    class TestProgressMonitor extends UnicastRemoteObject implements WindupToolingProgressMonitor, Remote
    {
        private static final long serialVersionUID = 1L;
        final List<LogRecord> logRecords = new ArrayList<>();

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
        }

        @Override
        public void done()
        {
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
        }
    }
}