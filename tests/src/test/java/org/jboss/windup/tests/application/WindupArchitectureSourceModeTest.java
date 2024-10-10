package org.jboss.windup.tests.application;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
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
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.model.PropertiesModel;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateCompatibleFileReportRuleProvider;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;
import org.jboss.windup.rules.apps.javaee.rules.CreateEJBReportRuleProvider;
import org.jboss.windup.rules.apps.javaee.rules.CreateJPAReportRuleProvider;
import org.jboss.windup.rules.apps.javaee.rules.CreateSpringBeanReportRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.WebXmlService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.rules.apps.xml.service.XsltTransformationService;
import org.jboss.windup.tests.application.rules.TestServletAnnotationRuleProvider;
import org.jboss.windup.testutil.html.TestCompatibleReportUtil;
import org.jboss.windup.testutil.html.TestEJBReportUtil;
import org.jboss.windup.testutil.html.TestEJBReportUtil.EJBType;
import org.jboss.windup.testutil.html.TestJPAReportUtil;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
import org.jboss.windup.testutil.html.TestMigrationIssuesReportUtil;
import org.jboss.windup.testutil.html.TestSpringBeanReportUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@RunWith(Arquillian.class)
public class WindupArchitectureSourceModeTest extends WindupArchitectureTest {
    private static final String EXAMPLE_USERSCRIPT_INPUT = "/exampleuserscript.xml";
    private static final String EXAMPLE_USERSCRIPT_OUTPUT = "exampleuserscript_output.windup.xml";
    private static final String XSLT_OUTPUT_NAME = "exampleconversion_userdir.xslt";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
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
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addClass(TestServletAnnotationRuleProvider.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"))
                .addAsResource(new File("src/test/xml/XmlExample.windup.xml"))
                .addAsResource(new File("src/test/xml/exampleuserscript.xml"), EXAMPLE_USERSCRIPT_INPUT)
                .addAsResource(new File("src/test/xml/exampleconversion.xsl"));
    }

    @Test
    public void testRunWindupSourceMode() throws Exception {
        Path userPath = FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupuserscriptsdir_" + RandomStringUtils.randomAlphanumeric(6));
        try {
            Files.createDirectories(userPath);
            try (InputStream is = getClass().getResourceAsStream(EXAMPLE_USERSCRIPT_INPUT);
                 OutputStream os = new FileOutputStream(userPath.resolve(EXAMPLE_USERSCRIPT_OUTPUT).toFile())) {
                IOUtils.copy(is, os);
            }
            try (InputStream is = getClass().getResourceAsStream("/exampleconversion.xsl");
                 OutputStream os = new FileOutputStream(userPath.resolve(XSLT_OUTPUT_NAME).toFile())) {
                IOUtils.copy(is, os);
            }

            try (GraphContext context = createGraphContext()) {
                // The test-files folder in the project root dir.
                List<String> includeList = Collections.emptyList();
                List<String> excludeList = Collections.emptyList();
                super.runTest(context, true, "../test-files/src_example", Collections.singletonList(userPath.toFile()), true, includeList, excludeList);

                validateWebXmlReferences(context);
                validatePropertiesModels(context);
                validateReports(context);
                validateCompatibleReport(context);
                validateCsvReport(context.getGraphDirectory());
            }
        } finally {
            FileUtils.deleteQuietly(userPath.toFile());
        }
    }

    private void validateCsvReport(Path reportDirectory) throws Exception {
        Path csvPath = reportDirectory.resolve("src_example.csv");
        Map<String, Boolean> expectedRegexMatches = new HashMap<>();
        expectedRegexMatches.put("\"Rule Id\",\"Issue Category\".*", false);
        expectedRegexMatches.put("\"DiscoverWebXmlRuleProvider_1\",\"information\",\"Web XML\",\" Web Application Deployment Descriptors\",\"\",\"src_example\",\"web.xml\".*", false);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(csvPath.toFile()))) {
            String line = null;
            while ((line = fileReader.readLine()) != null) {
                final String lineFinal = line;
                expectedRegexMatches.keySet().forEach(key -> {
                    if (lineFinal.matches(key))
                        expectedRegexMatches.put(key, true);
                });
            }
        }

        expectedRegexMatches.entrySet().forEach(entry -> {
            if (!entry.getValue())
                Assert.fail("CSV Export lacked a line matching: " + entry.getKey());
        });
    }

    /**
     * Validate that a web.xml file was found, and that the metadata was extracted correctly
     */
    private void validateWebXmlReferences(GraphContext context) {
        WebXmlService webXmlService = new WebXmlService(context);
        Iterator<WebXmlModel> models = webXmlService.findAll().iterator();

        // There should be at least one file
        Assert.assertTrue(models.hasNext());
        WebXmlModel model = models.next();

        // and only one file
        Assert.assertFalse(models.hasNext());

        Assert.assertEquals("Sample Display Name", model.getDisplayName());

        int numberFound = 0;
        for (EnvironmentReferenceModel envRefModel : model.getEnvironmentReferences()) {
            Assert.assertEquals("jdbc/myJdbc", envRefModel.getName());
            Assert.assertEquals("javax.sql.DataSource", envRefModel.getReferenceType());
            numberFound++;
        }

        // there is only one env-ref
        Assert.assertEquals(1, numberFound);
    }

    /**
     * Validate that the expected Properties Models were found
     */
    private void validatePropertiesModels(GraphContext context) throws Exception {
        GraphService<PropertiesModel> service = new GraphService<>(context, PropertiesModel.class);

        int numberFound = 0;
        for (PropertiesModel model : service.findAll()) {
            numberFound++;

            Properties props = model.getProperties();
            Assert.assertEquals("value1", props.getProperty("example1"));
            Assert.assertEquals("anothervalue", props.getProperty("anotherproperty"));
            Assert.assertEquals("1234", props.getProperty("timetaken"));
        }

        Assert.assertEquals(1, numberFound);
    }

    private void validateSpringBeanReport(GraphContext context) {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                ReportModel.TEMPLATE_PATH,
                CreateSpringBeanReportRuleProvider.TEMPLATE_SPRING_REPORT);
        TestSpringBeanReportUtil util = new TestSpringBeanReportUtil();
        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util.checkSpringBeanInReport("mysamplebean", "org.example.MyExampleBean"));
    }

    private void validateEJBReport(GraphContext context) {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                ReportModel.TEMPLATE_PATH,
                CreateEJBReportRuleProvider.TEMPLATE_EJB_REPORT);
        TestEJBReportUtil util = new TestEJBReportUtil();
        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util.checkBeanInReport(EJBType.MDB, "MyNameForMessageDrivenBean",
                "org.windup.examples.ejb.messagedriven.MessageDrivenBean",
                "jms/MyQueue"));
        Assert.assertTrue(util.checkBeanInReport(EJBType.STATELESS, "MyNameForSimpleStatelessEJB", "",
                "org.windup.examples.ejb.simplestateless.SimpleStatelessEJB"));
        Assert.assertTrue(util.checkBeanInReport(EJBType.STATEFUL, "MyNameForSimpleStatefulEJB", "",
                "org.windup.examples.ejb.simplestateful.SimpleStatefulEJB"));
        Assert.assertTrue(util.checkBeanInReport(EJBType.STATELESS, "MyNameForSimpleStatelessJakartaEJB", "",
                "org.windup.examples.ejb.simplestateless.JakartaSimpleStatelessEJB"));
        Assert.assertTrue(util.checkBeanInReport(EJBType.STATEFUL, "MyNameForSimpleStatefulJakartaEJB", "",
                "org.windup.examples.ejb.simplestateful.JakartaSimpleStatefulEJB"));
    }

    private void validateCompatibleReport(GraphContext context) {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                ReportModel.TEMPLATE_PATH,
                CreateCompatibleFileReportRuleProvider.TEMPLATE_APPLICATION_REPORT);
        TestCompatibleReportUtil util = new TestCompatibleReportUtil();


        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util
                .checkFileInReport("src/main/resources/springexample/spring-sample-file.xml", ""));
        Assert.assertTrue(util
                .checkFileInReport(
                        "org/windup/examples/ejb/entitybean/SecondEntity.java", ""));
        Assert.assertTrue("An application has duplicate entries for a single file.", util.checkTableWithoutDuplicates());

    }

    private void validateJPAReport(GraphContext context) {
        TestJPAReportUtil util = new TestJPAReportUtil();

        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                ReportModel.TEMPLATE_PATH,
                CreateJPAReportRuleProvider.TEMPLATE_JPA_REPORT);
        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        util.loadPage(reportPath);

        Assert.assertTrue(util.checkEntityInReport("SimpleEntity", "org.windup.examples.ejb.entitybean.SimpleEntity",
                "SimpleEntityTable"));
        Assert.assertTrue(util.checkEntityInReport("SimpleEntityNoTableName",
                "org.windup.examples.ejb.entitybean.SimpleEntityNoTableName", "SimpleEntityNoTableName"));
    }

    private void validateMigrationIssuesReport(GraphContext context) {
        TestMigrationIssuesReportUtil util = new TestMigrationIssuesReportUtil();

        ApplicationReportService reportService = new ApplicationReportService(context);
        Iterable<ApplicationReportModel> reportModels = reportService.findAllByProperty(
                ReportModel.TEMPLATE_PATH,
                "/reports/templates/migration-issues.ftl");
        ApplicationReportModel reportModel = null;
        for (ApplicationReportModel possibleModel : reportModels) {
            if (possibleModel.getProjectModel() != null) {
                reportModel = possibleModel;
                break;
            }
        }

        if (reportModel == null)
            throw new RuntimeException("Failed to find migration issues report!");

        Path reportPath = new ReportService(context).getReportDirectory().resolve(reportModel.getReportFilename());
        util.loadPage(reportPath);

        Assert.assertTrue(util.checkIssue("Classification ActivationConfigProperty", 2, 8, "Unknown effort", 16));
        Assert.assertTrue(util.checkIssue("Title for Hint from XML", 1, 0, "Info", 0));
        Assert.assertTrue(util.checkIssue("Web Servlet", 1, 0, "Info", 0));

        Assert.assertTrue(util.checkIssue("In Summary", 2, 0, "Info", 0));
        Assert.assertFalse(util.checkIssue("Not In Summary", 2, 0, "Info", 0));
    }

    /**
     * Validate that the report pages were generated correctly
     */
    private void validateReports(GraphContext context) {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = getMainApplicationReport(context);
        Path appReportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());

        TestJavaApplicationOverviewUtil util = new TestJavaApplicationOverviewUtil();
        util.loadPage(appReportPath);
        util.checkFilePathAndTag("src_example", "src/main/resources/test.properties", "Properties");
        util.checkFilePathAndTag("src_example", "src/main/resources/WEB-INF/web.xml", "Web XML 3.0");
        util.checkFilePathAndTag("src_example", "src/main/resources/WEB-INF/web.xml", "TestTag2"); // WINDUP-679
        util.checkFilePathAndIssues("src_example", "org.windup.examples.servlet.SampleServlet",
                "References annotation 'javax.servlet.annotation.WebServlet'");
        util.checkFilePathAndIssues("src_example", "src/main/resources/WEB-INF/web.xml", "Container");
        util.checkFilePathAndIssues("src_example", "src/main/resources/WEB-INF/web.xml", "Title for Hint from XML");
        util.checkFilePathAndIssues("src_example", "src/main/resources/WEB-INF/web.xml", "title from user script");

        util.checkFilePathAndIssues("src_example", "org.windup.examples.servlet.SampleServlet",
                "javax.servlet.http.HttpServletRequest usage");

        XsltTransformationService xsltService = new XsltTransformationService(context);
        ProjectService projectService = new ProjectService(context);
        ProjectModel projectModel = projectService.create();
        projectModel.setName("src_example");
        XmlFileService xmlFileService = new XmlFileService(context);
        XmlFileModel xmlFileModel = xmlFileService.create();
        projectModel.addFileModel(xmlFileModel);
        Assert.assertTrue(Files.isRegularFile(xsltService.getTransformedXSLTPath(xmlFileModel).resolve(
                "web-xml-converted-example.xml")));
        Assert.assertTrue(Files.isRegularFile(xsltService.getTransformedXSLTPath(xmlFileModel).resolve(
                "web-xmluserscript-converted-example.xml")));

        validateSpringBeanReport(context);
        validateEJBReport(context);
        validateJPAReport(context);
        validateMigrationIssuesReport(context);
    }
}
