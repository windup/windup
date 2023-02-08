package org.jboss.windup.tests.application;

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
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateUnparsableFilesReportRuleProvider;
import org.jboss.windup.testutil.html.TestUnparsablesUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The archive has the following unparsable items: jee-example-app-1.0.0.ear/unparsableClass.jar!/unparsable.class
 * jee-example-app-1.0.0.ear/META-INF/maven/org.windup.example/unparsable/pom.xml
 * jee-example-app-1.0.0.ear/META-INF/maven/org.windup.example/unparsable/pom.properties jee-example-app-1.0.0.ear/unparsable.jar
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */

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
            super.runTest(context, true, path, false);
            validateUnparsablesReport(context);
        }
    }

    private void validateUnparsablesReport(GraphContext context) {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                ReportModel.TEMPLATE_PATH,
                CreateUnparsableFilesReportRuleProvider.TEMPLATE_UNPARSABLE);
        TestUnparsablesUtil util = new TestUnparsablesUtil();
        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
        util.loadPage(reportPath);
        Assert.assertTrue(util.checkUnparsableFileInReport("jee-example-app-1.0.0.ear/jee-example-services.jar", "NonParsable.class"));
        Assert.assertTrue(util.checkUnparsableFileInReport("jee-example-app-1.0.0.ear/jee-example-services.jar", "NonParsable.xml"));
        Assert.assertFalse(util.checkUnparsableFileInReport("jee-example-app-1.0.0.ear/jee-example-services.jar", "unparsable.map"));
    }

    @Test
    public void testRunWindup_newReports() throws Exception {
        final String path = "../test-files/jee-example-app-1.0.0.ear";
        try (GraphContext context = super.createGraphContext()) {
            super.runTest(context, false, path, false);

            String jsonFilename = ApplicationUnparsableFilesRuleProvider.PATH + ".json";
            File jsonFile = new ReportService(context).getApiDataDirectory().resolve(jsonFilename).toFile();

            ApplicationUnparsableFilesDto[] dtoList = new ObjectMapper().readValue(jsonFile, ApplicationUnparsableFilesDto[].class);
            Assert.assertEquals(1, dtoList.length);

            Optional<ApplicationUnparsableFilesDto.SubProjectDto> subProjectDto = dtoList[0].subProjects.stream()
                    .filter(dto -> dto.path.equals("jee-example-app-1.0.0.ear/jee-example-services.jar"))
                    .findFirst();
            Assert.assertTrue(subProjectDto.isPresent());

            List<String> filenames = subProjectDto.get().unparsableFiles.stream()
                    .map(unparsableFileDto -> unparsableFileDto.fileName)
                    .collect(Collectors.toList());
            Assert.assertTrue(filenames.containsAll(Arrays.asList("NonParsable.class", "NonParsable.xml")));
            Assert.assertFalse(filenames.contains("unparsable.map"));
        }
    }
}
