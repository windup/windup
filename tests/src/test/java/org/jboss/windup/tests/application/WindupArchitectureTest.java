package org.jboss.windup.tests.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupConfiguration;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.engine.WindupProgressMonitor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;

/**
 * Base class for Windup end-to-end tests.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class WindupArchitectureTest
{
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    /**
     * Get an instance of the {@link GraphContextFactory}.
     */
    protected GraphContextFactory getFactory()
    {
        return factory;
    }

    void runTest(String inputPath, boolean sourceMode) throws Exception
    {
        List<String> includeList = Collections.emptyList();
        List<String> excludeList = Collections.emptyList();
        runTest(factory.create(), inputPath, sourceMode, includeList, excludeList);
    }

    void runTest(GraphContext graphContext, String inputPath, boolean sourceMode)
                throws Exception
    {
        List<String> includeList = Collections.emptyList();
        List<String> excludeList = Collections.emptyList();
        runTest(graphContext, inputPath, sourceMode, includeList, excludeList);
    }

    void runTest(GraphContext graphContext, String inputPath, boolean sourceMode,
                List<String> includePackages) throws Exception
    {
        List<String> excludeList = Collections.emptyList();
        runTest(graphContext, inputPath, sourceMode, includePackages, excludeList);
    }

    void runTest(final GraphContext graphContext,
                final String inputPath,
                final boolean sourceMode,
                final List<String> includePackages,
                final List<String> excludePackages) throws Exception
    {

        WindupConfigurationModel windupConfig = GraphService.getConfigurationModel(graphContext);
        windupConfig.setInputPath(inputPath);
        windupConfig.setSourceMode(sourceMode);
        windupConfig.setScanJavaPackageList(includePackages);
        windupConfig.setExcludeJavaPackageList(excludePackages);

        if (windupConfig.getOutputPath() == null)
            windupConfig.setOutputPath(graphContext.getGraphDirectory().toString());
        windupConfig.setSourceMode(false);

        WindupConfiguration wpc = new WindupConfiguration().setGraphContext(graphContext);
        RecordingWindupProgressMonitor progressMonitor = new RecordingWindupProgressMonitor();
        wpc.setProgressMonitor(progressMonitor);

        processor.execute(wpc);

        Assert.assertFalse(progressMonitor.isCancelled());
        Assert.assertTrue(progressMonitor.isDone());
        Assert.assertFalse(progressMonitor.getSubTaskNames().isEmpty());
        Assert.assertTrue(progressMonitor.getTotalWork() > 0);
        Assert.assertTrue(progressMonitor.getCompletedWork() > 0);
        Assert.assertEquals(progressMonitor.getTotalWork(), progressMonitor.getCompletedWork());
    }

    /*
     * Supporting types
     */
    private static class RecordingWindupProgressMonitor implements WindupProgressMonitor
    {
        private int totalWork = -1;
        private boolean done;
        private boolean cancelled;
        private List<String> taskNames = new ArrayList<>();
        private List<String> subTaskNames = new ArrayList<>();
        private int workDone;

        @Override
        public void beginTask(String name, int totalWork)
        {
            if (this.totalWork == -1)
                this.totalWork = totalWork;
            else
                throw new IllegalStateException("Total work already set.");
        }

        @Override
        public void done()
        {
            this.done = true;
        }

        @Override
        public boolean isCancelled()
        {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled)
        {
            if (cancelled)
                this.cancelled = true;
        }

        @Override
        public void setTaskName(String name)
        {
            this.taskNames.add(name);
        }

        @Override
        public void subTask(String name)
        {
            this.subTaskNames.add(name);
        }

        @Override
        public void worked(int work)
        {
            this.workDone += work;
        }

        public int getTotalWork()
        {
            return totalWork;
        }

        public boolean isDone()
        {
            return done;
        }

        public List<String> getSubTaskNames()
        {
            return subTaskNames;
        }

        public int getCompletedWork()
        {
            return workDone;
        }

    }
}
