package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.rules.CreateApplicationListReportRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;
import org.jboss.windup.testutil.html.TestApplicationListUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class WindupMultiAppBasicTest extends WindupArchitectureTest {
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
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Test
    public void testRunWindupMedium() throws Exception {
        final String path1 = "../test-files/Windup1x-javaee-example.war";
        final String path2 = "../test-files/maven-info-missing.war";
        final String path3 = "../test-files/badly_named_app";
        List<String> paths = new ArrayList<>();
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);

        try (GraphContext context = createGraphContext()) {
            super.runTest(context, true, paths, false);
            checkEJBDescriptors(context);
            checkWebXmls(context);
            validateApplicationList(context);
        }

    }

    private void validateApplicationList(GraphContext context) {
        Service<ReportModel> service = context.service(ReportModel.class);
        ReportModel report = service.getUniqueByProperty(ReportModel.TEMPLATE_PATH, CreateApplicationListReportRuleProvider.TEMPLATE_PATH);
        Assert.assertNotNull(report);

        Path reportPath = new ReportService(context).getReportDirectory().resolve(report.getReportFilename());
        Assert.assertNotNull(reportPath);

        TestApplicationListUtil util = new TestApplicationListUtil();
        util.loadPage(reportPath);

        validateSorting(util);
        validateTagFiltering(util);
    }

    private void validateTagFiltering(TestApplicationListUtil util) {
        List<String> applicationNames = util.getApplicationNames().stream()
                .filter(util::isDisplayed)
                .collect(Collectors.toList());

        Assert.assertEquals(3, applicationNames.size());

        util.clickTag("Windup1x-javaee-example.war", "bad regex(foo");

        applicationNames = util.getApplicationNames().stream()
                .filter(util::isDisplayed)
                .collect(Collectors.toList());
        Assert.assertEquals(2, applicationNames.size());
    }

    private void validateSorting(TestApplicationListUtil util) {
        List<String> presortedList = util.getApplicationNames();

        // Check that they are sorted by name
        Assert.assertEquals("badly_named_app", presortedList.get(0));
        Assert.assertEquals("maven-info-missing.war", presortedList.get(1));
        Assert.assertEquals("Windup1x-javaee-example.war", presortedList.get(2));

        util.sortApplicationListByEffortPoints();

        presortedList = util.getApplicationNames();
        // Check that they are sorted by points (same order)
        Assert.assertEquals("badly_named_app", presortedList.get(0));
        Assert.assertEquals("maven-info-missing.war", presortedList.get(1));
        Assert.assertEquals("Windup1x-javaee-example.war", presortedList.get(2));

        util.reverseSortOrder();

        presortedList = util.getApplicationNames();
        // Check that the order reversed
        Assert.assertEquals("Windup1x-javaee-example.war", presortedList.get(0));
        Assert.assertEquals("maven-info-missing.war", presortedList.get(1));
        Assert.assertEquals("badly_named_app", presortedList.get(2));

        util.reverseSortOrder();

        presortedList = util.getApplicationNames();
        // Check that they are back to descending order
        Assert.assertEquals("badly_named_app", presortedList.get(0));
        Assert.assertEquals("maven-info-missing.war", presortedList.get(1));
        Assert.assertEquals("Windup1x-javaee-example.war", presortedList.get(2));
    }

    private void checkEJBDescriptors(GraphContext context) {
        GraphService<EjbDeploymentDescriptorModel> ejbDescriptors = new GraphService<>(context, EjbDeploymentDescriptorModel.class);
        for (EjbDeploymentDescriptorModel ejbDeploymentDescriptorModel : ejbDescriptors.findAll()) {
            Assert.assertTrue(1 >= getIterableSize(ejbDeploymentDescriptorModel.getLinksToTransformedFiles()));
        }

    }

    private void checkWebXmls(GraphContext context) {
        GraphService<WebXmlModel> webXmls = new GraphService<>(context, WebXmlModel.class);
        for (WebXmlModel webXml : webXmls.findAll()) {
            Assert.assertTrue(1 >= getIterableSize(webXml.getLinksToTransformedFiles()));
        }
    }

    private int getIterableSize(Iterable<?> iterable) {
        int itemCount = 0;
        for (Object o : iterable) {
            itemCount++;
        }
        return itemCount;
    }

}
