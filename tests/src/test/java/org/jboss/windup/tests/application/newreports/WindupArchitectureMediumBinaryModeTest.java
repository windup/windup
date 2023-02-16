package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.rules.ApplicationDetailsRuleProvider;
import org.jboss.windup.reporting.data.rules.FilesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class WindupArchitectureMediumBinaryModeTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-tattletale"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addClass(WindupArchitectureMediumBinaryModeTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    /**
     * Validate that the report pages were generated correctly
     */
    protected void validateReports(GraphContext context) throws IOException {
        validateApplicationDetailsReport(context);
        validateStaticIPReport(context);
//        validateCompatibleReport(context);
//        validateReportIndex(context);
//        validateTagsInSourceReport(context);
    }

    private void validateApplicationDetailsReport(GraphContext context) throws IOException {
        File appDetailsJson = new ReportService(context).getApiDataDirectory()
                .resolve(ApplicationDetailsRuleProvider.PATH + ".json")
                .toFile();

        // Application details
        ApplicationDetailsDto[] appDetailsDtoList = new ObjectMapper().readValue(appDetailsJson, ApplicationDetailsDto[].class);
        Assert.assertEquals(1, appDetailsDtoList.length);

        // Verify Windup1x-javaee-example.war
        validateApplicationDetails(context, appDetailsDtoList[0], "Windup1x-javaee-example.war", 2, "META-INF/maven/javaee/javaee/pom.properties", List.of("Properties"), 0);
        validateApplicationDetails(context, appDetailsDtoList[0], "Windup1x-javaee-example.war/WEB-INF/lib/joda-time-2.0.jar", 64, "org.joda.time.tz.DateTimeZoneBuilder", List.of("Decompiled Java File"), 32);
        validateApplicationDetails(context, appDetailsDtoList[0], "Windup1x-javaee-example.war/WEB-INF/lib/slf4j-api-1.6.1.jar", 16, "org.slf4j.LoggerFactory", List.of("Decompiled Java File"), 16);
        validateApplicationDetails(context, appDetailsDtoList[0], "Windup1x-javaee-example.war/WEB-INF/lib/wicket-devutils-1.5.10.jar", 0, "wicket.properties", List.of("Properties"), 0);
        validateApplicationDetails(context, appDetailsDtoList[0], "Windup1x-javaee-example.war/WEB-INF/lib/wicket-request-1.5.10.jar", 24, "org.apache.wicket.request.Response", List.of("Decompiled Java File"), 24);
    }

    private void validateApplicationDetails(
            GraphContext context,
            ApplicationDetailsDto applicationDetailsDto,
            String fileRootPath,
            int fileExpectedTotalStoryPoints,
            String childFilename,
            List<String> childFileExpectedTags,
            int childFileExpectedStoryPoints
    ) throws IOException {
        File filesJson = new ReportService(context).getApiDataDirectory().resolve(FilesRuleProvider.PATH + ".json").toFile();

        // Files
        FileDto[] filesDtoList = new ObjectMapper().readValue(filesJson, FileDto[].class);
        Assert.assertTrue(filesDtoList.length > 1);

        // Find file
        Optional<ApplicationDetailsDto.ApplicationFileDto> applicationFileDto = applicationDetailsDto.applicationFiles.stream()
                .filter(dto -> dto.rootPath.equals(fileRootPath))
                .findFirst();
        Assert.assertTrue(applicationFileDto.isPresent());

        // Map child files to Files
        List<FileDto> fileDtoList = applicationFileDto.get().childrenFileIds.stream()
                .map(childFileId -> Stream.of(filesDtoList)
                        .filter(fileDto -> fileDto.id.equals(childFileId))
                        .findFirst()
                        .orElse(null)
                ).collect(Collectors.toList());

        // Validate total story points
        int totalStoryPoints = fileDtoList.stream()
                .map(dto -> dto.storyPoints)
                .reduce(0, Integer::sum);
        Assert.assertEquals(fileExpectedTotalStoryPoints, totalStoryPoints);

        // Validate child file
        Optional<FileDto> fileDto = fileDtoList.stream()
                .filter(dto -> dto.prettyFileName.equals(childFilename))
                .findFirst();
        Assert.assertTrue(fileDto.isPresent());

        // Validate child file tags
        boolean tagsMatched = fileDto.get().tags.stream().anyMatch(tagDto -> childFileExpectedTags.contains(tagDto.name));
        Assert.assertTrue(tagsMatched);

        // Validate child file Story points
        Assert.assertEquals(childFileExpectedStoryPoints, fileDto.get().storyPoints);
    }

    private void validateStaticIPReport(GraphContext context) {
//        ReportService reportService = new ReportService(context);
//        ReportModel reportModel = reportService.getUniqueByProperty(
//                ReportModel.TEMPLATE_PATH,
//                CreateHardcodedIPAddressReportRuleProvider.TEMPLATE_REPORT);
//        TestHardcodedPReportUtil util = new TestHardcodedPReportUtil();
//        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
//        util.loadPage(reportPath);
//        Assert.assertTrue(util
//                .checkHardcodedIPInReport(
//                        "org.apache.wicket.protocol.http.mock.MockHttpServletRequest (65, 32)",
//                        "Line: 65, Position: 32", "127.0.0.1"));
//        Assert.assertTrue(util
//                .checkHardcodedIPInReport(
//                        "org.apache.wicket.protocol.http.mock.MockHttpServletRequest (721, 14)",
//                        "Line: 721, Position: 14", "127.0.0.1"));
//        Assert.assertTrue(util
//                .checkHardcodedIPInReport(
//                        "org.apache.wicket.protocol.http.mock.MockHttpServletRequest (725, 14)",
//                        "Line: 725, Position: 14", "127.0.0.1"));

    }

//    private void validateCompatibleReport(GraphContext context) {
//        ReportService reportService = new ReportService(context);
//        ReportModel reportModel = reportService.getUniqueByProperty(
//                ReportModel.TEMPLATE_PATH,
//                CreateCompatibleFileReportRuleProvider.TEMPLATE_APPLICATION_REPORT);
//        TestCompatibleReportUtil util = new TestCompatibleReportUtil();
//
//
//        Path reportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
//        util.loadPage(reportPath);
//        Assert.assertTrue(util
//                .checkFileInReport("org/jboss/devconf/openshift/HomePage.class", ""));
//        Assert.assertTrue(util
//                .checkFileInReport("org/joda/time/DateMidnight.class", ""));
//        Assert.assertTrue(util
//                .checkFileInReport(
//                        "org/joda/time/Chronology.class", ""));
//        Assert.assertTrue("An application has duplicate entries for a single file.", util.checkTableWithoutDuplicates());
//
//    }
//
//    private void validateReportIndex(GraphContext context) {
//        ReportService reportService = new ReportService(context);
//        ReportModel reportModel = reportService.getUniqueByProperty(
//                ReportModel.TEMPLATE_PATH,
//                CreateReportIndexRuleProvider.TEMPLATE);
//        Path appReportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());
//        TestReportIndexReportUtil util = new TestReportIndexReportUtil();
//        util.loadPage(appReportPath);
//
//        Assert.assertTrue(util.checkIncidentByCategoryRow("optional", 291, 2306));
//        Assert.assertTrue(util.checkIncidentByCategoryRow("mandatory", 0, 0));
//        Assert.assertTrue(util.checkIncidentByCategoryRow("information", 10, 0));
//        Assert.assertTrue(util.checkIncidentByCategoryRow("cloud-mandatory", 3, 3));
//        Assert.assertTrue(util.checkIncidentByCategoryRow("potential", 0, 0));
//    }
//
//    private void validateTagsInSourceReport(GraphContext context) throws IOException {
//        FileService fileService = new FileService(context);
//        boolean reportFound = false;
//        for (FileModel fileModel : fileService.findByFilenameRegex("AbstractClassResolver.java")) {
//            if (fileModel.getPrettyPath().contains("wicket-core-1.5.10.jar/org/apache/wicket/application/AbstractClassResolver.java")) {
//                Assert.assertTrue(fileModel instanceof SourceFileModel);
//                ReportService reportService = new ReportService(context);
//                SourceReportService sourceReportService = new SourceReportService(context);
//                SourceReportModel sourceReportModel = sourceReportService.getSourceReportForFileModel(fileModel);
//                Path sourceReportPath = reportService.getReportDirectory().resolve(sourceReportModel.getReportFilename());
//
//                String sourceReportContents = FileUtils.readFileToString(sourceReportPath.toFile());
//                Assert.assertTrue(sourceReportContents.contains("<span class=\"label label-info\" title=\"GroovyTestHintTag\">GroovyTestHintTag</span>"));
//                reportFound = true;
//            }
//        }
//
//        Assert.assertTrue(reportFound);
//    }

    protected void validateManifestEntries(GraphContext context) throws Exception {
        JarManifestService jarManifestService = new JarManifestService(context);
        Iterable<JarManifestModel> manifests = jarManifestService.findAll();

        int numberFound = 0;
        boolean warManifestFound = false;
        for (JarManifestModel manifest : manifests) {
            if (manifest.getArchive().getFileName().equals("Windup1x-javaee-example.war") && !manifest.getFilePath().contains("/WEB-INF/")) {
                Assert.assertEquals("1.0", manifest.getElement().property("Manifest-Version").value());
                Assert.assertEquals("Plexus Archiver", manifest.getElement().property("Archiver-Version").value());
                Assert.assertEquals("Apache Maven", manifest.getElement().property("Created-By").value());
                warManifestFound = true;
            }

            numberFound++;
        }
        Assert.assertEquals(10, numberFound);
        Assert.assertTrue(warManifestFound);
    }

}
