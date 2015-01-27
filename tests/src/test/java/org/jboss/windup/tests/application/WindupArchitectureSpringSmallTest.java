package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
import org.jboss.windup.rules.apps.java.reporting.rules.CreateJavaApplicationOverviewReportRuleProvider;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.model.SpringConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.rules.CreateSpringBeanReportRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.SpringConfigurationFileService;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
import org.jboss.windup.testutil.html.TestSpringBeanReportUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureSpringSmallTest extends WindupArchitectureTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java-ee"),
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
                                AddonDependencyEntry.create("org.jboss.windup.tests:test-util"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Test
    public void testRunWindupSmallSpringApp() throws Exception
    {
        try (GraphContext context = super.createGraphContext())
        {
            final String path = "../test-files/spring-small-example.war";

            List<String> includeList = Collections.singletonList("nocodescanning");
            List<String> excludeList = Collections.emptyList();
            super.runTest(context, path, null, false, includeList, excludeList);

            validateSpringBeans(context);
            validateReports(context);
            validateSpringBeanReport(context);
        }
    }

    /**
     * Validate that the spring beans were extracted correctly
     */
    private void validateSpringBeans(GraphContext context)
    {
        SpringConfigurationFileService springConfigurationFileService = new SpringConfigurationFileService(context);
        Iterable<SpringConfigurationFileModel> models = springConfigurationFileService.findAll();

        int numberFound = 0;
        boolean foundSpringMvcContext = false;
        boolean foundSpringBusinessContext = false;
        for (SpringConfigurationFileModel model : models)
        {
            numberFound++;
            if (model.getFileName().equals("spring-mvc-context.xml"))
            {
                foundSpringMvcContext = true;
                Iterator<SpringBeanModel> beanIter = model.getSpringBeans().iterator();
                SpringBeanModel springBean = beanIter.next();

                Assert.assertEquals("org.springframework.web.servlet.view.InternalResourceViewResolver", springBean
                            .getJavaClass().getQualifiedName());

                Assert.assertFalse(beanIter.hasNext());
            }
            else if (model.getFileName().equals("spring-business-context.xml"))
            {
                foundSpringBusinessContext = true;
                Assert.assertFalse(model.getSpringBeans().iterator().hasNext());
            }
        }
        Assert.assertEquals(2, numberFound);
        Assert.assertTrue(foundSpringMvcContext);
        Assert.assertTrue(foundSpringBusinessContext);
    }

    private void validateSpringBeanReport(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateSpringBeanReportRuleProvider.TEMPLATE_SPRING_REPORT);
        TestSpringBeanReportUtil util = new TestSpringBeanReportUtil();
        Path reportPath = Paths.get(reportService.getReportDirectory(), reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util.checkSpringBeanInReport("", "org.springframework.web.servlet.view.InternalResourceViewResolver"));
    }

    private void validateReports(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateJavaApplicationOverviewReportRuleProvider.TEMPLATE_APPLICATION_REPORT);
        Path appReportPath = Paths.get(reportService.getReportDirectory(), reportModel.getReportFilename());

        TestJavaApplicationOverviewUtil util = new TestJavaApplicationOverviewUtil();
        util.loadPage(appReportPath);
        util.checkFilePathAndTag("spring-small-example.war", "WEB-INF/spring-business-context.xml", "Spring XML");
    }
}
