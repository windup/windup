package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.ip.CreateStaticIPAddressReportRuleProvider;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateJavaApplicationOverviewReportRuleProvider;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
import org.jboss.windup.testutil.html.TestStaticIPReportUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureMediumBinaryModeTest extends WindupArchitectureTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-tattletale"),
                @AddonDependency(name = "org.jboss.windup.tests:test-util"),
                @AddonDependency(name = "org.jboss.windup.ext:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(WindupArchitectureTest.class)
                    .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java-ee"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-tattletale"),
                                AddonDependencyEntry.create("org.jboss.windup.tests:test-util"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Test
    public void testRunWindupMedium() throws Exception
    {
        final String path = "../test-files/Windup1x-javaee-example.war";

        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, path, false);
            validateManifestEntries(context);
            validateReports(context);
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
            if (manifest.getArchive().getFileName().equals("Windup1x-javaee-example.war"))
            {
                Assert.assertEquals("1.0", manifest.asVertex().getProperty("Manifest-Version"));
                Assert.assertEquals("Plexus Archiver", manifest.asVertex().getProperty("Archiver-Version"));
                Assert.assertEquals("Apache Maven", manifest.asVertex().getProperty("Created-By"));
                warManifestFound = true;
            }

            numberFound++;
        }
        Assert.assertEquals(9, numberFound);
        Assert.assertTrue(warManifestFound);
    }

    private void validateStaticIPReport(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateStaticIPAddressReportRuleProvider.TEMPLATE_REPORT);
        TestStaticIPReportUtil util = new TestStaticIPReportUtil();
        Path reportPath = Paths.get(reportService.getReportDirectory(), reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util
                    .checkStaticIPInReport(
                                "org.apache.wicket.protocol.http.mock.MockHttpServletRequest",
                                "Line Number 140, Column Number 25", "127.0.0.1"));
        Assert.assertTrue(util
                    .checkStaticIPInReport(
                                "org.apache.wicket.protocol.http.mock.MockHttpServletRequest",
                                "Line Number 653, Column Number 16", "127.0.0.1"));
        Assert.assertTrue(util
                    .checkStaticIPInReport(
                                "org.apache.wicket.protocol.http.mock.MockHttpServletRequest",
                                "Line Number 656, Column Number 16", "127.0.0.1"));
    }

    /**
     * Validate that the report pages were generated correctly
     */
    private void validateReports(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateJavaApplicationOverviewReportRuleProvider.TEMPLATE_APPLICATION_REPORT);
        Path appReportPath = Paths.get(reportService.getReportDirectory(), reportModel.getReportFilename());

        TestJavaApplicationOverviewUtil util = new TestJavaApplicationOverviewUtil();
        util.loadPage(appReportPath);
        util.checkFilePathAndTag("Windup1x-javaee-example.war", "META-INF/maven/javaee/javaee/pom.properties",
                    "Properties");
        util.checkFilePathEffort("Windup1x-javaee-example.war", "META-INF/maven/javaee/javaee/pom.properties", 0);
        util.checkFilePathEffort("Windup1x-javaee-example.war/WEB-INF/lib/joda-time-2.0.jar",
                    "org.joda.time.tz.DateTimeZoneBuilder", 32);
        validateStaticIPReport(context);
    }
}
