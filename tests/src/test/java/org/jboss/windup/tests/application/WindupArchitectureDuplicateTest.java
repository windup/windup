package org.jboss.windup.tests.application;

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
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.MigrationIssuesReportModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.rules.CreateApplicationListReportRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.dependencyreport.CreateDependencyReportRuleProvider;
import org.jboss.windup.rules.apps.java.model.JavaApplicationOverviewReportModel;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateReportIndexRuleProvider;
import org.jboss.windup.testutil.html.CheckFailedException;
import org.jboss.windup.testutil.html.TestApplicationListUtil;
import org.jboss.windup.testutil.html.TestDependencyReportUtil;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
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
    private static final String MAIN_APP_FILENAME = "duplicate-ear-test-1.ear";
    private static final String SECOND_APP_FILENAME = "duplicate-ear-test-2.ear";
    private static final String THIRD_APP_FILENAME = "duplicate-ear-test-3.ear";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-project"),
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
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"))
                .addAsResource(new File("src/test/xml/DuplicateTestRules.windup.xml"));
    }

    @Test
    public void testRunWindupDuplicateEAR() throws Exception
    {
        final String path1 = "../test-files/duplicate/" + MAIN_APP_FILENAME;
        final String path2 = "../test-files/duplicate/" + SECOND_APP_FILENAME;
        final String path3 = "../test-files/duplicate/" + THIRD_APP_FILENAME;
        final Path outputPath = getDefaultPath();

        try (GraphContext context = createGraphContext(outputPath))
        {
            List<String> inputPaths = Arrays.asList(path1, path2, path3);

            super.runTest(context, inputPaths, false);
            validateApplicationList(context);
            validateReportIndex(context);
            validateMigrationIssues(context);
            validateJarDependencyReport(context);
            validateOverviewReport(context);
        } finally
        {
            //FileUtils.deleteDirectory(testTempPath.toFile());
        }
    }

    private void validateOverviewReport(GraphContext context) {
        ReportService reportService = new ReportService(context);
        Iterable<ReportModel> reportModels = getApplicationDetailsReports(context);

        ReportModel report = null;
        for (ReportModel reportModel : reportModels)
        {
            if (!(reportModel instanceof JavaApplicationOverviewReportModel))
                continue;

            JavaApplicationOverviewReportModel javaReport = (JavaApplicationOverviewReportModel)reportModel;
            if ("duplicate-ear-test-1.ear".equals(javaReport.getProjectModel().getRootFileModel().getFileName()))
            {
                report = javaReport;
                break;
            }
        }
        if (report == null)
            throw new CheckFailedException("Could not find expected overview report!");

        Path appReportPath = reportService.getReportDirectory().resolve(report.getReportFilename());
        TestJavaApplicationOverviewUtil util = new TestJavaApplicationOverviewUtil();
        util.loadPage(appReportPath);
        util.checkApplicationMessage("log4j reference found");
    }

    private void validateApplicationList(GraphContext graphContext)
    {
        Service<ReportModel> service = graphContext.service(ReportModel.class);
        ReportModel report = service.getUniqueByProperty(ReportModel.TEMPLATE_PATH, CreateApplicationListReportRuleProvider.TEMPLATE_PATH);
        Assert.assertNotNull(report);

        Path reportPath = getPathForReport(graphContext, report);
        Assert.assertNotNull(reportPath);

        TestApplicationListUtil util = new TestApplicationListUtil();
        util.loadPage(reportPath);

        Assert.assertEquals(636, util.getTotalStoryPoints(MAIN_APP_FILENAME));
        Assert.assertEquals(584, util.getSharedStoryPoints(MAIN_APP_FILENAME));
        Assert.assertEquals(52, util.getUniqueStoryPoints(MAIN_APP_FILENAME));

        Assert.assertEquals(636, util.getTotalStoryPoints(SECOND_APP_FILENAME));
        Assert.assertEquals(584, util.getSharedStoryPoints(SECOND_APP_FILENAME));
        Assert.assertEquals(52, util.getUniqueStoryPoints(SECOND_APP_FILENAME));

        Assert.assertEquals(576, util.getTotalStoryPoints(THIRD_APP_FILENAME));
        Assert.assertEquals(576, util.getSharedStoryPoints(THIRD_APP_FILENAME));
        Assert.assertEquals(0, util.getUniqueStoryPoints(THIRD_APP_FILENAME));

        Assert.assertEquals(584, util.getTotalStoryPoints(ProjectService.SHARED_LIBS_APP_NAME));
    }

    private void validateReportIndex(GraphContext graphContext)
    {
        Path mainReportPath = getReportIndex(graphContext, MAIN_APP_FILENAME);
        Assert.assertNotNull(mainReportPath);

        Path secondAppPath = getReportIndex(graphContext, SECOND_APP_FILENAME);
        Assert.assertNotNull(secondAppPath);

        Path sharedLibsPath = getReportIndex(graphContext, ProjectService.SHARED_LIBS_FILENAME);
        Assert.assertNotNull(sharedLibsPath);

        TestReportIndexReportUtil reportIndex = new TestReportIndexReportUtil();

        reportIndex.loadPage(mainReportPath);
        // After removed duplicated classifications with XML rules, this goes away
        //Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("mandatory", 2, 0));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("optional", 89, 636));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("potential", 0, 0));

        reportIndex.loadPage(secondAppPath);
        // After removed duplicated classifications with XML rules, this goes away
        //Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("mandatory", 2, 0));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("optional", 89, 636));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("potential", 0, 0));

        reportIndex.loadPage(sharedLibsPath);
        // After removed duplicated classifications with XML rules, this goes away
        //Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("mandatory", 2, 0));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("optional", 85, 584));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("potential", 0, 0));
    }

    private void validateJarDependencyReport(GraphContext graphContext)
    {
        Path dependencyReport = getDependencyReportPath(graphContext);
        Assert.assertNotNull(dependencyReport);
        TestDependencyReportUtil dependencyReportUtil = new TestDependencyReportUtil();
        dependencyReportUtil.loadPage(dependencyReport);
        Assert.assertEquals(9, dependencyReportUtil.getNumberOfJarsOnPage());
        Assert.assertEquals(6, dependencyReportUtil.getNumberOfArchivePathsOnPage("log4j-1.2.6.jar"));
        Assert.assertEquals(3, dependencyReportUtil.getNumberOfArchivePathsOnPage("jee-example-services.jar"));
        Assert.assertTrue(dependencyReportUtil.findDependencyElement("jee-example-services.jar", "JEE Example EJB Services",
                    "org.windup.example:jee-example-services:1.0.0", "d910370c02710f4bb7f7856e18f50803f1c37e16", "1.0.0", "",
                    Arrays.asList(FOUND_PATHS_JEE_EXAMPLE_SERVICES)));
        Assert.assertTrue(dependencyReportUtil.findDependencyElement("commons-lang-2.5.jar", "Commons Lang",
                    "commons-lang:commons-lang:2.5", "b0236b252e86419eef20c31a44579d2aee2f0a69", "2.5", "The Apache Software Foundation",
                    Arrays.asList(FOUND_PATHS_COMMONS_LANG)));
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
            else if (inputFile.getFileName().equals(SECOND_APP_FILENAME))
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
        Service<ApplicationReportModel> service = graphContext.service(ApplicationReportModel.class);
        Iterable<ApplicationReportModel> reports = service.findAllByProperty(ReportModel.TEMPLATE_PATH,
                CreateReportIndexRuleProvider.TEMPLATE);

        for (ApplicationReportModel report : reports)
        {
            if (StringUtils.equals(applicationFilename, report.getProjectModel().getRootFileModel().getFileName()))
                return getPathForReport(graphContext, report);
        }
        return null;
    }

    private static final String[] FOUND_PATHS_JEE_EXAMPLE_SERVICES = {
        "duplicate-ear-test-1.ear/jee-example-services.jar",
        "duplicate-ear-test-2.ear/jee-example-services.jar",
        "duplicate-ear-test-3.ear/jee-example-services.jar"};

    private static final String[] FOUND_PATHS_COMMONS_LANG = {
            "duplicate-ear-test-1.ear/jee-example-web.war/WEB-INF/lib/commons-lang-2.5.jar",
            "duplicate-ear-test-2.ear/jee-example-web.war/WEB-INF/lib/commons-lang-2.5.jar",
            "duplicate-ear-test-3.ear/jee-example-web.war/WEB-INF/lib/commons-lang-2.5.jar",
    };

    private Path getDependencyReportPath(GraphContext graphContext)
    {
        Service<ApplicationReportModel> service = graphContext.service(ApplicationReportModel.class);
        Iterable<ApplicationReportModel> reports = service.findAllByProperty(ReportModel.TEMPLATE_PATH,
                    CreateDependencyReportRuleProvider.TEMPLATE);
        for (ApplicationReportModel report : reports)
        {
            // test checks only Global Jar Dependencies report 
            if ("dependency_report_global.html".equals(report.getReportFilename()))
                return getPathForReport(graphContext, report);
        }
        return null;
    }

    private Path getPathForReport(GraphContext graphContext, ReportModel report)
    {
        return new ReportService(graphContext).getReportDirectory().resolve(report.getReportFilename());
    }
}
