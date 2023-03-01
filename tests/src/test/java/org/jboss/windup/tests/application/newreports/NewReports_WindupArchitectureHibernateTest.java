package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.data.dto.ApplicationHibernateDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.rules.ApplicationDetailsRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationHibernateRuleProvider;
import org.jboss.windup.reporting.data.rules.FilesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateMappingFileModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.HibernateMappingFileService;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class NewReports_WindupArchitectureHibernateTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Test
    public void testRunWindupHibernate() throws Exception {
        final String path = "../test-files/hibernate-tutorial-web-3.3.2.GA.war";
        List<String> includeList = Collections.singletonList("nocodescanning");
        List<String> excludeList = Collections.emptyList();
        try (GraphContext context = super.createGraphContext()) {
            super.runTest(context, false, path, null, false, includeList, excludeList);
            validateHibernateFiles(context);
            validateReports(context);
        }
    }

    private void validateHibernateFiles(GraphContext context) {
        HibernateConfigurationFileService cfgService = new HibernateConfigurationFileService(context);

        int hibernateCfgFilesFound = 0;
        for (HibernateConfigurationFileModel model : cfgService.findAll()) {
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
        for (HibernateMappingFileModel model : allMappingModels) {
            numberModelsFound++;
            Assert.assertEquals("3.0", model.getSpecificationVersion());
            if (model.getFileName().equals("Person.hbm.xml")) {
                personHbmFound = true;
                Iterator<HibernateEntityModel> entities = model.getHibernateEntities().iterator();
                Assert.assertTrue(entities.hasNext());

                HibernateEntityModel entity = entities.next();
                Assert.assertEquals("3.0", entity.getSpecificationVersion());
                Assert.assertEquals("PERSON", entity.getTableName());
                Assert.assertEquals("org.hibernate.tutorial.domain.Person", entity.getJavaClass().getQualifiedName());

                Assert.assertFalse(entities.hasNext());
            } else if (model.getFileName().equals("Event.hbm.xml")) {
                eventHbmFound = true;

                Iterator<HibernateEntityModel> entities = model.getHibernateEntities().iterator();
                Assert.assertTrue(entities.hasNext());

                HibernateEntityModel entity = entities.next();
                Assert.assertEquals("3.0", entity.getSpecificationVersion());
                Assert.assertEquals("EVENTS", entity.getTableName());
                Assert.assertEquals("org.hibernate.tutorial.domain.Event", entity.getJavaClass().getQualifiedName());

                Assert.assertFalse(entities.hasNext());
            } else if (model.getFileName().equals("Item.hbm.xml")) {
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

    private void validateReports(GraphContext context) throws IOException {
        File applicationDetailsJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationDetailsRuleProvider.PATH + ".json")
                .toFile();
        File filesJson = new ReportService(context).getApiDataDirectory()
                .resolve(FilesRuleProvider.PATH + ".json").toFile();


        ApplicationDetailsDto[] appDetailsDtoList = new ObjectMapper().readValue(applicationDetailsJson, ApplicationDetailsDto[].class);
        Assert.assertEquals(1, appDetailsDtoList.length);

        FileDto[] filesDtoList = new ObjectMapper().readValue(filesJson, FileDto[].class);
        Assert.assertTrue(filesDtoList.length > 1);

        List<FileDto> filesDtoCollection = Arrays.asList(filesDtoList);

        // Validate application details
        validateAppDetails(filesDtoCollection, appDetailsDtoList[0], "hibernate-tutorial-web-3.3.2.GA.war", "META-INF/MANIFEST.MF", "Manifest");
        validateAppDetails(filesDtoCollection, appDetailsDtoList[0], "hibernate-tutorial-web-3.3.2.GA.war", "WEB-INF/classes/hibernate.cfg.xml", "Hibernate Cfg");
        validateAppDetails(filesDtoCollection, appDetailsDtoList[0], "hibernate-tutorial-web-3.3.2.GA.war", "WEB-INF/classes/org/hibernate/tutorial/domain/Event.hbm.xml", "Hibernate Mapping");

        //
        validateHibernateReport(context);
    }

    private void validateAppDetails(
            List<FileDto> filesDtoCollection,
            ApplicationDetailsDto appDetailsDto,
            String filename,
            String prettyFilename,
            String tag
    ) {
        // Verify app details exists
        Optional<ApplicationDetailsDto.ApplicationFileDto> appFileDto = appDetailsDto.getApplicationFiles().stream()
                .filter(dto -> dto.getFileName().equals(filename))
                .findFirst();
        Assert.assertTrue(appFileDto.isPresent());

        // Get child files
        List<FileDto> appChildrenFiles = appFileDto.get().getChildrenFileIds().stream()
                .map(childFileId -> filesDtoCollection.stream()
                        .filter(fileDto -> fileDto.getId().equals(childFileId))
                        .findFirst()
                        .orElse(null)
                ).collect(Collectors.toList());

        // Verify file and tag
        boolean pathAndTagExist = appChildrenFiles.stream()
                .filter(fileDto -> fileDto.getPrettyFileName().equals(prettyFilename))
                .allMatch(fileDto -> fileDto.getTags().stream()
                        .map(f -> f.getName())
                        .anyMatch(s -> s.equals(tag))
                );
        Assert.assertTrue(pathAndTagExist);
    }

    private void validateHibernateReport(GraphContext context) throws IOException {
        File hibernateJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationHibernateRuleProvider.PATH + ".json")
                .toFile();
        ApplicationHibernateDto[] appHibernatesDtoList = new ObjectMapper().readValue(hibernateJson, ApplicationHibernateDto[].class);
        Assert.assertEquals(1, appHibernatesDtoList.length);

        Assert.assertEquals(1, appHibernatesDtoList[0].getHibernateConfigurations().size());
        Assert.assertEquals(1, appHibernatesDtoList[0].getHibernateConfigurations().get(0).getSessionFactories().size());

        Map<String, String> properties = appHibernatesDtoList[0].getHibernateConfigurations().get(0).getSessionFactories().get(0).getProperties();
        Assert.assertEquals("2", properties.get("connection.pool_size"));
        Assert.assertEquals("org.hibernate.cache.NoCacheProvider", properties.get("cache.provider_class"));
        Assert.assertEquals("org.hibernate.dialect.HSQLDialect", properties.get("dialect"));
        Assert.assertEquals("org.hibernate.context.ManagedSessionContext", properties.get("current_session_context_class"));

        boolean entityItemExists = appHibernatesDtoList[0].getEntities().stream().anyMatch(entityDto -> entityDto.getTableName().equals("Items") && entityDto.getClassName().equals("org.hibernate.test.cache.Item"));
        boolean entityPersonExists = appHibernatesDtoList[0].getEntities().stream().anyMatch(entityDto -> entityDto.getTableName().equals("PERSON") && entityDto.getClassName().equals("org.hibernate.tutorial.domain.Person"));
        boolean entityEventsExists = appHibernatesDtoList[0].getEntities().stream().anyMatch(entityDto -> entityDto.getTableName().equals("EVENTS") && entityDto.getClassName().equals("org.hibernate.tutorial.domain.Event"));

        Assert.assertTrue(entityItemExists);
        Assert.assertTrue(entityPersonExists);
        Assert.assertTrue(entityEventsExists);
    }

}
