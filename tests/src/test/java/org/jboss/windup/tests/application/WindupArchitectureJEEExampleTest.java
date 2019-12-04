package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.jboss.windup.reporting.rules.CreateApplicationListReportRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.rules.CreateEJBReportRuleProvider;
import org.jboss.windup.testutil.html.TestApplicationListUtil;
import org.jboss.windup.testutil.html.TestEJBReportUtil;
import org.jboss.windup.testutil.html.TestEJBReportUtil.EJBType;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
import org.jboss.windup.testutil.html.TestTechReportUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@RunWith(Arquillian.class)
public class WindupArchitectureJEEExampleTest extends WindupArchitectureTest
{
    static final String LABEL_SUCCESS = "label label-success";
    static final String LABEL_DANGER = "label label-danger";
    static final String LABEL_WARNING = "label label-warning";
    static final String LABEL_INFO = "label label-info";
    static final String LABEL_DEFAULT = "label label-default";

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
            validateLabels(context);
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

    private void validateLabels(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateApplicationListReportRuleProvider.TEMPLATE_PATH);
        Assert.assertNotNull(reportModel);

        Path appReportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        Assert.assertNotNull(appReportPath);

        TestApplicationListUtil util = new TestApplicationListUtil();
        util.loadPage(appReportPath);

        validateRuntimeLabelsLegendHeader(util);
        validateRuntimeLabelsLegendContent(util);
        validateApplicationTargetRuntimeLabels(util);
        validateApplicationTargetRuntimeLabelsClickable(util);
    }

    private void validateRuntimeLabelsLegendHeader(TestApplicationListUtil util)
    {
        WebElement runtimeLegendHeader = util.getApplicationTargetRuntimeLegendHeader();
        WebElement legendHeaderText = runtimeLegendHeader.findElement(By.tagName("a"));
        Assert.assertEquals("Runtime labels legend", legendHeaderText.getText().trim());

        List<WebElement> legendHeaderLabels = runtimeLegendHeader.findElements(By.tagName("span"));

        Assert.assertEquals("Supported", legendHeaderLabels.get(0).getText().trim());
        Assert.assertEquals(LABEL_SUCCESS, legendHeaderLabels.get(0).getAttribute("class"));

        Assert.assertEquals("Partially supported", legendHeaderLabels.get(1).getText().trim());
        Assert.assertEquals(LABEL_WARNING, legendHeaderLabels.get(1).getAttribute("class"));

        Assert.assertEquals("Unsuitable", legendHeaderLabels.get(2).getText().trim());
        Assert.assertEquals(LABEL_DANGER, legendHeaderLabels.get(2).getAttribute("class"));

        Assert.assertEquals("Neutral", legendHeaderLabels.get(3).getText().trim());
        Assert.assertEquals(LABEL_DEFAULT, legendHeaderLabels.get(3).getAttribute("class"));
    }

    private void validateRuntimeLabelsLegendContent(TestApplicationListUtil util)
    {
        WebElement runtimeLegendContent = util.getApplicationTargetRuntimeLegendContent();
        WebElement legendsDL = runtimeLegendContent.findElement(By.tagName("dl"));

        // validate tech sort
        List<WebElement> dd = legendsDL.findElements(By.tagName("dd"));
        for (WebElement webElement : dd)
        {
            List<WebElement> supportedTechLabels = webElement.findElements(By.className("label-success"));
            List<WebElement> unsuitableTechLabels = webElement.findElements(By.className("label-danger"));
            List<WebElement> neutralTechLabels = webElement.findElements(By.className("label-info"));

            Assert.assertTrue((verifyOrderedAlphabetically(supportedTechLabels)));
            Assert.assertTrue((verifyOrderedAlphabetically(unsuitableTechLabels)));
            Assert.assertTrue((verifyOrderedAlphabetically(neutralTechLabels)));
        }
    }

    private boolean verifyOrderedAlphabetically(List<WebElement> webElements)
    {
        boolean ordered = true;
        for (int i = 1; i < webElements.size(); i++)
        {
            WebElement currentWebElement = webElements.get(i);
            WebElement previousWebElement = webElements.get(i - 1);
            if (currentWebElement.getText().trim().compareTo(previousWebElement.getText().trim()) < 0)
            {
                ordered = false;
                break;
            }
        }
        return ordered;
    }

    private void validateApplicationTargetRuntimeLabels(TestApplicationListUtil util)
    {
        final String appName = "jee-example-app-1.0.0.ear";

        List<WebElement> targetRuntimes = util.getApplicationTargetRuntimeLabels(appName);
        Assert.assertEquals(3, targetRuntimes.size());

        WebElement spanTarget1 = targetRuntimes.get(0).findElement(By.tagName("span"));
        WebElement spanTarget2 = targetRuntimes.get(1).findElement(By.tagName("span"));
        WebElement spanTarget3 = targetRuntimes.get(2).findElement(By.tagName("span"));

        Assert.assertEquals("Target1", spanTarget1.getText().trim());
        Assert.assertEquals(LABEL_DANGER, spanTarget1.getAttribute("class"));

        Assert.assertEquals("Target2", spanTarget2.getText().trim());
        Assert.assertEquals(LABEL_WARNING, spanTarget2.getAttribute("class"));

        Assert.assertEquals("Target3", spanTarget3.getText().trim());
        Assert.assertEquals(LABEL_SUCCESS, spanTarget3.getAttribute("class"));
    }

    private void validateApplicationTargetRuntimeLabelsClickable(TestApplicationListUtil util)
    {
        final String appName = "jee-example-app-1.0.0.ear";

        List<WebElement> targetRuntimes = util.getApplicationTargetRuntimeLabels(appName);
        Assert.assertEquals(3, targetRuntimes.size());

        WebElement spanTarget1 = targetRuntimes.get(0).findElement(By.tagName("span"));
        WebElement spanTarget2 = targetRuntimes.get(1).findElement(By.tagName("span"));
        WebElement spanTarget3 = targetRuntimes.get(2).findElement(By.tagName("span"));

        // Verify initial state of tech labels
        List<WebElement> applicationTechLabels = util.getApplicationTechLabels(appName);
        Assert.assertFalse(applicationTechLabels.isEmpty());
        verifyTechLabelsInitialState(applicationTechLabels);

        // The supported, unsuitable, and neutral values are taken from tests/src/test/xml/rules/test.windup.label.xml
        spanTarget1.click();
        applicationTechLabels = util.getApplicationTechLabels(appName);
        Assert.assertFalse(applicationTechLabels.isEmpty());
        List<String> supported = Collections.singletonList("EJB");
        List<String> unsuitable = Collections.singletonList("WebLogic");
        List<String> neutral = Arrays.asList("Maven XML", "Web XML");
        verifyTechLabelsClicked(applicationTechLabels, supported, unsuitable, neutral);

        spanTarget2.click();
        applicationTechLabels = util.getApplicationTechLabels(appName);
        Assert.assertFalse(applicationTechLabels.isEmpty());
        supported = Collections.singletonList("WebLogic");
        unsuitable = Collections.singletonList("JPA");
        neutral = Arrays.asList("Maven XML", "Web XML");
        verifyTechLabelsClicked(applicationTechLabels, supported, unsuitable, neutral);

        spanTarget3.click();
        applicationTechLabels = util.getApplicationTechLabels(appName);
        Assert.assertFalse(applicationTechLabels.isEmpty());
        supported = Arrays.asList("WebLogic", "EJB");
        unsuitable = Collections.singletonList("JPA");
        neutral = Arrays.asList("Maven XML", "Web XML", "Manifest", "Properties");
        verifyTechLabelsClicked(applicationTechLabels, supported, unsuitable, neutral);

        // Should unselect all labels
        spanTarget3.click();
        applicationTechLabels = util.getApplicationTechLabels(appName);
        Assert.assertFalse(applicationTechLabels.isEmpty());
        verifyTechLabelsInitialState(applicationTechLabels);
    }

    private void verifyTechLabelsInitialState(List<WebElement> applicationTechLabels)
    {
        for (WebElement webElement : applicationTechLabels)
        {
            String techLabelClass = webElement.getAttribute("class");

            // Assert using contains instead of equals since application_list.ftl contains
            // an additional class 'label-important' in the labels. E.g. <span class="label label-info label-important" title="${tag.level}">
            Assert.assertTrue(techLabelClass.contains(LABEL_INFO));
        }
    }

    private void verifyTechLabelsClicked(
                List<WebElement> applicationTechLabels,
                List<String> supported, List<String> unsuitable, List<String> neutral)
    {
        for (WebElement webElement : applicationTechLabels)
        {
            String techLabel = webElement.getText().trim();
            String techLabelClass = webElement.getAttribute("class");

            if (supported.stream().anyMatch(techLabel::startsWith))
            {
                Assert.assertEquals(LABEL_SUCCESS, techLabelClass);
            }
            else if (unsuitable.stream().anyMatch(techLabel::startsWith))
            {
                Assert.assertEquals(LABEL_DANGER, techLabelClass);
            }
            else if (neutral.stream().anyMatch(techLabel::startsWith))
            {
                Assert.assertEquals(LABEL_DEFAULT, techLabelClass);
            }
            else
            {
                Assert.assertEquals(LABEL_WARNING, techLabelClass);
            }
        }
    }
}
