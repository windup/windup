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
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateReportIndexRuleProvider;
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
    private static final String MAIN_APP_FILENAME = "Windup1x-javaee-example.war";
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
        } finally
        {
            //FileUtils.deleteDirectory(testTempPath.toFile());
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
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Mandatory", 0, 0));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Optional", 3, 2));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Potential Issues", 0, 0));

        reportIndex.loadPage(copyAppPath);
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Mandatory", 0, 0));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Optional", 3, 2));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Potential Issues", 0, 0));

        reportIndex.loadPage(sharedLibsPath);
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Mandatory", 0, 0));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Optional", 289, 2208));
        Assert.assertTrue(reportIndex.checkIncidentByCategoryRow("Potential Issues", 0, 0));
    }

    private Path getReportIndex(GraphContext graphContext, String applicationFilename)
    {
        GraphService<ApplicationReportModel> service = graphContext.service(ApplicationReportModel.class);
        Iterable<ApplicationReportModel> reports = service.findAllByProperty(ReportModel.TEMPLATE_PATH,
                CreateReportIndexRuleProvider.TEMPLATE);

        for (ApplicationReportModel report : reports)
        {
            if (StringUtils.equals(applicationFilename, report.getProjectModel().getRootFileModel().getFileName()))
                return new ReportService(graphContext).getReportDirectory().resolve(report.getReportFilename());;
        }
        return null;
    }
}
