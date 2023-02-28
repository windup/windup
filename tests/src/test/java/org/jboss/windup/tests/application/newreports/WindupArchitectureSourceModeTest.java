package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.jboss.windup.reporting.data.dto.ApplicationCompatibleFilesDto;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.data.dto.ApplicationEJBsDto;
import org.jboss.windup.reporting.data.dto.ApplicationIssuesDto;
import org.jboss.windup.reporting.data.dto.ApplicationJPAsDto;
import org.jboss.windup.reporting.data.dto.ApplicationSpringBeansDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.rules.ApplicationCompatibleFilesRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationDetailsRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationEJBsRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationJPAsRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationSpringBeansRuleProvider;
import org.jboss.windup.reporting.data.rules.FilesRuleProvider;
import org.jboss.windup.reporting.data.rules.IssuesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.model.PropertiesModel;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;
import org.jboss.windup.rules.apps.javaee.service.WebXmlService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.rules.apps.xml.service.XsltTransformationService;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.jboss.windup.tests.application.rules.TestServletAnnotationRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

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
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
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
                 OutputStream os = new FileOutputStream(userPath.resolve(EXAMPLE_USERSCRIPT_OUTPUT).toFile())
            ) {
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
                super.runTest(context, false, "../test-files/src_example", Collections.singletonList(userPath.toFile()), true, includeList, excludeList);

                validateWebXmlReferences(context);
                validatePropertiesModels(context);
                validateApplicationDetails(context);
                validateCompatibleReport(context);
                validateCsvReport(context.getGraphDirectory());
            }
        } finally {
            FileUtils.deleteDirectory(userPath.toFile());
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
                    if (lineFinal.matches(key)) {
                        expectedRegexMatches.put(key, true);
                    }
                });
            }
        }

        expectedRegexMatches.entrySet().forEach(entry -> {
            if (!entry.getValue()) {
                Assert.fail("CSV Export lacked a line matching: " + entry.getKey());
            }
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

    private void validateSpringBeanReport(GraphContext context) throws IOException {
        File appSpringJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationSpringBeansRuleProvider.PATH + ".json")
                .toFile();

        ApplicationSpringBeansDto[] appSpringDtoList = new ObjectMapper().readValue(appSpringJson, ApplicationSpringBeansDto[].class);
        Assert.assertEquals(1, appSpringDtoList.length);

        Optional<ApplicationSpringBeansDto.SpringBeanDto> springBeanDto = appSpringDtoList[0].beans.stream()
                .filter(dto -> Objects.equals(dto.beanName, "mysamplebean") &&
                        Objects.equals(dto.className, "org.example.MyExampleBean") &&
                        dto.beanDescriptorFileId != null
                )
                .findFirst();
        Assert.assertTrue(springBeanDto.isPresent());
    }

    private void validateEJBReport(GraphContext context) throws IOException {
        File appEJBsJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationEJBsRuleProvider.PATH + ".json")
                .toFile();

        ApplicationEJBsDto[] appEJBDtoList = new ObjectMapper().readValue(appEJBsJson, ApplicationEJBsDto[].class);
        Assert.assertEquals(1, appEJBDtoList.length);

        // MDB
        Optional<ApplicationEJBsDto.MessageDrivenBeanDto> myNameForMessageDrivenBean = appEJBDtoList[0].messageDrivenBeans.stream()
                .filter(dto -> dto.beanName.equals("MyNameForMessageDrivenBean"))
                .findFirst();
        Assert.assertTrue(myNameForMessageDrivenBean.isPresent());
        Assert.assertEquals("org.windup.examples.ejb.messagedriven.MessageDrivenBean", myNameForMessageDrivenBean.get().className);
        Assert.assertEquals("jms/MyQueue", myNameForMessageDrivenBean.get().jmsDestination);

        // Stateless
        Optional<ApplicationEJBsDto.SessionBeanDto> myNameForSimpleStatelessEJB = appEJBDtoList[0].sessionBeans.stream()
                .filter(dto -> dto.beanName.equals("MyNameForSimpleStatelessEJB"))
                .findFirst();
        Optional<ApplicationEJBsDto.SessionBeanDto> myNameForSimpleStatelessJakartaEJB = appEJBDtoList[0].sessionBeans.stream()
                .filter(dto -> dto.beanName.equals("MyNameForSimpleStatelessJakartaEJB"))
                .findFirst();

        Assert.assertTrue(myNameForSimpleStatelessEJB.isPresent());
        Assert.assertEquals("org.windup.examples.ejb.simplestateless.SimpleStatelessEJB", myNameForSimpleStatelessEJB.get().className);

        Assert.assertTrue(myNameForSimpleStatelessJakartaEJB.isPresent());
        Assert.assertEquals("org.windup.examples.ejb.simplestateless.JakartaSimpleStatelessEJB", myNameForSimpleStatelessJakartaEJB.get().className);

        // Stateful
        Optional<ApplicationEJBsDto.SessionBeanDto> myNameForSimpleStatefulEJB = appEJBDtoList[0].sessionBeans.stream()
                .filter(dto -> dto.beanName.equals("MyNameForSimpleStatefulEJB"))
                .findFirst();
        Optional<ApplicationEJBsDto.SessionBeanDto> myNameForSimpleStatefulJakartaEJB = appEJBDtoList[0].sessionBeans.stream()
                .filter(dto -> dto.beanName.equals("MyNameForSimpleStatefulJakartaEJB"))
                .findFirst();

        Assert.assertTrue(myNameForSimpleStatefulEJB.isPresent());
        Assert.assertEquals("org.windup.examples.ejb.simplestateful.SimpleStatefulEJB", myNameForSimpleStatefulEJB.get().className);

        Assert.assertTrue(myNameForSimpleStatefulJakartaEJB.isPresent());
        Assert.assertEquals("org.windup.examples.ejb.simplestateful.JakartaSimpleStatefulEJB", myNameForSimpleStatefulJakartaEJB.get().className);
    }

    private void validateCompatibleReport(GraphContext context) throws IOException {
        File appCompatibleFilesJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationCompatibleFilesRuleProvider.PATH + ".json")
                .toFile();

        ApplicationCompatibleFilesDto[] appCompatibleFilesDtoList = new ObjectMapper().readValue(appCompatibleFilesJson, ApplicationCompatibleFilesDto[].class);
        Assert.assertEquals(1, appCompatibleFilesDtoList.length);

        //
        Optional<ApplicationCompatibleFilesDto.FileDto> fileDto1 = appCompatibleFilesDtoList[0].artifacts.stream()
                .flatMap(dto -> dto.files.stream())
                .filter(dto -> Objects.equals(dto.fileName, "src/main/resources/springexample/spring-sample-file.xml"))
                .findFirst();
        Optional<ApplicationCompatibleFilesDto.FileDto> fileDto2 = appCompatibleFilesDtoList[0].artifacts.stream()
                .flatMap(dto -> dto.files.stream())
                .filter(dto -> Objects.equals(dto.fileName, "org/windup/examples/ejb/entitybean/SecondEntity.java"))
                .findFirst();

        Assert.assertTrue(fileDto1.isPresent());
        Assert.assertTrue(fileDto2.isPresent());
    }

    private void validateJPAReport(GraphContext context) throws IOException {
        File appJPAJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationJPAsRuleProvider.PATH + ".json")
                .toFile();

        ApplicationJPAsDto[] appJPADtoList = new ObjectMapper().readValue(appJPAJson, ApplicationJPAsDto[].class);
        Assert.assertEquals(1, appJPADtoList.length);

        //
        Optional<ApplicationJPAsDto.JPAEntityDto> simpleEntity = appJPADtoList[0].entities.stream()
                .filter(dto -> Objects.equals(dto.entityName, "SimpleEntity"))
                .findFirst();
        Optional<ApplicationJPAsDto.JPAEntityDto> simpleEntityNoTableName = appJPADtoList[0].entities.stream()
                .filter(dto -> Objects.equals(dto.entityName, "SimpleEntityNoTableName"))
                .findFirst();

        Assert.assertTrue(simpleEntity.isPresent());
        Assert.assertEquals("org.windup.examples.ejb.entitybean.SimpleEntity", simpleEntity.get().className);
        Assert.assertEquals("SimpleEntityTable", simpleEntity.get().tableName);

        Assert.assertTrue(simpleEntityNoTableName.isPresent());
        Assert.assertEquals("org.windup.examples.ejb.entitybean.SimpleEntityNoTableName", simpleEntityNoTableName.get().className);
        Assert.assertEquals("SimpleEntityNoTableName", simpleEntityNoTableName.get().tableName);
    }

    private void validateMigrationIssuesReport(GraphContext context) throws IOException {
        File appIssuesJson = new ReportService(context).getApiDataDirectory()
                .resolve(IssuesRuleProvider.PATH + ".json")
                .toFile();

        ApplicationIssuesDto[] appIssuesDtoList = new ObjectMapper().readValue(appIssuesJson, ApplicationIssuesDto[].class);
        Assert.assertEquals(1, appIssuesDtoList.length);

        //
        List<ApplicationIssuesDto.IssueDto> allIssuesDto = appIssuesDtoList[0].issues.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertIssue(allIssuesDto, "Classification ActivationConfigProperty", 2, 16, "Unknown effort");
        assertIssue(allIssuesDto, "Title for Hint from XML", 1, 0, "Info");
        assertIssue(allIssuesDto, "Web Servlet", 1, 0, "Info");
        assertIssue(allIssuesDto, "In Summary", 2, 0, "Info");
    }

    public void assertIssue(List<ApplicationIssuesDto.IssueDto> issueDtoList, String issueName, int totalIncidents, int totalStoryPoints, String effortDescription) {
        Optional<ApplicationIssuesDto.IssueDto> issueDto = issueDtoList.stream()
                .filter(dto -> dto.name.equals(issueName))
                .findFirst();
        Assert.assertTrue(issueDto.isPresent());

        Assert.assertEquals(totalIncidents, issueDto.get().totalIncidents);
        Assert.assertEquals(totalStoryPoints, issueDto.get().totalStoryPoints);
        Assert.assertEquals(effortDescription, issueDto.get().effort.description);
    }

    public void validateApplicationDetails(GraphContext context) throws IOException {
        File filesJson = new ReportService(context).getApiDataDirectory()
                .resolve(FilesRuleProvider.PATH + ".json")
                .toFile();
        File appDetailsJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationDetailsRuleProvider.PATH + ".json")
                .toFile();

        ApplicationDetailsDto[] appDetailsDtoList = new ObjectMapper().readValue(appDetailsJson, ApplicationDetailsDto[].class);
        Assert.assertEquals(1, appDetailsDtoList.length);

        FileDto[] fileDtoList = new ObjectMapper().readValue(filesJson, FileDto[].class);
        Assert.assertNotNull(fileDtoList);

        // Verify appFile
        Optional<ApplicationDetailsDto.ApplicationFileDto> childGroupFile = appDetailsDtoList[0].applicationFiles.stream()
                .filter(applicationFileDto -> applicationFileDto.fileName.equals("src_example"))
                .findFirst();
        Assert.assertTrue(childGroupFile.isPresent());

        // Map appFile children files
        List<FileDto> childrenFilesDtoList = childGroupFile.get().childrenFileIds.stream()
                .map(s -> Arrays.stream(fileDtoList)
                        .filter(fileDto -> Objects.equals(fileDto.id, s))
                        .findFirst()
                        .orElse(null)
                )
                .collect(Collectors.toList());

        validateChildFile(childrenFilesDtoList, "src/main/resources/test.properties", "Properties", null, null);
        validateChildFile(childrenFilesDtoList, "src/main/resources/WEB-INF/web.xml", "Web XML", "TestTag2", null);
        validateChildFile(childrenFilesDtoList, "org.windup.examples.servlet.SampleServlet", null, null, "References annotation 'javax.servlet.annotation.WebServlet'");
        validateChildFile(childrenFilesDtoList, "src/main/resources/WEB-INF/web.xml", null, null, "Container");
        validateChildFile(childrenFilesDtoList, "src/main/resources/WEB-INF/web.xml", null, null, "Title for Hint from XML");
        validateChildFile(childrenFilesDtoList, "src/main/resources/WEB-INF/web.xml", null, null, "title from user script");

        validateChildFile(childrenFilesDtoList, "org.windup.examples.servlet.SampleServlet", null, null, "javax.servlet.http.HttpServletRequest usage");

        //
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

        //
        validateSpringBeanReport(context);
        validateEJBReport(context);
        validateJPAReport(context);
        validateMigrationIssuesReport(context);
    }

    public void validateChildFile(List<FileDto> childrenFilesDtoList, String filename, String expectedTag, String expectedClasificationHint, String expectedHint) {
        Optional<FileDto> fileDto = childrenFilesDtoList.stream()
                .filter(Objects::nonNull)
                .filter(dto -> dto.prettyFileName.equals(filename))
                .findFirst();
        Assert.assertTrue(fileDto.isPresent());

        if (expectedTag != null) {
            Assert.assertTrue(fileDto.get().tags.stream()
                    .map(t -> t.name)
                    .collect(Collectors.toList())
                    .contains(expectedTag)
            );
        }

        if (expectedClasificationHint != null) {
            Assert.assertTrue(fileDto.get().classificationsAndHintsTags.contains(expectedClasificationHint));
        }

        if (expectedHint != null) {
            Assert.assertTrue(fileDto.get().hints.stream()
                    .map(t -> t.title)
                    .collect(Collectors.toList())
                    .contains(expectedHint)
            );
        }
    }
}
