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
import org.jboss.windup.reporting.data.dto.ApplicationSpringBeansDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.rules.ApplicationDetailsRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationSpringBeansRuleProvider;
import org.jboss.windup.reporting.data.rules.FilesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.model.SpringConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.association.JNDIReferenceModel;
import org.jboss.windup.rules.apps.javaee.service.SpringConfigurationFileService;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class WindupArchitectureSpringSmallTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
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
    public void testRunWindupSmallSpringApp() throws Exception {
        try (GraphContext context = super.createGraphContext()) {
            final String path = "../test-files/spring-small-example.war";

            List<String> includeList = Collections.singletonList("nocodescanning");
            List<String> excludeList = Collections.emptyList();
            super.runTest(context, false, path, null, false, includeList, excludeList);

            validateSpringBeans(context);
            validateAppDetails(context);
            validateSpringBeanReport(context);
        }
    }

    /**
     * Validate that the spring beans were extracted correctly
     */
    private void validateSpringBeans(GraphContext context) {
        SpringConfigurationFileService springConfigurationFileService = new SpringConfigurationFileService(context);
        Iterable<SpringConfigurationFileModel> models = springConfigurationFileService.findAll();

        int numberFound = 0;
        boolean foundSpringMvcContext = false;
        boolean foundSpringBusinessContext = false;

        boolean foundDataSourceJNDIReference = false;
        boolean foundEntityManagerJNDIReference = false;
        for (SpringConfigurationFileModel model : models) {
            numberFound++;
            if (model.getFileName().equals("spring-mvc-context.xml")) {
                foundSpringMvcContext = true;
                Iterator<SpringBeanModel> beanIter = model.getSpringBeans().iterator();
                SpringBeanModel springBean = beanIter.next();

                Assert.assertEquals("org.springframework.web.servlet.view.InternalResourceViewResolver", springBean
                        .getJavaClass().getQualifiedName());

                Assert.assertFalse(beanIter.hasNext());
            } else if (model.getFileName().equals("spring-business-context.xml")) {
                foundSpringBusinessContext = true;

                for (SpringBeanModel springBeanModel : model.getSpringBeans()) {
                    if (springBeanModel instanceof JNDIReferenceModel) {
                        if ("dataSource".equals(springBeanModel.getSpringBeanName()))
                            foundDataSourceJNDIReference = true;
                        else if ("entityManager".equals(springBeanModel.getSpringBeanName()))
                            foundEntityManagerJNDIReference = true;
                    }
                }
            }
        }
        Assert.assertEquals(2, numberFound);
        Assert.assertTrue(foundSpringMvcContext);
        Assert.assertTrue(foundSpringBusinessContext);
        Assert.assertTrue(foundDataSourceJNDIReference);
        Assert.assertTrue(foundEntityManagerJNDIReference);
    }

    private void validateSpringBeanReport(GraphContext context) throws IOException {
        File springJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationSpringBeansRuleProvider.PATH + ".json")
                .toFile();

        ApplicationSpringBeansDto[] appSpringDtoList = new ObjectMapper().readValue(springJson, ApplicationSpringBeansDto[].class);
        Assert.assertEquals(1, appSpringDtoList.length);

        // Assert bean
        Optional<ApplicationSpringBeansDto.SpringBeanDto> springBeanDto = appSpringDtoList[0].beans.stream()
                .filter(dto -> dto.beanName.equals("WEB-INF/spring-mvc-context.xml"))
                .findFirst();
        Assert.assertTrue(springBeanDto.isPresent());
        Assert.assertEquals("org.springframework.web.servlet.view.InternalResourceViewResolver", springBeanDto.get().className);
    }

    private void validateAppDetails(GraphContext context) throws IOException {
        File appDetailsJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationDetailsRuleProvider.PATH + ".json")
                .toFile();
        File filesJson = new ReportService(context).getApiDataDirectory()
                .resolve(FilesRuleProvider.PATH + ".json")
                .toFile();

        ApplicationDetailsDto[] appDetailsDtoList = new ObjectMapper().readValue(appDetailsJson, ApplicationDetailsDto[].class);
        Assert.assertEquals(1, appDetailsDtoList.length);

        FileDto[] filesDtoList = new ObjectMapper().readValue(filesJson, FileDto[].class);
        Assert.assertTrue(appDetailsDtoList.length > 0);

        //
        Optional<ApplicationDetailsDto.ApplicationFileDto> sectionDto = appDetailsDtoList[0].applicationFiles.stream()
                .filter(dto -> dto.fileName.equals("spring-small-example.war"))
                .findFirst();
        Assert.assertTrue(sectionDto.isPresent());

        Optional<FileDto> childFile = sectionDto.get().childrenFileIds.stream()
                .map(childFileId -> Arrays.stream(filesDtoList).filter(f -> f.id.equals(childFileId)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .filter(dto -> dto.prettyFileName.equals("WEB-INF/spring-business-context.xml"))
                .findFirst();
        Assert.assertTrue(childFile.isPresent());
        Assert.assertTrue(childFile.get().tags.stream()
                .map(f -> f.name)
                .collect(Collectors.toList())
                .contains("Spring XML")
        );
    }
}
