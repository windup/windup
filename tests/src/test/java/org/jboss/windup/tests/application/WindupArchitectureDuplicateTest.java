package org.jboss.windup.tests.application;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.MigrationIssuesReportModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateJarDependencyReportRuleProvider;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateReportIndexRuleProvider;
import org.jboss.windup.testutil.html.TestJarDependencyReportUtil;
import org.jboss.windup.testutil.html.TestMigrationIssuesReportUtil;
import org.jboss.windup.testutil.html.TestReportIndexReportUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class WindupArchitectureDuplicateTest extends WindupArchitectureTest
{
    private static final String MAIN_APP_FILENAME = "jee-example-app-1.0.0.ear";
    public static final String COPY_EAR_FILENAME = "copy.ear";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-tattletale"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Test
    public void testRunWindupDuplicateEAR() throws Exception
    {
        final String path = "../test-files/" + MAIN_APP_FILENAME;
        final Path testTempPath = getDefaultPath();
        final Path copyOfMainApp = testTempPath.resolve(COPY_EAR_FILENAME);
        FileUtils.copyFile(new File(path), copyOfMainApp.toFile());

        final Path outputPath = testTempPath.resolve("reports");

        try (GraphContext context = createGraphContext(outputPath))
        {
            List<String> inputPaths = Arrays.asList(path, copyOfMainApp.toString());

            super.runTest(context, inputPaths, false);
            validateReportIndex(context);
            validateMigrationIssues(context);
            validateJarDependencyReport(context);
        } finally
        {
            FileUtils.deleteDirectory(testTempPath.toFile());
        }
    }

    private void validateReportIndex(GraphContext graphContext)
    {
        Path mainReportPath = getReportIndex(graphContext, MAIN_APP_FILENAME);
        Assert.assertNotNull(mainReportPath);

        Path copyAppPath = getReportIndex(graphContext, COPY_EAR_FILENAME);
        Assert.assertNotNull(copyAppPath);

        Path sharedLibsPath = getReportIndex(graphContext, ProjectService.SHARED_LIBS_FILENAME);
        Assert.assertNotNull(sharedLibsPath);

        TestReportIndexReportUtil reportIndex = new TestReportIndexReportUtil();

        reportIndex.loadPage(mainReportPath);
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Mandatory", 2, 6));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Optional", 88, 584));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Potential Issues", 0, 0));

        reportIndex.loadPage(copyAppPath);
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Mandatory", 2, 6));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Optional", 88, 584));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Potential Issues", 0, 0));

        reportIndex.loadPage(sharedLibsPath);
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Mandatory", 2, 6));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Optional", 86, 584));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Potential Issues", 0, 0));
    }

    private void validateJarDependencyReport(GraphContext graphContext)
    {
        Path dependencyReport = getJarDependencyReportPath(graphContext);
        Assert.assertNotNull(dependencyReport);
        TestJarDependencyReportUtil jarDependencyReportUtil = new TestJarDependencyReportUtil();
        jarDependencyReportUtil.loadPage(dependencyReport);
        Assert.assertEquals(8, jarDependencyReportUtil.getNumberOfJarsOnPage());
        Assert.assertTrue(jarDependencyReportUtil.findDependencyElement("jee-example-services.jar", "JEE Example EJB Services", "org.windup.example:jee-example-services:1.0.0", null, "1.0.0", "", Arrays.asList(FOUND_PATHS) ) );
    }

    private void validateMigrationIssues(GraphContext graphContext)
    {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(graphContext);
        ProjectModel mainProject = null;
        ProjectModel copyProject = null;
        for (FileModel inputFile : configurationModel.getInputPaths())
        {
            if (inputFile.getFileName().equals(MAIN_APP_FILENAME))
                mainProject = inputFile.getProjectModel();
            else if (inputFile.getFileName().equals(COPY_EAR_FILENAME))
                copyProject = inputFile.getProjectModel();
        }
        Assert.assertNotNull(mainProject);
        Assert.assertNotNull(copyProject);

        MigrationIssuesReportModel mainIssuesReportModel = getMigrationIssuesReport(graphContext, mainProject);
        MigrationIssuesReportModel copyIssuesReportModel = getMigrationIssuesReport(graphContext, copyProject);

        TestMigrationIssuesReportUtil migrationIssuesReportUtil = new TestMigrationIssuesReportUtil();
        migrationIssuesReportUtil.loadPage(getPathForReport(graphContext, mainIssuesReportModel));
        Assert.assertTrue(migrationIssuesReportUtil.checkIssue("Maven POM", 6, 0, "Info", 0));
        Assert.assertTrue(migrationIssuesReportUtil.checkIssue("Unparsable XML File", 2, 0, "Info", 0));

        migrationIssuesReportUtil.loadPage(getPathForReport(graphContext, copyIssuesReportModel));
        Assert.assertTrue(migrationIssuesReportUtil.checkIssue("Maven POM", 6, 0, "Info", 0));
        Assert.assertTrue(migrationIssuesReportUtil.checkIssue("Unparsable XML File", 2, 0, "Info", 0));
    }

    private Path getReportIndex(GraphContext graphContext, String applicationFilename)
    {
        GraphService<ApplicationReportModel> service = graphContext.service(ApplicationReportModel.class);
        Iterable<ApplicationReportModel> reports = service.findAllByProperty(ReportModel.TEMPLATE_PATH,
                CreateReportIndexRuleProvider.TEMPLATE);

        for (ApplicationReportModel report : reports)
        {
            if (StringUtils.equals(applicationFilename, report.getProjectModel().getRootFileModel().getFileName()))
                return getPathForReport(graphContext, report);
        }
        return null;
    }

    private static final String[] FOUND_PATHS = {
        "copy.ear/jee-example-services.jar",
        "jee-example-app-1.0.0.ear/jee-example-services.jar"};

    private Path getJarDependencyReportPath(GraphContext graphContext)
    {
        GraphService<ApplicationReportModel> service = graphContext.service(ApplicationReportModel.class);
        Iterable<ApplicationReportModel> reports = service.findAllByProperty(ReportModel.TEMPLATE_PATH,
                    CreateJarDependencyReportRuleProvider.TEMPLATE);
        for (ApplicationReportModel report : reports)
        {
            // test checks only Global Jar Dependencies report 
            if ("dependency_report.html".equals(report.getReportFilename()))
                return getPathForReport(graphContext, report);
        }
        return null;
    }

    private Path getPathForReport(GraphContext graphContext, ReportModel report)
    {
        return new ReportService(graphContext).getReportDirectory().resolve(report.getReportFilename());
    }
}
