package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.UserRulesDirectoryOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.MigrationIssuesReportModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.config.ExcludePackagesOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.model.JavaApplicationOverviewReportModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateJavaApplicationOverviewReportRuleProvider;
import org.jboss.windup.rules.apps.java.reporting.rules.EnableCompatibleFilesReportOption;
import org.jboss.windup.rules.apps.tattletale.EnableTattletaleReportOption;
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

    Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                    .resolve("windupgraph_" + RandomStringUtils.randomAlphanumeric(6));
    }

    GraphContext createGraphContext()
    {
        return createGraphContext(getDefaultPath());
    }

    GraphContext createGraphContext(Path path)
    {
        return factory.create(path);
    }

    void runTest(String inputPath, boolean sourceMode) throws Exception
    {
        runTest(Collections.singletonList(inputPath), sourceMode);
    }

    void runTest(Iterable<String> inputPaths, boolean sourceMode) throws Exception
    {
        List<String> includeList = Collections.emptyList();
        List<String> excludeList = Collections.emptyList();
        runTest(createGraphContext(), inputPaths, null, sourceMode, includeList, excludeList);
    }

    void runTest(GraphContext graphContext, String inputPath, boolean sourceMode)
                throws Exception
    {
        runTest(graphContext, Collections.singletonList(inputPath), sourceMode);
    }

    void runTest(GraphContext graphContext, Iterable<String> inputPaths, boolean sourceMode)
                throws Exception
    {
        List<String> includeList = Collections.emptyList();
        List<String> excludeList = Collections.emptyList();
        runTest(graphContext, inputPaths, null, sourceMode, includeList, excludeList);
    }

    void runTest(GraphContext graphContext, String inputPath, boolean sourceMode,
                List<String> includePackages) throws Exception
    {
        runTest(graphContext, Collections.singletonList(inputPath), sourceMode, includePackages);
    }

    void runTest(GraphContext graphContext, Iterable<String> inputPaths, boolean sourceMode,
                List<String> includePackages) throws Exception
    {
        List<String> excludeList = Collections.emptyList();
        runTest(graphContext, inputPaths, null, sourceMode, includePackages, excludeList);
    }

    void runTest(final GraphContext graphContext,
                final String inputPath,
                final File userRulesDir,
                final boolean sourceMode,
                final List<String> includePackages,
                final List<String> excludePackages) throws Exception
    {
        runTest(graphContext, Collections.singletonList(inputPath), userRulesDir, sourceMode, includePackages, excludePackages);
    }

    void runTest(final GraphContext graphContext,
                final Iterable<String> inputPaths,
                final File userRulesDir,
                final boolean sourceMode,
                final List<String> includePackages,
                final List<String> excludePackages) throws Exception
    {
        Map<String, Object> otherOptions = Collections.emptyMap();
        runTest(graphContext, inputPaths, userRulesDir, sourceMode, includePackages, excludePackages, otherOptions);
    }

    void runTest(final GraphContext graphContext,
                final Iterable<String> inputPaths,
                final File userRulesDir,
                final boolean sourceMode,
                final List<String> includePackages,
                final List<String> excludePackages,
                final Map<String, Object> otherOptions) throws Exception
    {

        WindupConfiguration windupConfiguration = new WindupConfiguration().setGraphContext(graphContext);
        windupConfiguration.setAlwaysHaltOnException(true);
        for (String inputPath : inputPaths)
        {
            windupConfiguration.addInputPath(Paths.get(inputPath));
        }
        windupConfiguration.setOutputDirectory(graphContext.getGraphDirectory());
        if (userRulesDir != null)
        {
            windupConfiguration.setOptionValue(UserRulesDirectoryOption.NAME, userRulesDir);
        }
        windupConfiguration.setOptionValue(SourceModeOption.NAME, sourceMode);
        windupConfiguration.setOptionValue(ScanPackagesOption.NAME, includePackages);
        windupConfiguration.setOptionValue(ExcludePackagesOption.NAME, excludePackages);
        windupConfiguration.setOptionValue(EnableTattletaleReportOption.NAME, true);
        windupConfiguration.setOptionValue(EnableCompatibleFilesReportOption.NAME, true);

        for (Map.Entry<String, Object> otherOption : otherOptions.entrySet())
        {
            windupConfiguration.setOptionValue(otherOption.getKey(), otherOption.getValue());
        }

        RecordingWindupProgressMonitor progressMonitor = new RecordingWindupProgressMonitor();
        windupConfiguration.setProgressMonitor(progressMonitor);

        processor.execute(windupConfiguration);

        Assert.assertFalse(progressMonitor.isCancelled());
        Assert.assertTrue(progressMonitor.isDone());
        Assert.assertFalse(progressMonitor.getSubTaskNames().isEmpty());
        Assert.assertTrue(progressMonitor.getTotalWork() > 0);
        Assert.assertTrue(progressMonitor.getCompletedWork() > 0);
        Assert.assertEquals(progressMonitor.getTotalWork(), progressMonitor.getCompletedWork());
    }

    JavaApplicationOverviewReportModel getMainApplicationReport(GraphContext context)
    {
        return (JavaApplicationOverviewReportModel) getReport(context, CreateJavaApplicationOverviewReportRuleProvider.TEMPLATE_APPLICATION_REPORT,
                    CreateJavaApplicationOverviewReportRuleProvider.DETAILS_REPORT);
    }

    MigrationIssuesReportModel getCatchallApplicationReport(GraphContext context)
    {
        // Before WINDUP-986: "Potential Issues"
        return (MigrationIssuesReportModel) getReport(context, "/reports/templates/migration-issues.ftl", "Migration Issues");
    }

    ReportModel getReport(GraphContext context, String template, String name)
    {
        ReportService reportService = new ReportService(context);
        Iterable<ReportModel> reportModels = reportService.findAllByProperty(
                    ReportModel.TEMPLATE_PATH,
                    template);
        ReportModel reportModel = null;
        for (ReportModel candidateModel : reportModels)
        {
            if (StringUtils.equals(candidateModel.getReportName(), name))
            {
                reportModel = candidateModel;
                break;
            }
        }
        Assert.assertNotNull(reportModel);

        return reportModel;
    }

    protected void allDecompiledFilesAreLinked(GraphContext context)
    {
        GraphService<JavaClassFileModel> classModels = new GraphService<>(context, JavaClassFileModel.class);
        for (JavaClassFileModel javaClassFileModel : classModels.findAllWithoutProperty(JavaClassFileModel.SKIP_DECOMPILATION, true))
        {
            Assert.assertNotNull(javaClassFileModel.getJavaClass().getDecompiledSource());
        }

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
