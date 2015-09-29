package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateMappingFileModel;
import org.jboss.windup.rules.apps.javaee.rules.CreateHibernateReportRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.HibernateConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.HibernateMappingFileService;
import org.jboss.windup.testutil.html.TestHibernateReportUtil;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureHibernateTest extends WindupArchitectureTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.windup.tests:test-util"),
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
    public void testRunWindupHibernate() throws Exception
    {
        final String path = "../test-files/hibernate-tutorial-web-3.3.2.GA.war";
        List<String> includeList = Collections.singletonList("nocodescanning");
        List<String> excludeList = Collections.emptyList();
        try (GraphContext context = super.createGraphContext())
        {
            super.runTest(context, path, null, false, includeList, excludeList);
            validateHibernateFiles(context);
            validateReports(context);
        }
    }

    private void validateHibernateFiles(GraphContext context)
    {
        HibernateConfigurationFileService cfgService = new HibernateConfigurationFileService(context);

        int hibernateCfgFilesFound = 0;
        for (HibernateConfigurationFileModel model : cfgService.findAll())
        {
            Assert.assertEquals("3.0", model.getSpecificationVersion());
            hibernateCfgFilesFound++;
        }
        Assert.assertEquals(1, hibernateCfgFilesFound);

        HibernateMappingFileService mappingService = new HibernateMappingFileService(context);
        boolean personHbmFound = false;
        boolean eventHbmFound = false;
        boolean itemHbmFound = false;
        int numberModelsFound = 0;
        Iterable<HibernateMappingFileModel> allMappingModels = mappingService.findAll();
        for (HibernateMappingFileModel model : allMappingModels)
        {
            numberModelsFound++;
            Assert.assertEquals("3.0", model.getSpecificationVersion());
            if (model.getFileName().equals("Person.hbm.xml"))
            {
                personHbmFound = true;
                Iterator<HibernateEntityModel> entities = model.getHibernateEntities().iterator();
                Assert.assertTrue(entities.hasNext());

                HibernateEntityModel entity = entities.next();
                Assert.assertEquals("3.0", entity.getSpecificationVersion());
                Assert.assertEquals("PERSON", entity.getTableName());
                Assert.assertEquals("org.hibernate.tutorial.domain.Person", entity.getJavaClass().getQualifiedName());

                Assert.assertFalse(entities.hasNext());
            }
            else if (model.getFileName().equals("Event.hbm.xml"))
            {
                eventHbmFound = true;

                Iterator<HibernateEntityModel> entities = model.getHibernateEntities().iterator();
                Assert.assertTrue(entities.hasNext());

                HibernateEntityModel entity = entities.next();
                Assert.assertEquals("3.0", entity.getSpecificationVersion());
                Assert.assertEquals("EVENTS", entity.getTableName());
                Assert.assertEquals("org.hibernate.tutorial.domain.Event", entity.getJavaClass().getQualifiedName());

                Assert.assertFalse(entities.hasNext());
            }
            else if (model.getFileName().equals("Item.hbm.xml"))
            {
                itemHbmFound = true;

                Iterator<HibernateEntityModel> entities = model.getHibernateEntities().iterator();
                Assert.assertTrue(entities.hasNext());

                HibernateEntityModel entity = entities.next();
                Assert.assertEquals("3.0", entity.getSpecificationVersion());
                Assert.assertEquals("Items", entity.getTableName());
                Assert.assertEquals("org.hibernate.test.cache.Item", entity.getJavaClass().getQualifiedName());

                Assert.assertFalse(entities.hasNext());
            }
        }
        Assert.assertTrue(personHbmFound);
        Assert.assertTrue(eventHbmFound);
        Assert.assertTrue(itemHbmFound);
        Assert.assertEquals(3, numberModelsFound);
    }

    private void validateHibernateReport(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateHibernateReportRuleProvider.TEMPLATE_HIBERNATE_REPORT);
        TestHibernateReportUtil util = new TestHibernateReportUtil();
        Path reportPath = Paths.get(reportService.getReportDirectory(), reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util.checkSessionFactoryPropertyInReport("connection.pool_size", "2"));
        Assert.assertTrue(util.checkSessionFactoryPropertyInReport("cache.provider_class", "org.hibernate.cache.NoCacheProvider"));
        Assert.assertTrue(util.checkSessionFactoryPropertyInReport("dialect", "org.hibernate.dialect.HSQLDialect"));
        Assert.assertTrue(util.checkSessionFactoryPropertyInReport("current_session_context_class", "org.hibernate.context.ManagedSessionContext"));

        Assert.assertTrue(util.checkHibernateEntityInReport("org.hibernate.test.cache.Item", "Items"));
        Assert.assertTrue(util.checkHibernateEntityInReport("org.hibernate.tutorial.domain.Person", "PERSON"));
        Assert.assertTrue(util.checkHibernateEntityInReport("org.hibernate.tutorial.domain.Event", "EVENTS"));
    }

    private void validateReports(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = getMainApplicationReport(context);
        Path appReportPath = Paths.get(reportService.getReportDirectory(), reportModel.getReportFilename());

        TestJavaApplicationOverviewUtil util = new TestJavaApplicationOverviewUtil();
        util.loadPage(appReportPath);
        util.checkFilePathAndTag("hibernate-tutorial-web-3.3.2.GA.war", "META-INF/MANIFEST.MF", "Manifest");
        util.checkFilePathAndTag("hibernate-tutorial-web-3.3.2.GA.war", "WEB-INF/classes/hibernate.cfg.xml",
                    "Hibernate Cfg");
        util.checkFilePathAndTag("hibernate-tutorial-web-3.3.2.GA.war",
                    "WEB-INF/classes/org/hibernate/tutorial/domain/Event.hbm.xml", "Hibernate Mapping");

        validateHibernateReport(context);
    }
}
