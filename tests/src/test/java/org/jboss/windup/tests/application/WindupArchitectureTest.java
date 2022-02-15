package org.jboss.windup.tests.application;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.ExportCSVOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.MigrationIssuesReportModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.config.ExcludePackagesOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.dependencyreport.CreateDependencyReportRuleProvider;
import org.jboss.windup.rules.apps.java.model.JavaApplicationOverviewReportModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateDependencyGraphReportRuleProvider;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateJavaApplicationOverviewReportRuleProvider;
import org.jboss.windup.rules.apps.java.reporting.rules.EnableCompatibleFilesReportOption;
import org.jboss.windup.rules.apps.tattletale.DisableTattletaleReportOption;
import org.junit.Assert;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Base class for Windup end-to-end tests.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class WindupArchitectureTest
{
    public static final String REPORTS_TEMPLATES_MIGRATION_ISSUES_FTL = "/reports/templates/migration-issues.ftl";

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

    protected GraphContext createGraphContext()
    {
        return createGraphContext(getDefaultPath());
    }

    GraphContext createGraphContext(Path path)
    {
        return factory.create(path, true);
    }

    protected void runTest(String inputPath, boolean sourceMode) throws Exception
    {
        runTest(Collections.singletonList(inputPath), sourceMode);
    }

    protected void runTest(Iterable<String> inputPaths, boolean sourceMode) throws Exception
    {
        List<String> includeList = Collections.emptyList();
        List<String> excludeList = Collections.emptyList();
        runTest(createGraphContext(), inputPaths, null, sourceMode, includeList, excludeList);
    }

    protected void runTest(GraphContext graphContext, String inputPath, String userRulesDir, boolean sourceMode) throws Exception
    {
        List<String> incl = Collections.emptyList();
        List<String> excl = Collections.emptyList();
        runTest(graphContext, Collections.singletonList(inputPath), Collections.singletonList(new File(userRulesDir)), sourceMode, incl, excl);
    }

    protected void runTest(GraphContext graphContext, String inputPath, boolean sourceMode)
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
                final Iterable<File> userRulesDirs,
                final boolean sourceMode,
                final List<String> includePackages,
                final List<String> excludePackages) throws Exception
    {
        runTest(graphContext, Collections.singletonList(inputPath), userRulesDirs, sourceMode, includePackages, excludePackages);
    }

    void runTest(final GraphContext graphContext,
                final Iterable<String> inputPaths,
                final Iterable<File> userRulesDirs,
                final boolean sourceMode,
                final List<String> includePackages,
                final List<String> excludePackages) throws Exception
    {
        Map<String, Object> otherOptions = Collections.emptyMap();
        runTest(graphContext, inputPaths, userRulesDirs, sourceMode, includePackages, excludePackages, otherOptions);
    }

    void runTest(final GraphContext graphContext,
                final Iterable<String> inputPaths,
                final Iterable<File> userRulesDirs,
                final boolean sourceMode,
                final List<String> includePackages,
                final List<String> excludePackages,
                final Map<String, Object> otherOptions) throws Exception
    {

    	Locale previousLocale = Locale.getDefault();
    	Locale.setDefault(Locale.US);
        WindupConfiguration windupConfiguration = new WindupConfiguration().setGraphContext(graphContext);
        windupConfiguration.setAlwaysHaltOnException(true);
        for (String inputPath : inputPaths)
        {
            windupConfiguration.addInputPath(Paths.get(inputPath));
        }
        windupConfiguration.setOutputDirectory(graphContext.getGraphDirectory());
        if (userRulesDirs != null)        {
            for (File uRulesDir : userRulesDirs)            {
                windupConfiguration.addDefaultUserRulesDirectory(uRulesDir.toPath());
                windupConfiguration.addDefaultUserLabelsDirectory(uRulesDir.toPath());
            }
        }
        windupConfiguration.setOptionValue(SourceModeOption.NAME, sourceMode);
        windupConfiguration.setOptionValue(ScanPackagesOption.NAME, includePackages);
        windupConfiguration.setOptionValue(ExcludePackagesOption.NAME, excludePackages);
        windupConfiguration.setOptionValue(DisableTattletaleReportOption.NAME, true);
        windupConfiguration.setOptionValue(EnableCompatibleFilesReportOption.NAME, true);
        windupConfiguration.setOptionValue(ExportCSVOption.NAME, true);

        for (Map.Entry<String, Object> otherOption : otherOptions.entrySet())
        {
            windupConfiguration.setOptionValue(otherOption.getKey(), otherOption.getValue());
        }

        RecordingWindupProgressMonitor recordingMonitor = new RecordingWindupProgressMonitor();
        WindupProgressMonitor testMonitor = overrideWindupProgressMonitor(recordingMonitor);
        windupConfiguration.setProgressMonitor(testMonitor);

        processor.execute(windupConfiguration);

        assertRecordedData(recordingMonitor);

        Locale.setDefault(previousLocale);
    }

    /**
     * Override to customize what's expected.
     */
    protected void assertRecordedData(RecordingWindupProgressMonitor recordingMonitor)
    {
        Assert.assertFalse(recordingMonitor.isCancelled());
        Assert.assertTrue(recordingMonitor.isDone());
        Assert.assertFalse(recordingMonitor.getSubTaskNames().isEmpty());
        Assert.assertTrue(recordingMonitor.getTotalWork() > 0);
        Assert.assertTrue(recordingMonitor.getCompletedWork() > 0);
        Assert.assertEquals(recordingMonitor.getTotalWork() + 1, recordingMonitor.getCompletedWork());
    }

    /**
     * Override this if you need to intercept the calls to WindupProgressMonitor.
     * The overriding method must keep the functionality of passed monitor.
     * See {@link CombinedWindupProgressMonitor}.
     */
    public WindupProgressMonitor overrideWindupProgressMonitor(final WindupProgressMonitor testMonitor)
    {
        return testMonitor;
    }

    JavaApplicationOverviewReportModel getMainApplicationReport(GraphContext context)
    {
        return (JavaApplicationOverviewReportModel) getReport(context, CreateJavaApplicationOverviewReportRuleProvider.TEMPLATE_APPLICATION_REPORT,
                    CreateJavaApplicationOverviewReportRuleProvider.DETAILS_REPORT);
    }

    Iterable<ReportModel> getApplicationDetailsReports(GraphContext context)
    {
        return getReports(context, CreateJavaApplicationOverviewReportRuleProvider.TEMPLATE_APPLICATION_REPORT);
    }

    MigrationIssuesReportModel getMigrationIssuesReport(GraphContext context, final ProjectModel projectModel)
    {
        Iterable<ReportModel> reportModels = Iterables.filter(getReports(context, REPORTS_TEMPLATES_MIGRATION_ISSUES_FTL),
                    new Predicate<ReportModel>()
                    {
                        @Override
                        public boolean apply(@Nullable ReportModel input)
                        {
                            if (!(input instanceof MigrationIssuesReportModel))
                                return false;

                            MigrationIssuesReportModel migrationIssuesReportModel = (MigrationIssuesReportModel) input;
                            return projectModel == null || projectModel.equals(migrationIssuesReportModel.getProjectModel());
                        }
                    });

        if (!reportModels.iterator().hasNext())
            return null;

        if (Iterables.size(reportModels) > 1)
            Assert.fail("Only one migration issues report expected for the application!");

        return (MigrationIssuesReportModel)reportModels.iterator().next();
    }

    MigrationIssuesReportModel getMigrationIssuesReport(GraphContext context)
    {
        return (MigrationIssuesReportModel) getReport(context, REPORTS_TEMPLATES_MIGRATION_ISSUES_FTL, "Issues");
    }

    ApplicationReportModel getJarDependencyReport(GraphContext context)
    {
        return (ApplicationReportModel) getReport(context, CreateDependencyReportRuleProvider.TEMPLATE, CreateDependencyReportRuleProvider.REPORT_NAME);
    }

    Iterable<ReportModel> getReports(GraphContext context, String template)
    {
        ReportService reportService = new ReportService(context);
        return reportService.findAllByProperty(ReportModel.TEMPLATE_PATH, template);
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
            Assert.assertNotNull(String.format("%s not found", javaClassFileModel.getJavaClass().getClassName()), javaClassFileModel.getJavaClass().getDecompiledSource());
        }

    }

    Path getPathForReport(GraphContext graphContext, ReportModel report)
    {
        return new ReportService(graphContext).getReportDirectory().resolve(report.getReportFilename());
    }

    Path getGlobalDependencyGraphReportPath(GraphContext graphContext)
    {
        return getDependencyGraphReportPath (graphContext,"dependency_graph_report_global.html");
    }

    Path getApplicationDependencyGraphReportPath(GraphContext graphContext)
    {
        return getDependencyGraphReportPath (graphContext,"dependency_graph_report.html");
    }

    private Path getDependencyGraphReportPath(GraphContext graphContext, String reportFilename)
    {
        Service<ApplicationReportModel> service = graphContext.service(ApplicationReportModel.class);
        Iterable<ApplicationReportModel> reports = service.findAllByProperty(ReportModel.TEMPLATE_PATH,
                CreateDependencyGraphReportRuleProvider.TEMPLATE);
        for (ApplicationReportModel report : reports)
        {
            if (reportFilename.equals(report.getReportFilename()))
                return getPathForReport(graphContext, report);
        }
        return null;
    }

    /**
     * Stores the info about incoming calls which the tests can review.
     */
    protected static class RecordingWindupProgressMonitor implements WindupProgressMonitor
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
