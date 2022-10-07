package org.jboss.windup.tests.application;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.testutil.html.TestDependencyReportUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

@RunWith(Arquillian.class)
public class WindupArchitectureDependencyTest extends WindupArchitectureTest {

    private static final String[] FOUND_PATH_LIB = {
            "application-with-dependencies.ear/lib/example-0-1.0.0.jar"
    };

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Test
    public void testRunWindupDependencies() throws Exception {
        try (GraphContext context = createGraphContext()) {
            super.runTest(context, "../test-files/application-with-dependencies.ear", false, Collections.emptyList());
            validateDependenciesReport(context);
        }
    }

    private void validateDependenciesReport(GraphContext context) {
        ReportService reportService = new ReportService(context);
        ReportModel dependencyReportModel = getJarDependencyReport(context);
        Path dependencyReport = reportService.getReportDirectory().resolve(dependencyReportModel.getReportFilename());
        TestDependencyReportUtil dependencyReportUtil = new TestDependencyReportUtil();
        dependencyReportUtil.loadPage(dependencyReport);
        Assert.assertTrue(dependencyReportUtil.findDependencyElement("example-0-1.0.0.jar",
                "example-0:test:1.0.0", "9e9944d81b31d376643f100775aba3d0b83210ef", "1.0.0", "",
                Arrays.asList(FOUND_PATH_LIB)));
    }
}
