package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

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
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.rules.CreateEJBReportRuleProvider;
import org.jboss.windup.testutil.html.TestEJBReportUtil;
import org.jboss.windup.testutil.html.TestEJBReportUtil.EJBType;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureJEEExampleTest extends WindupArchitectureTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.windup.tests:test-util"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
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
    public void testRunWindupJEEExampleMode() throws Exception
    {
        try (GraphContext context = super.createGraphContext())
        {
            // The test-files folder in the project root dir.
            super.runTest(context, "../test-files/jee-example-app-1.0.0.ear", false);

            validateEjbXmlReferences(context);
            validateReports(context);
            validateParentOfSourceReports(context);
        }
    }

    /**
     * Validate that a ejb-jar.xml file was found, and that the metadata was extracted correctly
     */
    private void validateEjbXmlReferences(GraphContext context)
    {
        Service<EjbDeploymentDescriptorModel> ejbXmlService = new GraphService<>(context,
                    EjbDeploymentDescriptorModel.class);
        Iterator<EjbDeploymentDescriptorModel> models = ejbXmlService.findAll().iterator();

        // There should be at least one file
        Assert.assertTrue(models.hasNext());
        EjbDeploymentDescriptorModel model = models.next();

        // and only two files
        EjbDeploymentDescriptorModel model2 = models.next();
        Assert.assertFalse(models.hasNext());

        // We don't know which one will come first, and the beans are only in one of them.
        if (!model.getEjbSessionBeans().iterator().hasNext())
            model = model2;

        int sessionBeansFound = 0;
        for (EjbSessionBeanModel sessionBean : model.getEjbSessionBeans())
        {
            if (sessionBean.getBeanName().equals("ItemLookupBean"))
            {
                Assert.assertEquals("com.acme.anvil.service.ItemLookupHome", sessionBean.getEjbHome()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookup", sessionBean.getEjbRemote().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupLocalHome", sessionBean.getEjbLocalHome()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupLocal", sessionBean.getEjbLocal()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupBean", sessionBean.getEjbClass()
                            .getQualifiedName());
                Assert.assertEquals("Stateless", sessionBean.getSessionType());
                Assert.assertEquals("Container", sessionBean.getTransactionType());
            }
            else if (sessionBean.getBeanName().equals("ProductCatalogBean"))
            {
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogHome", sessionBean.getEjbHome()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalog", sessionBean.getEjbRemote()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogLocalHome", sessionBean.getEjbLocalHome()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogLocal", sessionBean.getEjbLocal()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogBean", sessionBean.getEjbClass()
                            .getQualifiedName());
                Assert.assertEquals("Stateless", sessionBean.getSessionType());
                Assert.assertEquals("Container", sessionBean.getTransactionType());
            }
            else
            {
                Assert.fail("Unrecognized session bean found: " + sessionBean.getBeanName());
            }
            sessionBeansFound++;
        }
        Assert.assertEquals(2, sessionBeansFound);

        int messageDrivenFound = 0;
        for (EjbMessageDrivenModel messageDriven : model.getMessageDriven())
        {
            Assert.assertEquals("LogEventSubscriber", messageDriven.getBeanName());
            Assert.assertEquals("com.acme.anvil.service.jms.LogEventSubscriber", messageDriven.getEjbClass()
                        .getQualifiedName());
            Assert.assertEquals("Container", messageDriven.getTransactionType());
            messageDrivenFound++;
        }
        Assert.assertEquals(1, messageDrivenFound);
    }

    private void validateEJBBeanReport(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateEJBReportRuleProvider.TEMPLATE_EJB_REPORT);
        TestEJBReportUtil util = new TestEJBReportUtil();
        Path reportPath = Paths.get(reportService.getReportDirectory(), reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util.checkBeanInReport(EJBType.MDB, "LogEventSubscriber", "com.acme.anvil.service.jms.LogEventSubscriber"));
        Assert.assertTrue(util.checkBeanInReport(EJBType.STATELESS, "ItemLookupBean", "LocalRemote", "com.acme.anvil.service.ItemLookupBean"));
        Assert.assertTrue(util.checkBeanInReport(EJBType.STATELESS, "ProductCatalogBean", "LocalRemote", "com.acme.anvil.service.ProductCatalogBean"));
    }

    private void validateReports(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = getMainApplicationReport(context);
        Path appReportPath = Paths.get(reportService.getReportDirectory(), reportModel.getReportFilename());

        TestJavaApplicationOverviewUtil util = new TestJavaApplicationOverviewUtil();
        util.loadPage(appReportPath);
        util.checkFilePathAndTag("jee-example-app-1.0.0.ear/jee-example-services.jar", "META-INF/ejb-jar.xml",
                    "EJB XML 2.1");

        validateEJBBeanReport(context);
    }

    private void validateParentOfSourceReports(GraphContext context)
    {
        SourceReportService reportService = new SourceReportService(context);
        for (SourceReportModel sourceReportModel : reportService.findAll())
        {
            List<ReportModel> parents = sourceReportModel.getAllParentsInReversedOrder();
            Assert.assertTrue(parents.size() == 2);
            Assert.assertTrue(parents.get(0).getReportName().equals("Overview"));
            Assert.assertTrue(parents.get(0).getReportFilename().contains("JEE_Example_App__"));
        }
    }
}
