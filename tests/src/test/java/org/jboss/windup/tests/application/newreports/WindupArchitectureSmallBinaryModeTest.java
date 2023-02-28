package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.data.dto.ApplicationCompatibleFilesDto;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.rules.ApplicationCompatibleFilesRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationDetailsRuleProvider;
import org.jboss.windup.reporting.data.rules.FilesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@RunWith(Arquillian.class)
public class WindupArchitectureSmallBinaryModeTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
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
    public void testRunWindupTiny() throws Exception {
        try (GraphContext context = createGraphContext()) {
            super.runTest(context, false, "../test-files/jee-example-app-1.0.0.ear", false, Arrays.asList("com.acme"));

            // Verify compatible files exists
            File appCompatibleFilesJson = new ReportService(context).getApiDataDirectory().resolve(ApplicationCompatibleFilesRuleProvider.PATH + ".json").toFile();

            ApplicationCompatibleFilesDto[] appCompatibleFilesDtoList = new ObjectMapper().readValue(appCompatibleFilesJson, ApplicationCompatibleFilesDto[].class);
            Assert.assertEquals(1, appCompatibleFilesDtoList.length);
            Assert.assertTrue(appCompatibleFilesDtoList[0].artifacts.size() > 0);

            // Verify file has been identified
            File filesJson = new ReportService(context).getApiDataDirectory().resolve(FilesRuleProvider.PATH + ".json").toFile();

            FileDto[] fileDtos = new ObjectMapper().readValue(filesJson, FileDto[].class);
            Assert.assertTrue(fileDtos.length > 1);

            Optional<FileDto> fileDto = Stream.of(fileDtos)
                    .filter(f -> f.prettyFileName.equals("com.acme.anvil.service.ProductCatalogBean"))
                    .findFirst();
            Assert.assertTrue(fileDto.isPresent());

            // Assert app details
            File appDetailsJson = new ReportService(context).getApiDataDirectory().resolve(ApplicationDetailsRuleProvider.PATH + ".json").toFile();

            ApplicationDetailsDto[] applicationDetailsDtos = new ObjectMapper().readValue(appDetailsJson, ApplicationDetailsDto[].class);
            Assert.assertEquals(1, applicationDetailsDtos.length);

            Optional<ApplicationDetailsDto.ApplicationFileDto> childAppDto = applicationDetailsDtos[0].applicationFiles.stream()
                    .filter(applicationFileDto -> applicationFileDto.maven != null &&
                            Objects.equals(applicationFileDto.maven.description, "Used only to support migration activities.")
                    )
                    .findFirst();
            Assert.assertTrue(childAppDto.isPresent());
        }
    }

}
