package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;
import java.util.Properties;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.ip.CreateHardcodedIPAddressReportRuleProvider;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateCompatibleFileReportRuleProvider;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateReportIndexRuleProvider;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.jboss.windup.testutil.html.TestCompatibleReportUtil;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
import org.jboss.windup.testutil.html.TestReportIndexReportUtil;
import org.jboss.windup.testutil.html.TestHardcodedPReportUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureMediumBinaryModeTest extends WindupArchitectureTest
{

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
    public void testRunWindupMediumWithFernflower() throws Exception
    {
        final String path = "../test-files/Windup1x-javaee-example.war";

        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, path, false);
            allDecompiledFilesAreLinked(context);
            validateManifestEntries(context);
            validateReports(context);
        }

    }

    @Test
    public void testRunWindupMediumWithProcyon() throws Exception
    {
        final String path = "../test-files/Windup1x-javaee-example.war";
        try (GraphContext context = createGraphContext())
        {
            Properties props = System.getProperties();
            props.setProperty("windup.decompiler", "Procyon");
            super.runTest(context, path, false);
            props.remove("windup.decompiler");
            allDecompiledFilesAreLinked(context);
        }
    }

    private void validateManifestEntries(GraphContext context) throws Exception
    {
        JarManifestService jarManifestService = new JarManifestService(context);
        Iterable<JarManifestModel> manifests = jarManifestService.findAll();

        int numberFound = 0;
        boolean warManifestFound = false;
        for (JarManifestModel manifest : manifests)
        {
            if (manifest.getArchive().getFileName().equals("Windup1x-javaee-example.war") && !manifest.getFilePath().contains("/WEB-INF/"))
            {
                Assert.assertEquals("1.0", manifest.asVertex().getProperty("Manifest-Version"));
                Assert.assertEquals("Plexus Archiver", manifest.asVertex().getProperty("Archiver-Version"));
                Assert.assertEquals("Apache Maven", manifest.asVertex().getProperty("Created-By"));
                warManifestFound = true;
            }

            numberFound++;
        }
        Assert.assertEquals(10, numberFound);
        Assert.assertTrue(warManifestFound);
    }

    private void validateStaticIPReport(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateHardcodedIPAddressReportRuleProvider.TEMPLATE_REPORT);
        TestHardcodedPReportUtil util = new TestHardcodedPReportUtil();
        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util
                    .checkHardcodedIPInReport(
                                "org.apache.wicket.protocol.http.mock.MockHttpServletRequest (67, 32)",
                                "Line: 67, Position: 32", "127.0.0.1"));
        Assert.assertTrue(util
                    .checkHardcodedIPInReport(
                                "org.apache.wicket.protocol.http.mock.MockHttpServletRequest (723, 14)",
                                "Line: 723, Position: 14", "127.0.0.1"));
        Assert.assertTrue(util
                    .checkHardcodedIPInReport(
                                "org.apache.wicket.protocol.http.mock.MockHttpServletRequest (727, 14)",
                                "Line: 727, Position: 14", "127.0.0.1"));

    }

    private void validateCompatibleReport(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateCompatibleFileReportRuleProvider.TEMPLATE_APPLICATION_REPORT);
        TestCompatibleReportUtil util = new TestCompatibleReportUtil();


        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util
                    .checkFileInReport("org/jboss/devconf/openshift/HomePage.class", ""));
        Assert.assertTrue(util
                    .checkFileInReport("org/joda/time/DateMidnight.class", ""));
        Assert.assertTrue(util
                    .checkFileInReport(
                                "org/joda/time/Chronology.class", ""));
        Assert.assertTrue("An application has duplicate entries for a single file.",util.checkTableWithoutDuplicates());

    }

    private void validateOverviewReport(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = getMainApplicationReport(context);
        Path appReportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        TestJavaApplicationOverviewUtil util = new TestJavaApplicationOverviewUtil();
        util.loadPage(appReportPath);
        util.checkFilePathAndTag("Windup1x-javaee-example.war", "META-INF/maven/javaee/javaee/pom.properties",
                    "Properties");
        util.checkFilePathEffort("Windup1x-javaee-example.war", "META-INF/maven/javaee/javaee/pom.properties", 0);
        util.checkFilePathEffort("Windup1x-javaee-example.war/WEB-INF/lib/joda-time-2.0.jar",
                    "org.joda.time.tz.DateTimeZoneBuilder", 32);
        util.checkMainEffort(2213);
        util.checkAppSectionEffort("Windup1x-javaee-example.war", 2);
        util.checkAppSectionEffort("Windup1x-javaee-example.war/WEB-INF/lib/joda-time-2.0.jar", 64);
        util.checkAppSectionEffort("Windup1x-javaee-example.war/WEB-INF/lib/slf4j-api-1.6.1.jar", 16);
        util.checkAppSectionEffort("Windup1x-javaee-example.war/WEB-INF/lib/wicket-devutils-1.5.10.jar", 0);
        util.checkAppSectionEffort("Windup1x-javaee-example.war/WEB-INF/lib/wicket-request-1.5.10.jar", 24);
    }

    private void validateReportIndex(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateReportIndexRuleProvider.TEMPLATE);
        Path appReportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        TestReportIndexReportUtil util = new TestReportIndexReportUtil();
        util.loadPage(appReportPath);

        Assert.assertTrue(util.checkIncidentByCategoryRow("optional", 292, 2213));
    }

    /**
     * Validate that the report pages were generated correctly
     */
    private void validateReports(GraphContext context)
    {
        validateOverviewReport(context);
        validateStaticIPReport(context);
        validateCompatibleReport(context);
        validateReportIndex(context);
    }
}
