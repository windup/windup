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
import org.jboss.windup.testutil.html.TestDependencyGraphReportUtil;
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
public class WindupArchitectureDuplicateTest extends WindupArchitectureTest {
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
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"))
                .addAsResource(new File("src/test/xml/DuplicateTestRules.windup.xml"))
                .addAsResource(new File("src/test/xml/rules/embedded-libraries/embedded-cache-libraries.windup.xml"))
                .addAsResource(new File("src/test/xml/rules/embedded-libraries/embedded-framework-libraries.windup.xml"));
    }

    @Test
    public void testRunWindupDuplicateEAR() throws Exception {
        final String path1 = "../test-files/duplicate/" + MAIN_APP_FILENAME;
        final String path2 = "../test-files/duplicate/" + SECOND_APP_FILENAME;
        final String path3 = "../test-files/duplicate/" + THIRD_APP_FILENAME;
        final Path outputPath = getDefaultPath();

        try (GraphContext context = createGraphContext(outputPath)) {
            List<String> inputPaths = Arrays.asList(path1, path2, path3);

            super.runTest(context, true, inputPaths, false);
            validateApplicationList(context);
            validateReportIndex(context);
            validateMigrationIssues(context);
            validateJarDependencyReport(context);
            validateJarDependencyGraphReport(context);
            validateOverviewReport(context);
        } finally {
            //FileUtils.deleteDirectory(testTempPath.toFile());
        }
    }

    private void validateOverviewReport(GraphContext context) {
        ReportService reportService = new ReportService(context);
        Iterable<ReportModel> reportModels = getApplicationDetailsReports(context);

        ReportModel report = null;
        for (ReportModel reportModel : reportModels) {
            if (!(reportModel instanceof JavaApplicationOverviewReportModel))
                continue;

            JavaApplicationOverviewReportModel javaReport = (JavaApplicationOverviewReportModel) reportModel;
            if ("duplicate-ear-test-1.ear".equals(javaReport.getProjectModel().getRootFileModel().getFileName())) {
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

    private void validateApplicationList(GraphContext graphContext) {
        Service<ReportModel> service = graphContext.service(ReportModel.class);
        ReportModel report = service.getUniqueByProperty(ReportModel.TEMPLATE_PATH, CreateApplicationListReportRuleProvider.TEMPLATE_PATH);
        Assert.assertNotNull(report);

        Path reportPath = getPathForReport(graphContext, report);
        Assert.assertNotNull(reportPath);

        TestApplicationListUtil util = new TestApplicationListUtil();
        util.loadPage(reportPath);

        Assert.assertEquals(649, util.getTotalStoryPoints(MAIN_APP_FILENAME));
        Assert.assertEquals(597, util.getSharedStoryPoints(MAIN_APP_FILENAME));
        Assert.assertEquals(52, util.getUniqueStoryPoints(MAIN_APP_FILENAME));

        Assert.assertEquals(649, util.getTotalStoryPoints(SECOND_APP_FILENAME));
        Assert.assertEquals(597, util.getSharedStoryPoints(SECOND_APP_FILENAME));
        Assert.assertEquals(52, util.getUniqueStoryPoints(SECOND_APP_FILENAME));

        Assert.assertEquals(589, util.getTotalStoryPoints(THIRD_APP_FILENAME));
        Assert.assertEquals(589, util.getSharedStoryPoints(THIRD_APP_FILENAME));
        Assert.assertEquals(0, util.getUniqueStoryPoints(THIRD_APP_FILENAME));

        Assert.assertEquals(597, util.getTotalStoryPoints(ProjectService.SHARED_LIBS_APP_NAME));
    }

    private void validateReportIndex(GraphContext graphContext) {
        Path mainReportPath = getReportIndex(graphContext, MAIN_APP_FILENAME);
        Assert.assertNotNull(mainReportPath);

        Path secondAppPath = getReportIndex(graphContext, SECOND_APP_FILENAME);
        Assert.assertNotNull(secondAppPath);

        Path sharedLibsPath = getReportIndex(graphContext, ProjectService.SHARED_LIBS_FILENAME);
        Assert.assertNotNull(sharedLibsPath);

        TestReportIndexReportUtil reportIndex = new TestReportIndexReportUtil();

        reportIndex.loadPage(mainReportPath);
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("mandatory", 1, 3));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("optional", 80, 636));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("potential", 0, 0));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("cloud-mandatory", 2, 10));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("information", 11, 0));

        reportIndex.loadPage(secondAppPath);
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("mandatory", 1, 3));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("optional", 80, 636));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("potential", 0, 0));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("cloud-mandatory", 2, 10));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("information", 11, 0));

        reportIndex.loadPage(sharedLibsPath);
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("mandatory", 1, 3));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("optional", 77, 584));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("potential", 0, 0));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("cloud-mandatory", 2, 10));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("information", 10, 0));
    }

    private void validateJarDependencyReport(GraphContext graphContext) {
        Path dependencyReport = getDependencyReportPath(graphContext);
        Assert.assertNotNull(dependencyReport);
        TestDependencyReportUtil dependencyReportUtil = new TestDependencyReportUtil();
        dependencyReportUtil.loadPage(dependencyReport);
        Assert.assertEquals(11, dependencyReportUtil.getNumberOfJarsOnPage());
        Assert.assertEquals(6, dependencyReportUtil.getNumberOfArchivePathsOnPage("log4j-1.2.6.jar"));
        Assert.assertEquals(3, dependencyReportUtil.getNumberOfArchivePathsOnPage("jee-example-services.jar"));
        Assert.assertEquals(3, dependencyReportUtil.getNumberOfArchivePathsOnPage("ehcache-1.6.2.jar"));
        Assert.assertEquals(3, dependencyReportUtil.getNumberOfArchivePathsOnPage("hibernate-ehcache-3.6.9.Final.jar"));
        Assert.assertTrue(dependencyReportUtil.findDependencyElement("jee-example-services.jar",
                "org.windup.example:jee-example-services:1.0.0", "1c603ef950e920769c3389e6a1282e3c96f360d0", "1.0.0", "",
                Arrays.asList(FOUND_PATHS_JEE_EXAMPLE_SERVICES)));
        Assert.assertTrue(dependencyReportUtil.findDependencyElement("commons-lang-2.5.jar",
                "commons-lang:commons-lang:2.5", "07df6997525697b211367cf7f359e5232ec65375", "2.5", "The Apache Software Foundation",
                Arrays.asList(FOUND_PATHS_COMMONS_LANG)));
        Assert.assertTrue(dependencyReportUtil.findDependencyElement("ehcache-1.6.2.jar",
                "net.sf.ehcache:ehcache:1.6.2", "a89c93852c8e1da630bfe9ac292abbc346c242dc", "1.6.2", "",
                Arrays.asList(FOUND_PATHS_EHCACHE)));
        Assert.assertTrue(dependencyReportUtil.findDependencyElement("hibernate-ehcache-3.6.9.Final.jar",
                "org.hibernate:hibernate-ehcache:3.6.9.Final", "ef67aaff9ded441f83ea49c6ae1e3d245d9cacae", "3.6.9.Final", "Hibernate.org",
                Arrays.asList(FOUND_PATHS_HIBERNATE_EHCACHE)));
    }

    private void validateJarDependencyGraphReport(GraphContext graphContext) {
        Path dependencyReport = getGlobalDependencyGraphReportPath(graphContext);
        Assert.assertNotNull(dependencyReport);
        TestDependencyGraphReportUtil dependencyGraphReportUtil = new TestDependencyGraphReportUtil();
        dependencyGraphReportUtil.loadPage(dependencyReport);
        Assert.assertEquals(18, dependencyGraphReportUtil.getNumberOfArchivesInTheGraph());
        Assert.assertEquals(1, dependencyGraphReportUtil.getNumberOfArchivesInTheGraphByName("duplicate-ear-test-3.ear"));
        Assert.assertEquals(1, dependencyGraphReportUtil.getNumberOfArchivesInTheGraphByName("log4j-1.2.6.jar"));
        Assert.assertEquals(1, dependencyGraphReportUtil.getNumberOfArchivesInTheGraphByName("jee-example-services2.jar"));
        Assert.assertEquals(34, dependencyGraphReportUtil.getNumberOfRelationsInTheGraph());
    }

    private void validateMigrationIssues(GraphContext graphContext) {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(graphContext);
        ProjectModel mainProject = null;
        ProjectModel copyProject = null;
        for (FileModel inputFile : configurationModel.getInputPaths()) {
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
        Assert.assertTrue(migrationIssuesReportUtil.checkIssue("Maven POM (pom.xml)", 8, 0, "Info", 0));
        Assert.assertTrue(migrationIssuesReportUtil.checkIssue("Unparsable XML File", 2, 0, "Info", 0));

        migrationIssuesReportUtil.loadPage(getPathForReport(graphContext, copyIssuesReportModel));
        Assert.assertTrue(migrationIssuesReportUtil.checkIssue("Maven POM (pom.xml)", 8, 0, "Info", 0));
        Assert.assertTrue(migrationIssuesReportUtil.checkIssue("Unparsable XML File", 2, 0, "Info", 0));
    }

    private Path getReportIndex(GraphContext graphContext, String applicationFilename) {
        Service<ApplicationReportModel> service = graphContext.service(ApplicationReportModel.class);
        Iterable<ApplicationReportModel> reports = service.findAllByProperty(ReportModel.TEMPLATE_PATH,
                CreateReportIndexRuleProvider.TEMPLATE);

        for (ApplicationReportModel report : reports) {
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

    private static final String[] FOUND_PATHS_EHCACHE = {
            "duplicate-ear-test-1.ear/lib/ehcache-1.6.2.jar",
            "duplicate-ear-test-2.ear/lib/ehcache-1.6.2.jar",
            "duplicate-ear-test-3.ear/lib/ehcache-1.6.2.jar",
    };

    private static final String[] FOUND_PATHS_HIBERNATE_EHCACHE = {
            "duplicate-ear-test-1.ear/lib/hibernate-ehcache-3.6.9.Final.jar",
            "duplicate-ear-test-2.ear/lib/hibernate-ehcache-3.6.9.Final.jar",
            "duplicate-ear-test-3.ear/lib/hibernate-ehcache-3.6.9.Final.jar",
    };

    private Path getDependencyReportPath(GraphContext graphContext) {
        Service<ApplicationReportModel> service = graphContext.service(ApplicationReportModel.class);
        Iterable<ApplicationReportModel> reports = service.findAllByProperty(ReportModel.TEMPLATE_PATH,
                CreateDependencyReportRuleProvider.TEMPLATE);
        for (ApplicationReportModel report : reports) {
            // test checks only Global Jar Dependencies report 
            if ("dependency_report_global.html".equals(report.getReportFilename()))
                return getPathForReport(graphContext, report);
        }
        return null;
    }

}
