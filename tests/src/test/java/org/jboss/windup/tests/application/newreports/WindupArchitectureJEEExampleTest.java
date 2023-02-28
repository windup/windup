package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.data.dto.ApplicationDto;
import org.jboss.windup.reporting.data.dto.ApplicationEJBsDto;
import org.jboss.windup.reporting.data.dto.ApplicationTechnologiesDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.dto.LabelDto;
import org.jboss.windup.reporting.data.rules.ApplicationDetailsRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationEJBsRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationsRuleProvider;
import org.jboss.windup.reporting.data.rules.FilesRuleProvider;
import org.jboss.windup.reporting.data.rules.LabelsRuleProvider;
import org.jboss.windup.reporting.data.rules.TechnologiesRuleProvider;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.rules.CreateApplicationListReportRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.jboss.windup.testutil.html.TestApplicationListUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@RunWith(Arquillian.class)
public class WindupArchitectureJEEExampleTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
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
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Test
    public void testRunWindupJEEExampleMode() throws Exception {
        try (GraphContext context = super.createGraphContext()) {
            // The test-files folder in the project root dir.
            super.runTest(context, false, "../test-files/jee-example-app-1.0.0.ear", "src/test/xml/rules", false);

            validateEjbXmlReferences(context);
            validateReports(context);
            validateLabels(context);
        }
    }

    @Test
    public void testTechReportFrameworksWar() throws Exception {
        try (GraphContext context = super.createGraphContext()) {
            // The test-files folder in the project root dir.
            super.runTest(context, false, "../test-files/techReport/frameworks.war", "src/test/xml/rules", false);
            validateTechReportFrameworksWar(context);
        }
    }

    /**
     * Validate that a ejb-jar.xml file was found, and that the metadata was extracted correctly
     */
    private void validateEjbXmlReferences(GraphContext context) {
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
        for (EjbSessionBeanModel sessionBean : model.getEjbSessionBeans()) {
            if (sessionBean.getBeanName().equals("ItemLookupBean")) {
                Assert.assertEquals("com.acme.anvil.service.ItemLookupHome", sessionBean.getEjbHome().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookup", sessionBean.getEjbRemote().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupLocalHome", sessionBean.getEjbLocalHome().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupLocal", sessionBean.getEjbLocal().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupBean", sessionBean.getEjbClass().getQualifiedName());
                Assert.assertEquals("Stateless", sessionBean.getSessionType());
                Assert.assertEquals("Container", sessionBean.getTransactionType());
            } else if (sessionBean.getBeanName().equals("ProductCatalogBean")) {
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogHome", sessionBean.getEjbHome().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalog", sessionBean.getEjbRemote().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogLocalHome", sessionBean.getEjbLocalHome().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogLocal", sessionBean.getEjbLocal().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogBean", sessionBean.getEjbClass().getQualifiedName());
                Assert.assertEquals("Stateless", sessionBean.getSessionType());
                Assert.assertEquals("Container", sessionBean.getTransactionType());
            } else {
                Assert.fail("Unrecognized session bean found: " + sessionBean.getBeanName());
            }
            sessionBeansFound++;
        }
        Assert.assertEquals(2, sessionBeansFound);

        int messageDrivenFound = 0;
        for (EjbMessageDrivenModel messageDriven : model.getMessageDriven()) {
            Assert.assertEquals("LogEventSubscriber", messageDriven.getBeanName());
            Assert.assertEquals("com.acme.anvil.service.jms.LogEventSubscriber", messageDriven.getEjbClass()
                    .getQualifiedName());
            Assert.assertEquals("Container", messageDriven.getTransactionType());
            messageDrivenFound++;
        }
        Assert.assertEquals(1, messageDrivenFound);
    }

    private void validateReports(GraphContext context) throws IOException {
        File files = new ReportService(context).getApiDataDirectory()
                .resolve(FilesRuleProvider.PATH + ".json")
                .toFile();
        File appDetailsFile = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationDetailsRuleProvider.PATH + ".json")
                .toFile();

        // Load files
        FileDto[] filesDtoList = new ObjectMapper().readValue(files, FileDto[].class);
        Assert.assertTrue(filesDtoList.length > 0);

        // Load app details
        ApplicationDetailsDto[] appDetailsDtoList = new ObjectMapper().readValue(appDetailsFile, ApplicationDetailsDto[].class);
        Assert.assertEquals(1, appDetailsDtoList.length);

        // Verify app details
        Optional<ApplicationDetailsDto.ApplicationFileDto> jeeExample = appDetailsDtoList[0].applicationFiles.stream()
                .filter(dto -> dto.rootPath.equals("jee-example-app-1.0.0.ear/jee-example-services.jar"))
                .findFirst();
        Assert.assertTrue(jeeExample.isPresent());

        Optional<FileDto> metaInfEjbJarXml = Stream.of(filesDtoList).filter(dto -> dto.prettyFileName.equals("META-INF/ejb-jar.xml"))
                .findFirst();
        Assert.assertTrue(metaInfEjbJarXml.isPresent());

        // Verify tags
        boolean tagMatches = metaInfEjbJarXml.get().tags.stream()
                .anyMatch(tagDto -> tagDto.name.equals("EJB XML") &&
                        tagDto.version.equals("2.1")
                );
        Assert.assertTrue(tagMatches);

        // Verify other data
        validateEJBBeanReport(context);
        validateTechReportJEEExample(context);
    }

    private void validateEJBBeanReport(GraphContext context) throws IOException {
        File ejbFile = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationEJBsRuleProvider.PATH + ".json")
                .toFile();

        ApplicationEJBsDto[] dtoList = new ObjectMapper().readValue(ejbFile, ApplicationEJBsDto[].class);
        Assert.assertEquals(1, dtoList.length);

        Optional<ApplicationEJBsDto.MessageDrivenBeanDto> logEventSubscriber = dtoList[0].messageDrivenBeans.stream()
                .filter(dto -> dto.beanName.equals("LogEventSubscriber"))
                .findFirst();
        Assert.assertTrue(logEventSubscriber.isPresent());
        Assert.assertEquals("LogEventSubscriber", logEventSubscriber.get().beanName);
        Assert.assertEquals("com.acme.anvil.service.jms.LogEventSubscriber", logEventSubscriber.get().className);

        Optional<ApplicationEJBsDto.SessionBeanDto> itemLookupBean = dtoList[0].sessionBeans.stream()
                .filter(dto -> dto.beanName.equals("ItemLookupBean"))
                .findFirst();
        Assert.assertTrue(itemLookupBean.isPresent());
        Assert.assertEquals(ApplicationEJBsDto.SessionBeanType.STATELESS, itemLookupBean.get().type);
        Assert.assertEquals("ItemLookupBean", itemLookupBean.get().beanName);
        Assert.assertEquals("com.acme.anvil.service.ItemLookupBean", itemLookupBean.get().className);
        Assert.assertNotNull(itemLookupBean.get().homeEJBFileId);
        Assert.assertNotNull(itemLookupBean.get().localEJBFileId);
        Assert.assertNotNull(itemLookupBean.get().remoteEJBFileId);

        Optional<ApplicationEJBsDto.SessionBeanDto> productCatalogBean = dtoList[0].sessionBeans.stream()
                .filter(dto -> dto.beanName.equals("ProductCatalogBean"))
                .findFirst();
        Assert.assertTrue(productCatalogBean.isPresent());
        Assert.assertEquals(ApplicationEJBsDto.SessionBeanType.STATELESS, productCatalogBean.get().type);
        Assert.assertEquals("ProductCatalogBean", productCatalogBean.get().beanName);
        Assert.assertEquals("com.acme.anvil.service.ProductCatalogBean", productCatalogBean.get().className);
        Assert.assertNotNull(productCatalogBean.get().homeEJBFileId);
        Assert.assertNotNull(productCatalogBean.get().localEJBFileId);
        Assert.assertNotNull(productCatalogBean.get().remoteEJBFileId);
    }

    private void validateTechReportJEEExample(GraphContext context) throws IOException {
        File technologiesFile = new ReportService(context).getApiDataDirectory()
                .resolve(TechnologiesRuleProvider.PATH + ".json")
                .toFile();

        ApplicationTechnologiesDto[] appTechnologiesDtoList = new ObjectMapper().readValue(technologiesFile, ApplicationTechnologiesDto[].class);
        Assert.assertEquals(1, appTechnologiesDtoList.length);

        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("View").get("Web").size());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("View").get("Web").get("Web XML File").intValue());

        Assert.assertEquals(2, appTechnologiesDtoList[0].technologyGroups.get("Connect").get("EJB").size());
        Assert.assertEquals(4, appTechnologiesDtoList[0].technologyGroups.get("Connect").get("EJB").get("Stateless (SLSB)").intValue());
        Assert.assertEquals(2, appTechnologiesDtoList[0].technologyGroups.get("Connect").get("EJB").get("Message (MDB)").intValue());

        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Sustain").get("Transactions").size());
        Assert.assertEquals(3, appTechnologiesDtoList[0].technologyGroups.get("Sustain").get("Transactions").get("JTA").intValue());

        Assert.assertEquals(0, appTechnologiesDtoList[0].technologyGroups.get("View").get("Rich").size());
        Assert.assertEquals(0, appTechnologiesDtoList[0].technologyGroups.get("Sustain").get("Test").size());
        Assert.assertEquals(0, appTechnologiesDtoList[0].technologyGroups.get("Sustain").get("Logging").size());
        Assert.assertEquals(0, appTechnologiesDtoList[0].technologyGroups.get("Execute").get("Processing").size());
        Assert.assertEquals(0, appTechnologiesDtoList[0].technologyGroups.get("Execute").get("Inversion of Control").size());

        Assert.assertTrue(appTechnologiesDtoList[0].technologyGroups.get("Store").values().stream().allMatch(Map::isEmpty));
    }

    private void validateLabels(GraphContext context) throws IOException {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                ReportModel.TEMPLATE_PATH,
                CreateApplicationListReportRuleProvider.TEMPLATE_PATH);
        Assert.assertNotNull(reportModel);

        Path appReportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        Assert.assertNotNull(appReportPath);

        TestApplicationListUtil util = new TestApplicationListUtil();
        util.loadPage(appReportPath);

        validateApplicationTargetRuntimeLabels(context);
        validateApplicationTargetLabels(context);
    }

    private void validateApplicationTargetRuntimeLabels(GraphContext context) throws IOException {
        File labelsFile = new ReportService(context).getApiDataDirectory()
                .resolve(LabelsRuleProvider.PATH + ".json")
                .toFile();

        LabelDto[] labelsDtoList = new ObjectMapper().readValue(labelsFile, LabelDto[].class);
        Assert.assertEquals(3, labelsDtoList.length);

        boolean targetsExists = Stream.of(labelsDtoList)
                .allMatch(labelDto -> labelDto.id.equals("target1") ||
                        labelDto.id.equals("target2") ||
                        labelDto.id.equals("target3")
                );
        Assert.assertTrue(targetsExists);
    }

    private void validateApplicationTargetLabels(GraphContext context) throws IOException {
        File applicationsFile = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationsRuleProvider.PATH + ".json")
                .toFile();

        ApplicationDto[] applicationDtoList = new ObjectMapper().readValue(applicationsFile, ApplicationDto[].class);
        Assert.assertEquals(1, applicationDtoList.length);

        boolean appContainsTags = applicationDtoList[0].tags.containsAll(Arrays.asList(
                "Web XML 2.4",
                "WebLogic EJB XML",
                "WebLogic Web XML",
                "EJB XML 2.1",
                "Manifest",
                "Maven XML",
                "Properties"
        ));
        Assert.assertTrue(appContainsTags);
    }


    private void validateTechReportFrameworksWar(GraphContext context) throws IOException {
        File technologiesFile = new ReportService(context).getApiDataDirectory()
                .resolve(TechnologiesRuleProvider.PATH + ".json")
                .toFile();

        ApplicationTechnologiesDto[] appTechnologiesDtoList = new ObjectMapper().readValue(technologiesFile, ApplicationTechnologiesDto[].class);
        Assert.assertEquals(1, appTechnologiesDtoList.length);

        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("View").get("Web").size());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("View").get("Web").get("Web XML File").intValue());

        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("View").get("Markup").size());
        Assert.assertEquals(4, appTechnologiesDtoList[0].technologyGroups.get("View").get("Markup").get("HTML").intValue());

        Assert.assertEquals(5, appTechnologiesDtoList[0].technologyGroups.get("Connect").get("WebService").size());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Connect").get("WebService").get("CXF").intValue());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Connect").get("WebService").get("XFire").intValue());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Connect").get("WebService").get("Axis2").intValue());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Connect").get("WebService").get("Axis2-technology-tag").intValue());
        Assert.assertEquals(2, appTechnologiesDtoList[0].technologyGroups.get("Connect").get("WebService").get("Axis").intValue());

        Assert.assertEquals(3, appTechnologiesDtoList[0].technologyGroups.get("Store").get("Object Mapping").size());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Store").get("Object Mapping").get("Hibernate OGM").intValue());
        Assert.assertEquals(2, appTechnologiesDtoList[0].technologyGroups.get("Store").get("Object Mapping").get("Hibernate").intValue());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Store").get("Object Mapping").get("EclipseLink").intValue());

        Assert.assertEquals(3, appTechnologiesDtoList[0].technologyGroups.get("Execute").get("Rules & Processes").size());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Execute").get("Rules & Processes").get("Drools").intValue());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Execute").get("Rules & Processes").get("JBPM").intValue());
        Assert.assertEquals(1, appTechnologiesDtoList[0].technologyGroups.get("Execute").get("Rules & Processes").get("iLog").intValue());
    }

}