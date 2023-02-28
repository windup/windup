package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.data.dto.ApplicationUnparsableFilesDto;
import org.jboss.windup.reporting.data.rules.ApplicationUnparsableFilesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class UnparsablesReportTest extends WindupArchitectureTest {

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
                .addClass(WindupArchitectureTest.class);
    }

    @Test
    public void testRunWindup() throws Exception {
        final String path = "../test-files/jee-example-app-1.0.0.ear";
        try (GraphContext context = super.createGraphContext()) {
            super.runTest(context, false, path, false);
            validateUnparsablesReport(context);
        }
    }

    private void validateUnparsablesReport(GraphContext context) throws IOException {
        File jsonFile = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationUnparsableFilesRuleProvider.PATH + ".json")
                .toFile();

        ApplicationUnparsableFilesDto[] dtoList = new ObjectMapper().readValue(jsonFile, ApplicationUnparsableFilesDto[].class);
        Assert.assertEquals(1, dtoList.length);

        Optional<ApplicationUnparsableFilesDto.SubProjectDto> subProjectDto = dtoList[0].getSubProjects().stream()
                .filter(dto -> dto.getPath().equals("jee-example-app-1.0.0.ear/jee-example-services.jar"))
                .findFirst();
        Assert.assertTrue(subProjectDto.isPresent());

        List<String> filenames = subProjectDto.get().getUnparsableFiles().stream()
                .map(unparsableFileDto -> unparsableFileDto.getFileName())
                .collect(Collectors.toList());
        Assert.assertTrue(filenames.containsAll(Arrays.asList("NonParsable.class", "NonParsable.xml")));
        Assert.assertFalse(filenames.contains("unparsable.map"));
    }

}
