package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.model.TechReportModel;
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
import org.jboss.windup.testutil.html.TestTechReportUtil;
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
            super.runTest(context, "../test-files/jee-example-app-1.0.0.ear", "src/test/xml/rules", false);

            validateEjbXmlReferences(context);
            validateReports(context);
            validateParentOfSourceReports(context);
        }
    }

    @Test
    public void testTechReportFrameworksWar() throws Exception
    {
        try (GraphContext context = super.createGraphContext())
        {
            // The test-files folder in the project root dir.
            super.runTest(context, "../test-files/techReport/frameworks.war", "src/test/xml/rules", false);
            validateTechReportFrameworksWar(context);
            validateTechReportPointsCount(context);
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
                Assert.assertEquals("com.acme.anvil.service.ItemLookupHome", sessionBean.getEjbHome().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookup", sessionBean.getEjbRemote().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupLocalHome", sessionBean.getEjbLocalHome().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupLocal", sessionBean.getEjbLocal().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupBean", sessionBean.getEjbClass().getQualifiedName());
                Assert.assertEquals("Stateless", sessionBean.getSessionType());
                Assert.assertEquals("Container", sessionBean.getTransactionType());
            }
            else if (sessionBean.getBeanName().equals("ProductCatalogBean"))
            {
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogHome", sessionBean.getEjbHome().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalog", sessionBean.getEjbRemote().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogLocalHome", sessionBean.getEjbLocalHome().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogLocal", sessionBean.getEjbLocal().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogBean", sessionBean.getEjbClass().getQualifiedName());
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
        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util.checkBeanInReport(EJBType.MDB, "LogEventSubscriber", "com.acme.anvil.service.jms.LogEventSubscriber"));
        Assert.assertTrue(util.checkBeanInReport(EJBType.STATELESS, "ItemLookupBean", "HomeLocalRemote", "com.acme.anvil.service.ItemLookupBean"));
        Assert.assertTrue(util.checkBeanInReport(EJBType.STATELESS, "ProductCatalogBean", "HomeLocalRemote", "com.acme.anvil.service.ProductCatalogBean"));
    }

    private void validateReports(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = getMainApplicationReport(context);
        Path appReportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());

        TestJavaApplicationOverviewUtil util = new TestJavaApplicationOverviewUtil();
        util.loadPage(appReportPath);
        util.checkFilePathAndTag("jee-example-app-1.0.0.ear/jee-example-services.jar", "META-INF/ejb-jar.xml",
                    "EJB XML 2.1");

        validateEJBBeanReport(context);

        validateTechReportJEEExample(context);
    }

    private void validateTechReportPointsCount(GraphContext graphContext)
    {
        Iterable<TechReportModel> techReportsIt = graphContext.findAll(TechReportModel.class);
        ReportService reportService = new ReportService(graphContext);

        // Check the reports
        for (TechReportModel techReportModel : techReportsIt)
        {
            final Path path = reportService.getReportDirectory().resolve(techReportModel.getReportFilename());

            if (techReportModel.getProjectModel() == null)
            {
                TestTechReportUtil techReportUtil = new TestTechReportUtil();
                techReportUtil.loadPage(path);
                String appName = "frameworks.war";
                techReportUtil.checkPoints(appName, TestTechReportUtil.PointsType.MANDATORY, 6);
                techReportUtil.checkPoints(appName, TestTechReportUtil.PointsType.CLOUD_MANDATORY, 15);
                techReportUtil.checkPoints(appName, TestTechReportUtil.PointsType.POTENTIAL, 0);

            }
        }
    }

    private void validateTechReport(GraphContext graphContext, List<TestTechReportUtil.BubbleInfo> bubblesExpected, List<TestTechReportUtil.BoxInfo> boxesExpected)
    {

        // 2 reports - a global one and the app one.
        Iterable<TechReportModel> techReportsIt = graphContext.findAll(TechReportModel.class);
        List<TechReportModel> techReports = new ArrayList<>();
        techReportsIt.forEach(techReports::add);
        Assert.assertEquals(2, techReports.size());


        // There should be one box report for each app.

        Map<Long, TechReportModel> idToReport = new HashMap<>();
        techReportsIt.forEach(techReportModel -> {
            Long id = null;
            if (techReportModel.getProjectModel() != null) {
                id = (Long) techReportModel.getProjectModel().getElement().id();
            }
            final TechReportModel previous = idToReport.put(id, techReportModel);
            if (previous != null)
                Assert.fail("Duplicate report for project with vertex ID #" + id + " (if null -> the global one)");
        });

        Assert.assertNotNull(idToReport.get(null));
        for (ProjectModel app : new ProjectService(graphContext).getRootProjectModels())
        {
            final TechReportModel techReport = idToReport.get(app.getElement().id());
            Assert.assertNotNull(techReport);
        }

        ReportService reportService = new ReportService(graphContext);

        // Check the reports
        for (TechReportModel techReportModel : techReportsIt)
        {
            final Path path = reportService.getReportDirectory().resolve(techReportModel.getReportFilename());

            if (techReportModel.getProjectModel() == null)
            {
                // Global bubbles report
                new TestTechReportUtil().checkTechGlobalReport(path, bubblesExpected);
            }
            else {
                // Per-app box report
                new TestTechReportUtil().checkTechBoxReport(path, boxesExpected);
            }
        }

    }

    private void validateParentOfSourceReports(GraphContext context)
    {
        SourceReportService reportService = new SourceReportService(context);
        for (SourceReportModel sourceReportModel : reportService.findAll())
        {
            List<ReportModel> parents = sourceReportModel.getAllParentsInReversedOrder();
            Assert.assertTrue(parents.size() == 2);
            Assert.assertTrue(parents.get(0).getReportName().equals("Dashboard"));
            Assert.assertTrue(parents.get(0).getReportFilename().contains("report_index"));
        }
    }

    private void validateTechReportJEEExample(GraphContext context)
    {
        List<TestTechReportUtil.BubbleInfo> bubblesExpected = new ArrayList<>();
        final String appName = "jee-example-app-1.0.0.ear";
        bubblesExpected.add(new TestTechReportUtil.BubbleInfo(appName, "Web", 2, 3));
        bubblesExpected.add(new TestTechReportUtil.BubbleInfo(appName, "EJB", 2, 3));
        bubblesExpected.add(new TestTechReportUtil.BubbleInfo(appName, "Transactions", 3, 5));
        bubblesExpected.add(new TestTechReportUtil.BubbleInfo(appName, "Rich", 0, 0));
        bubblesExpected.add(new TestTechReportUtil.BubbleInfo(appName, "Test", 0, 0));
        bubblesExpected.add(new TestTechReportUtil.BubbleInfo(appName, "Logging", 0, 0));
        bubblesExpected.add(new TestTechReportUtil.BubbleInfo(appName, "Processing", 0, 0));
        bubblesExpected.add(new TestTechReportUtil.BubbleInfo(appName, "Inversion of Control", 0, 0));

        List<TestTechReportUtil.BoxInfo> boxesExpected = new ArrayList<>();
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Java_EE", "View", "Web", "Web XML File", 1, 999));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Java_EE", "Sustain", "Transactions", "JTA", 3, 999));
        // There should be no box in sector Store.
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Java_EE", "Store", null, null, 0, 0));
        // There should be no box in row Embedded.
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", null, null, null, 0, 0));

        validateTechReport(context, bubblesExpected, boxesExpected);
    }

    private void validateTechReportFrameworksWar(GraphContext context)
    {
        List<TestTechReportUtil.BubbleInfo> bubblesExpected = new ArrayList<>();
        final String appName = "frameworks.war";
        //bubblesExpected.add(new TestTechReportUtil.BubbleInfo(appName, "Web", 2, 3));

        List<TestTechReportUtil.BoxInfo> boxesExpected = new ArrayList<>();
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Java_EE", "View", "Web", "Web XML File", 1, 999));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "View", "Markup", "HTML", 4, 4));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Connect", "WebService", "CXF", 1, 1));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Connect", "WebService", "XFire", 1, 1));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Connect", "WebService", "Axis2", 1, 1));
        //boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Connect", "WebService", "Axis", 2, 2));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Store", "Object Mapping", "Hibernate OGM", 1, 1));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Store", "Object Mapping", "Hibernate", 2, 2));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Store", "Object Mapping", "EclipseLink", 1, 1));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Execute", "Rules & Processes", "Drools", 1, 1));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Execute", "Rules & Processes", "JBPM", 1, 1));
        boxesExpected.add(new TestTechReportUtil.BoxInfo("Embedded", "Execute", "Rules & Processes", "iLog", 1, 1));

        validateTechReport(context, bubblesExpected, boxesExpected);
    }
}
