package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.data.dto.ApplicationCompatibleFilesDto;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.data.dto.ApplicationDto;
import org.jboss.windup.reporting.data.dto.ApplicationHardcodedIpAddressesDto;
import org.jboss.windup.reporting.data.dto.ApplicationIssuesDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.rules.ApplicationCompatibleFilesRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationDetailsRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationHardcodedIpAddressesRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationsRuleProvider;
import org.jboss.windup.reporting.data.rules.FilesRuleProvider;
import org.jboss.windup.reporting.data.rules.IssuesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        validateCompatibleReport(context);
        validateReportIndex(context);
        validateTagsInSourceReport(context);
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
        Optional<ApplicationDetailsDto.ApplicationFileDto> applicationFileDto = applicationDetailsDto.getApplicationFiles().stream()
                .filter(dto -> dto.getRootPath().equals(fileRootPath))
                .findFirst();
        Assert.assertTrue(applicationFileDto.isPresent());

        // Map child files to Files
        List<FileDto> fileDtoList = applicationFileDto.get().getChildrenFileIds().stream()
                .map(childFileId -> Stream.of(filesDtoList)
                        .filter(fileDto -> fileDto.getId().equals(childFileId))
                        .findFirst()
                        .orElse(null)
                ).collect(Collectors.toList());

        // Validate total story points
        int totalStoryPoints = fileDtoList.stream()
                .map(dto -> dto.getStoryPoints())
                .reduce(0, Integer::sum);
        Assert.assertEquals(fileExpectedTotalStoryPoints, totalStoryPoints);

        // Validate child file
        Optional<FileDto> fileDto = fileDtoList.stream()
                .filter(dto -> dto.getPrettyFileName().equals(childFilename))
                .findFirst();
        Assert.assertTrue(fileDto.isPresent());

        // Validate child file tags
        boolean tagsMatched = fileDto.get().getTags().stream().anyMatch(tagDto -> childFileExpectedTags.contains(tagDto.getName()));
        Assert.assertTrue(tagsMatched);

        // Validate child file Story points
        Assert.assertEquals(childFileExpectedStoryPoints, fileDto.get().getStoryPoints());
    }

    private void validateStaticIPReport(GraphContext context) throws IOException {
        File hardcodedFilesJson = new ReportService(context).getApiDataDirectory().resolve(ApplicationHardcodedIpAddressesRuleProvider.PATH + ".json").toFile();
        File filesJson = new ReportService(context).getApiDataDirectory().resolve(FilesRuleProvider.PATH + ".json").toFile();

        // Hardcoded files
        ApplicationHardcodedIpAddressesDto[] appHardcodedIpAddressList = new ObjectMapper().readValue(hardcodedFilesJson, ApplicationHardcodedIpAddressesDto[].class);
        Assert.assertEquals(1, appHardcodedIpAddressList.length);

        // Files
        FileDto[] filesDtoList = new ObjectMapper().readValue(filesJson, FileDto[].class);
        Assert.assertTrue(filesDtoList.length > 1);

        // Assert ip addresses
        Assert.assertEquals(3, appHardcodedIpAddressList[0].getFiles().size());
        Assert.assertEquals(1, appHardcodedIpAddressList[0].getFiles().stream()
                .map(fileDto -> fileDto.getFileId())
                .collect(Collectors.toSet())
                .size()
        );

        Set<String> ipAddresses = appHardcodedIpAddressList[0].getFiles().stream()
                .map(fileDto -> fileDto.getIpAddress())
                .collect(Collectors.toSet());
        Assert.assertEquals(1, ipAddresses.size());
        Assert.assertTrue(ipAddresses.contains("127.0.0.1"));

        boolean lineAndColumnNumbersMatch = appHardcodedIpAddressList[0].getFiles().stream()
                .allMatch(dto -> (dto.getLineNumber() == 65 && dto.getColumnNumber() == 32) ||
                        (dto.getLineNumber() == 721 && dto.getColumnNumber() == 14) ||
                        (dto.getLineNumber() == 725 && dto.getColumnNumber() == 14)
                );
        Assert.assertTrue(lineAndColumnNumbersMatch);

        // Assert file associated to ip address
        Optional<FileDto> sourceFile = Stream.of(filesDtoList)
                .filter(fileDto -> fileDto.getId().equals(appHardcodedIpAddressList[0].getFiles().get(0).getFileId()))
                .findFirst();

        Assert.assertTrue(sourceFile.isPresent());
        Assert.assertEquals("org.apache.wicket.protocol.http.mock.MockHttpServletRequest", sourceFile.get().getPrettyFileName());
    }

    private void validateCompatibleReport(GraphContext context) throws IOException {
        File compatibleFilesJson = new ReportService(context).getApiDataDirectory().resolve(ApplicationCompatibleFilesRuleProvider.PATH + ".json").toFile();

        ApplicationCompatibleFilesDto[] applicationCompatibleFilesDtos = new ObjectMapper().readValue(compatibleFilesJson, ApplicationCompatibleFilesDto[].class);
        Assert.assertEquals(1, applicationCompatibleFilesDtos.length);

        // Assert
        Optional<ApplicationCompatibleFilesDto.FileDto> file1 = applicationCompatibleFilesDtos[0].getArtifacts().stream()
                .filter(artifactDto -> artifactDto.getName().equals("Windup1x-javaee-example.war"))
                .flatMap(artifactDto -> artifactDto.getFiles().stream())
                .filter(fileDto -> fileDto.getFileName().equals("org/jboss/devconf/openshift/HomePage.class"))
                .findFirst();
        Optional<ApplicationCompatibleFilesDto.FileDto> file2 = applicationCompatibleFilesDtos[0].getArtifacts().stream()
                .filter(artifactDto -> artifactDto.getName().equals("Windup1x-javaee-example.war/WEB-INF/lib/joda-time-2.0.jar"))
                .flatMap(artifactDto -> artifactDto.getFiles().stream())
                .filter(fileDto -> fileDto.getFileName().equals("org/joda/time/DateMidnight.class"))
                .findFirst();
        Optional<ApplicationCompatibleFilesDto.FileDto> file3 = applicationCompatibleFilesDtos[0].getArtifacts().stream()
                .filter(artifactDto -> artifactDto.getName().equals("Windup1x-javaee-example.war/WEB-INF/lib/joda-time-2.0.jar"))
                .flatMap(artifactDto -> artifactDto.getFiles().stream())
                .filter(fileDto -> fileDto.getFileName().equals("org/joda/time/Chronology.class"))
                .findFirst();

        Assert.assertTrue(file1.isPresent());
        Assert.assertTrue(file2.isPresent());
        Assert.assertTrue(file3.isPresent());
    }

    private void validateReportIndex(GraphContext context) throws IOException {
        File applicationsJson = new ReportService(context).getApiDataDirectory().resolve(ApplicationsRuleProvider.PATH + ".json").toFile();
        File issuesJson = new ReportService(context).getApiDataDirectory().resolve(IssuesRuleProvider.PATH + ".json").toFile();

        // Assert incidents
        ApplicationDto[] applicationDtos = new ObjectMapper().readValue(applicationsJson, ApplicationDto[].class);
        Assert.assertEquals(1, applicationDtos.length);

        Assert.assertFalse(applicationDtos[0].getIncidents().containsKey("mandatory"));
        Assert.assertFalse(applicationDtos[0].getIncidents().containsKey("potential"));
        Assert.assertEquals(291, applicationDtos[0].getIncidents().get("optional").intValue());
        Assert.assertEquals(10, applicationDtos[0].getIncidents().get("information").intValue());
        Assert.assertEquals(3, applicationDtos[0].getIncidents().get("cloud-mandatory").intValue());

        // Assert story points and incidents from issues
        ApplicationIssuesDto[] issuesDtos = new ObjectMapper().readValue(issuesJson, ApplicationIssuesDto[].class);
        Assert.assertEquals(1, applicationDtos.length);

        Assert.assertFalse(issuesDtos[0].getIssues().containsKey("mandatory"));
        Assert.assertFalse(issuesDtos[0].getIssues().containsKey("potential"));

        int optionalStoryPoints = issuesDtos[0].getIssues().get("optional").stream()
                .map(issueDto -> issueDto.getTotalStoryPoints())
                .reduce(0, Integer::sum);
        int optionalTotalIncidents = issuesDtos[0].getIssues().get("optional").stream()
                .map(issueDto -> issueDto.getTotalIncidents())
                .reduce(0, Integer::sum);

        int informationStoryPoints = issuesDtos[0].getIssues().get("information").stream()
                .map(issueDto -> issueDto.getTotalStoryPoints())
                .reduce(0, Integer::sum);
        int informationTotalIncidents = issuesDtos[0].getIssues().get("information").stream()
                .map(issueDto -> issueDto.getTotalIncidents())
                .reduce(0, Integer::sum);

        int cloudMandatoryStoryPoints = issuesDtos[0].getIssues().get("cloud-mandatory").stream()
                .map(issueDto -> issueDto.getTotalStoryPoints())
                .reduce(0, Integer::sum);
        int cloudMandatoryTotalIncidents = issuesDtos[0].getIssues().get("cloud-mandatory").stream()
                .map(issueDto -> issueDto.getTotalIncidents())
                .reduce(0, Integer::sum);

        Assert.assertEquals(2306, optionalStoryPoints);
        Assert.assertEquals(291, optionalTotalIncidents);

        Assert.assertEquals(0, informationStoryPoints);
        Assert.assertEquals(10, informationTotalIncidents);

        Assert.assertEquals(3, cloudMandatoryStoryPoints);
        Assert.assertEquals(3, cloudMandatoryTotalIncidents);
    }

    private void validateTagsInSourceReport(GraphContext context) throws IOException {
        File filesJson = new ReportService(context).getApiDataDirectory().resolve(FilesRuleProvider.PATH + ".json").toFile();

        // Files
        FileDto[] filesDtoList = new ObjectMapper().readValue(filesJson, FileDto[].class);
        Assert.assertTrue(filesDtoList.length > 1);

        Optional<FileDto> fileDto = Stream.of(filesDtoList)
                .filter(f -> f.getFullPath().contains("wicket-core-1.5.10.jar/org/apache/wicket/application/AbstractClassResolver.java"))
                .findFirst();
        Assert.assertTrue(fileDto.isPresent());
        Assert.assertTrue(fileDto.get().getClassificationsAndHintsTags().contains("GroovyTestHintTag"));
    }

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
