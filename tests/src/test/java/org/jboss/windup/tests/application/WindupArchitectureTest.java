package org.jboss.windup.tests.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.engine.WindupProcessorConfig;
import org.jboss.windup.engine.WindupProgressMonitor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphLifecycleListener;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.junit.Assert;


/**
 * Base class for Windup end-to-end tests.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class WindupArchitectureTest
{
    void runTest(WindupProcessor processor, GraphContext graphContext, String inputPath, boolean sourceMode)
                throws Exception
    {
        List<String> excludeList = Collections.emptyList();
        runTest(processor, graphContext, inputPath, sourceMode, Collections.singletonList(""), excludeList);
    }

    void runTest(WindupProcessor processor, GraphContext graphContext, String inputPath, boolean sourceMode,
                List<String> includePackages) throws Exception
    {
        List<String> excludeList = Collections.emptyList();
        runTest(processor, graphContext, inputPath, sourceMode, includePackages, excludeList);
    }

    void runTest(WindupProcessor processor,
            final GraphContext graphContext,
            final String inputPath,
            final boolean sourceMode,
            final List<String> includePackages,
            final List<String> excludePackages) throws Exception
    {
        Assert.assertNotNull(processor);
        Assert.assertNotNull(processor.toString());

        // Output dir
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "WindupReport");
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        // GraphContext init
        final GraphLifecycleListener initer = new GraphLifecycleListener()
        {
            public void postOpen(GraphContext context)
            {
                // Windup config
                WindupConfigurationModel windupCfg = graphContext.getFramed().addVertex(null, WindupConfigurationModel.class);
                windupCfg.setInputPath(inputPath);
                windupCfg.setSourceMode(sourceMode);
                windupCfg.setScanJavaPackageList(includePackages);
                windupCfg.setExcludeJavaPackageList(excludePackages);
                
                windupCfg.setOutputPath(outputPath.toAbsolutePath().toString());
                windupCfg.setSourceMode(false);
            }

            public void preShutdown(GraphContext context) { }
        };

        // Processor config. Overlaps a bit.
        WindupProcessorConfig wpc = new WindupProcessorConfig().setGraphListener(initer);
        wpc.setOutputDirectory(outputPath);
        RecordingWindupProgressMonitor progressMonitor = new RecordingWindupProgressMonitor();
        wpc.setProgressMonitor(progressMonitor);
        
        // Execute
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

        public List<String> getTaskNames()
        {
            return taskNames;
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
