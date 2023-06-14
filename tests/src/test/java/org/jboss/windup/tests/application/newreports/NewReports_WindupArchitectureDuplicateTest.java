package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.data.dto.ApplicationDependenciesDto;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.data.dto.ApplicationDto;
import org.jboss.windup.reporting.data.dto.ApplicationIssuesDto;
import org.jboss.windup.reporting.data.rules.ApplicationDetailsRuleProvider;
import org.jboss.windup.reporting.data.rules.ApplicationsRuleProvider;
import org.jboss.windup.reporting.data.rules.DependenciesRuleProvider;
import org.jboss.windup.reporting.data.rules.IssuesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(Arquillian.class)
public class NewReports_WindupArchitectureDuplicateTest extends WindupArchitectureTest {
    private static final String MAIN_APP_FILENAME = "duplicate-ear-test-1.ear";
    private static final String SECOND_APP_FILENAME = "duplicate-ear-test-2.ear";
    private static final String THIRD_APP_FILENAME = "duplicate-ear-test-3.ear";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-project"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-tattletale"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"))
                .addAsResource(new File("src/test/xml/DuplicateTestRules.windup.xml"))
                .addAsResource(new File("src/test/xml/rules/embedded-libraries/embedded-cache-libraries.windup.xml"))
                .addAsResource(new File("src/test/xml/rules/embedded-libraries/embedded-framework-libraries.windup.xml"));
    }

    @Test
    public void testRunWindupDuplicateEAR() throws Exception {
        final String path1 = "../test-files/duplicate/" + MAIN_APP_FILENAME;
        final String path2 = "../test-files/duplicate/" + SECOND_APP_FILENAME;
        final String path3 = "../test-files/duplicate/" + THIRD_APP_FILENAME;
        final Path outputPath = getDefaultPath();

        try (GraphContext context = createGraphContext(outputPath)) {
            List<String> inputPaths = Arrays.asList(path1, path2, path3);

            super.runTest(context, false, inputPaths, false);
            validateApplicationList(context);
            validateApplicationDashboard(context);
            validateMigrationIssues(context);
            validateDependencies(context);
            validateApplicationDetails(context);
        } finally {
            //FileUtils.deleteDirectory(testTempPath.toFile());
        }
    }

    private void validateApplicationList(GraphContext graphContext) throws IOException {
        File applicationsJson = new ReportService(graphContext).getWindupUIApiDirectory()
                .resolve(ApplicationsRuleProvider.PATH + ".json")
                .toFile();

        ApplicationDto[] appDetailsDtoList = new ObjectMapper().readValue(applicationsJson, ApplicationDto[].class);
        Assert.assertTrue(appDetailsDtoList.length > 0);

        Supplier<Stream<ApplicationDto>> appDetailsDtoStream = () -> Arrays.stream(appDetailsDtoList);

        Optional<ApplicationDto> mainApp = appDetailsDtoStream.get()
                .filter(applicationDto -> applicationDto.getName().equals(MAIN_APP_FILENAME))
                .findFirst();
        Optional<ApplicationDto> secondApp = appDetailsDtoStream.get()
                .filter(applicationDto -> applicationDto.getName().equals(SECOND_APP_FILENAME))
                .findFirst();
        Optional<ApplicationDto> thirdApp = appDetailsDtoStream.get()
                .filter(applicationDto -> applicationDto.getName().equals(THIRD_APP_FILENAME))
                .findFirst();
        Optional<ApplicationDto> sharedLibsApp = appDetailsDtoStream.get()
                .filter(applicationDto -> applicationDto.getName().equals(ProjectService.SHARED_LIBS_APP_NAME))
                .findFirst();

        Assert.assertTrue(mainApp.isPresent());
        Assert.assertEquals(649, mainApp.get().getStoryPoints());
        Assert.assertEquals(597, mainApp.get().getStoryPointsInSharedArchives());

        Assert.assertTrue(secondApp.isPresent());
        Assert.assertEquals(649, secondApp.get().getStoryPoints());
        Assert.assertEquals(597, secondApp.get().getStoryPointsInSharedArchives());

        Assert.assertTrue(thirdApp.isPresent());
        Assert.assertEquals(589, thirdApp.get().getStoryPoints());
        Assert.assertEquals(589, thirdApp.get().getStoryPointsInSharedArchives());

        Assert.assertTrue(sharedLibsApp.isPresent());
        Assert.assertEquals(597, sharedLibsApp.get().getStoryPoints());
        Assert.assertEquals(0, sharedLibsApp.get().getStoryPointsInSharedArchives());
    }

    private void validateApplicationDashboard(GraphContext context) throws IOException {
        File applicationsJson = new ReportService(context).getWindupUIApiDirectory()
                .resolve(ApplicationsRuleProvider.PATH + ".json")
                .toFile();
        File appIssuesJson = new ReportService(context).getWindupUIApiDirectory()
                .resolve(IssuesRuleProvider.PATH + ".json")
                .toFile();

        // Application
        ApplicationDto[] appDtoList = new ObjectMapper().readValue(applicationsJson, ApplicationDto[].class);

        Optional<ApplicationDto> mainApp = Arrays.stream(appDtoList)
                .filter(dto -> dto.getName().equals(MAIN_APP_FILENAME))
                .findFirst();
        Optional<ApplicationDto> secondApp = Arrays.stream(appDtoList)
                .filter(dto -> dto.getName().equals(SECOND_APP_FILENAME))
                .findFirst();
        Optional<ApplicationDto> sharedApp = Arrays.stream(appDtoList)
                .filter(dto -> dto.getName().equals(ProjectService.SHARED_LIBS_APP_NAME))
                .findFirst();

        Assert.assertTrue(mainApp.isPresent());
        Assert.assertTrue(secondApp.isPresent());
        Assert.assertTrue(sharedApp.isPresent());

        // Issues
        ApplicationIssuesDto[] appIssuesDtoList = new ObjectMapper().readValue(appIssuesJson, ApplicationIssuesDto[].class);

        Optional<ApplicationIssuesDto> mainAppIssues = Stream.of(appIssuesDtoList)
                .filter(dto -> dto.getApplicationId().equals(mainApp.get().getId()))
                .findFirst();
        Optional<ApplicationIssuesDto> secondAppIssues = Stream.of(appIssuesDtoList)
                .filter(dto -> dto.getApplicationId().equals(secondApp.get().getId()))
                .findFirst();
        Optional<ApplicationIssuesDto> sharedAppIssues = Stream.of(appIssuesDtoList)
                .filter(dto -> dto.getApplicationId().equals(sharedApp.get().getId()))
                .findFirst();

        Assert.assertTrue(mainAppIssues.isPresent());
        Assert.assertTrue(secondAppIssues.isPresent());
        Assert.assertTrue(sharedAppIssues.isPresent());

        // Issues
        assertApplicationIssues(mainAppIssues.get(), "mandatory", 1, 3);
        assertApplicationIssues(mainAppIssues.get(), "optional", 80, 636);
        assertApplicationIssues(mainAppIssues.get(), "potential", 0, 0);
        assertApplicationIssues(mainAppIssues.get(), "cloud-mandatory", 2, 10);
        assertApplicationIssues(mainAppIssues.get(), "information", 11, 0);

        assertApplicationIssues(secondAppIssues.get(), "mandatory", 1, 3);
        assertApplicationIssues(secondAppIssues.get(), "optional", 80, 636);
        assertApplicationIssues(secondAppIssues.get(), "potential", 0, 0);
        assertApplicationIssues(secondAppIssues.get(), "cloud-mandatory", 2, 10);
        assertApplicationIssues(secondAppIssues.get(), "information", 11, 0);

        assertApplicationIssues(sharedAppIssues.get(), "mandatory", 1, 3);
        assertApplicationIssues(sharedAppIssues.get(), "optional", 77, 584);
        assertApplicationIssues(sharedAppIssues.get(), "potential", 0, 0);
        assertApplicationIssues(sharedAppIssues.get(), "cloud-mandatory", 2, 10);
        assertApplicationIssues(sharedAppIssues.get(), "information", 10, 0);
    }

    private void assertApplicationIssues(ApplicationIssuesDto appIssuesDto, String category, int expectedIncidents, int expectedStoryPoints) {
        int mainAppPotentialIncidents = appIssuesDto.getIssues()
                .getOrDefault(category, Collections.emptyList()).stream()
                .map(dto -> dto.getTotalIncidents())
                .reduce(0, Integer::sum);
        int mainAppPotentialStoryPoints = appIssuesDto.getIssues()
                .getOrDefault(category, Collections.emptyList()).stream()
                .map(dto -> dto.getTotalStoryPoints())
                .reduce(0, Integer::sum);

        Assert.assertEquals(expectedIncidents, mainAppPotentialIncidents);
        Assert.assertEquals(expectedStoryPoints, mainAppPotentialStoryPoints);
    }

    private void validateMigrationIssues(GraphContext graphContext) throws IOException {
        File applicationsJson = new ReportService(graphContext).getWindupUIApiDirectory()
                .resolve(ApplicationsRuleProvider.PATH + ".json")
                .toFile();
        File issuesJson = new ReportService(graphContext).getWindupUIApiDirectory()
                .resolve(IssuesRuleProvider.PATH + ".json")
                .toFile();

        // Applications
        ApplicationDto[] appDtoList = new ObjectMapper().readValue(applicationsJson, ApplicationDto[].class);
        Optional<ApplicationDto> mainApp = Arrays.stream(appDtoList)
                .filter(dto -> dto.getName().equals(MAIN_APP_FILENAME))
                .findFirst();
        Optional<ApplicationDto> secondApp = Arrays.stream(appDtoList)
                .filter(dto -> dto.getName().equals(SECOND_APP_FILENAME))
                .findFirst();

        Assert.assertTrue(mainApp.isPresent());
        Assert.assertTrue(secondApp.isPresent());

        // Get app issues
        ApplicationIssuesDto[] appIssuesDtoList = new ObjectMapper().readValue(issuesJson, ApplicationIssuesDto[].class);

        Optional<ApplicationIssuesDto> mainAppIssues = Arrays.stream(appIssuesDtoList)
                .filter(dto -> dto.getApplicationId().equals(mainApp.get().getId()))
                .findFirst();
        Optional<ApplicationIssuesDto> secondAppIssues = Arrays.stream(appIssuesDtoList)
                .filter(dto -> dto.getApplicationId().equals(secondApp.get().getId()))
                .findFirst();

        Assert.assertTrue(mainAppIssues.isPresent());
        Assert.assertTrue(secondAppIssues.isPresent());

        // Get issues
        Optional<ApplicationIssuesDto.IssueDto> maven1Issue = mainAppIssues.get().getIssues().values().stream()
                .flatMap(Collection::stream)
                .filter(dto -> dto.getName().equals("Maven POM (pom.xml)"))
                .findFirst();
        Optional<ApplicationIssuesDto.IssueDto> unparsable1Issue = mainAppIssues.get().getIssues().values().stream()
                .flatMap(Collection::stream)
                .filter(dto -> dto.getName().equals("Unparsable XML File"))
                .findFirst();

        Optional<ApplicationIssuesDto.IssueDto> maven2Issue = secondAppIssues.get().getIssues().values().stream()
                .flatMap(Collection::stream)
                .filter(dto -> dto.getName().equals("Maven POM (pom.xml)"))
                .findFirst();
        Optional<ApplicationIssuesDto.IssueDto> unparsable2Issue = secondAppIssues.get().getIssues().values().stream()
                .flatMap(Collection::stream)
                .filter(dto -> dto.getName().equals("Unparsable XML File"))
                .findFirst();

        Assert.assertTrue(maven1Issue.isPresent());
        Assert.assertTrue(unparsable1Issue.isPresent());
        Assert.assertTrue(maven2Issue.isPresent());
        Assert.assertTrue(unparsable2Issue.isPresent());

        Assert.assertEquals("Info", maven1Issue.get().getEffort().getDescription());
        Assert.assertEquals(8, maven1Issue.get().getTotalIncidents());
        Assert.assertEquals(0, maven1Issue.get().getTotalStoryPoints());

        Assert.assertEquals("Info", unparsable1Issue.get().getEffort().getDescription());
        Assert.assertEquals(2, unparsable1Issue.get().getTotalIncidents());
        Assert.assertEquals(0, unparsable1Issue.get().getTotalStoryPoints());

        Assert.assertEquals("Info", maven2Issue.get().getEffort().getDescription());
        Assert.assertEquals(8, maven2Issue.get().getTotalIncidents());
        Assert.assertEquals(0, maven2Issue.get().getTotalStoryPoints());

        Assert.assertEquals("Info", unparsable2Issue.get().getEffort().getDescription());
        Assert.assertEquals(2, unparsable2Issue.get().getTotalIncidents());
        Assert.assertEquals(0, unparsable2Issue.get().getTotalStoryPoints());
    }

    private void validateDependencies(GraphContext graphContext) throws IOException {
        File dependenciesJson = new ReportService(graphContext).getWindupUIApiDirectory()
                .resolve(DependenciesRuleProvider.PATH + ".json")
                .toFile();

        ApplicationDependenciesDto[] appDependenciesDtoList = new ObjectMapper().readValue(dependenciesJson, ApplicationDependenciesDto[].class);

        // Verify dependencies exists
        Optional<ApplicationDependenciesDto.DependencyDto> log4j = Arrays.stream(appDependenciesDtoList)
                .flatMap(dto -> dto.getDependencies().stream())
                .filter(dto -> dto.getName().equals("log4j-1.2.6.jar"))
                .findFirst();
        Optional<ApplicationDependenciesDto.DependencyDto> jeeExample = Arrays.stream(appDependenciesDtoList)
                .flatMap(dto -> dto.getDependencies().stream())
                .filter(dto -> dto.getName().equals("jee-example-services.jar"))
                .findFirst();
        Optional<ApplicationDependenciesDto.DependencyDto> ehcache = Arrays.stream(appDependenciesDtoList)
                .flatMap(dto -> dto.getDependencies().stream())
                .filter(dto -> dto.getName().equals("ehcache-1.6.2.jar"))
                .findFirst();
        Optional<ApplicationDependenciesDto.DependencyDto> hibernateEhcache = Arrays.stream(appDependenciesDtoList)
                .flatMap(dto -> dto.getDependencies().stream())
                .filter(dto -> dto.getName().equals("hibernate-ehcache-3.6.9.Final.jar"))
                .findFirst();

        Optional<ApplicationDependenciesDto.DependencyDto> commonsLang = Arrays.stream(appDependenciesDtoList)
                .flatMap(dto -> dto.getDependencies().stream())
                .filter(dto -> dto.getName().equals("commons-lang-2.5.jar"))
                .findFirst();

        Assert.assertTrue(log4j.isPresent());
        Assert.assertTrue(jeeExample.isPresent());
        Assert.assertTrue(ehcache.isPresent());
        Assert.assertTrue(hibernateEhcache.isPresent());
        Assert.assertTrue(commonsLang.isPresent());

        // Verify
        Assert.assertEquals("org.windup.example:jee-example-services:1.0.0", jeeExample.get().getMavenIdentifier());
        Assert.assertEquals("1c603ef950e920769c3389e6a1282e3c96f360d0", jeeExample.get().getSha1());
        Assert.assertEquals("1.0.0", jeeExample.get().getVersion());
        Assert.assertNull(jeeExample.get().getOrganization());

        Assert.assertEquals("commons-lang:commons-lang:2.5", commonsLang.get().getMavenIdentifier());
        Assert.assertEquals("07df6997525697b211367cf7f359e5232ec65375", commonsLang.get().getSha1());
        Assert.assertEquals("2.5", commonsLang.get().getVersion());
        Assert.assertEquals("The Apache Software Foundation", commonsLang.get().getOrganization());

        Assert.assertEquals("net.sf.ehcache:ehcache:1.6.2", ehcache.get().getMavenIdentifier());
        Assert.assertEquals("a89c93852c8e1da630bfe9ac292abbc346c242dc", ehcache.get().getSha1());
        Assert.assertEquals("1.6.2", ehcache.get().getVersion());
        Assert.assertNull(ehcache.get().getOrganization());

        Assert.assertEquals("org.hibernate:hibernate-ehcache:3.6.9.Final", hibernateEhcache.get().getMavenIdentifier());
        Assert.assertEquals("ef67aaff9ded441f83ea49c6ae1e3d245d9cacae", hibernateEhcache.get().getSha1());
        Assert.assertEquals("3.6.9.Final", hibernateEhcache.get().getVersion());
        Assert.assertEquals("Hibernate.org", hibernateEhcache.get().getOrganization());

        // Verify found paths
        boolean jeeExampleMatchFoundPaths = Arrays.stream(appDependenciesDtoList)
                .flatMap(dto -> dto.getDependencies().stream())
                .filter(dto -> dto.getName().equals("jee-example-services.jar"))
                .flatMap(dto -> dto.getFoundPaths().stream())
                .collect(Collectors.toList())
                .containsAll(FOUND_PATHS_JEE_EXAMPLE_SERVICES);
        boolean commonsLangMatchFoundPaths = Arrays.stream(appDependenciesDtoList)
                .flatMap(dto -> dto.getDependencies().stream())
                .filter(dto -> dto.getName().equals("commons-lang-2.5.jar"))
                .flatMap(dto -> dto.getFoundPaths().stream())
                .collect(Collectors.toList())
                .containsAll(FOUND_PATHS_COMMONS_LANG);
        boolean ehcacheMatchFoundPaths = Arrays.stream(appDependenciesDtoList)
                .flatMap(dto -> dto.getDependencies().stream())
                .filter(dto -> dto.getName().equals("ehcache-1.6.2.jar"))
                .flatMap(dto -> dto.getFoundPaths().stream())
                .collect(Collectors.toList())
                .containsAll(FOUND_PATHS_EHCACHE);
        boolean hibernateEhcacheMatchFoundPaths = Arrays.stream(appDependenciesDtoList)
                .flatMap(dto -> dto.getDependencies().stream())
                .filter(dto -> dto.getName().equals("hibernate-ehcache-3.6.9.Final.jar"))
                .flatMap(dto -> dto.getFoundPaths().stream())
                .collect(Collectors.toList())
                .containsAll(FOUND_PATHS_HIBERNATE_EHCACHE);

        Assert.assertTrue(jeeExampleMatchFoundPaths);
        Assert.assertTrue(commonsLangMatchFoundPaths);
        Assert.assertTrue(ehcacheMatchFoundPaths);
        Assert.assertTrue(hibernateEhcacheMatchFoundPaths);
    }

    private void validateApplicationDetails(GraphContext context) throws IOException {
        File applicationsJson = new ReportService(context).getWindupUIApiDirectory()
                .resolve(ApplicationsRuleProvider.PATH + ".json")
                .toFile();
        File appDetailsJson = new ReportService(context).getWindupUIApiDirectory()
                .resolve(ApplicationDetailsRuleProvider.PATH + ".json")
                .toFile();

        // Application
        ApplicationDto[] appDtoList = new ObjectMapper().readValue(applicationsJson, ApplicationDto[].class);
        Optional<ApplicationDto> mainApp = Arrays.stream(appDtoList)
                .filter(dto -> dto.getName().equals(MAIN_APP_FILENAME))
                .findFirst();

        Assert.assertTrue(mainApp.isPresent());

        // Application details
        ApplicationDetailsDto[] appDetailsDtoList = new ObjectMapper().readValue(appDetailsJson, ApplicationDetailsDto[].class);
        Optional<ApplicationDetailsDto> mainAppDetails = Arrays.stream(appDetailsDtoList)
                .filter(dto -> dto.getApplicationId().equals(mainApp.get().getId()))
                .findFirst();

        Assert.assertTrue(mainAppDetails.isPresent());

        // Message in app details
        boolean log4jReferenceFound = mainAppDetails.get().getMessages().stream().anyMatch(messageDto -> {
            return messageDto.getValue().equals("log4j reference found");
        });

        Assert.assertTrue(log4jReferenceFound);
    }

    private static final List<String> FOUND_PATHS_JEE_EXAMPLE_SERVICES = Arrays.asList(
            "duplicate-ear-test-1.ear/jee-example-services.jar",
            "duplicate-ear-test-2.ear/jee-example-services.jar",
            "duplicate-ear-test-3.ear/jee-example-services.jar"
    );

    private static final List<String> FOUND_PATHS_COMMONS_LANG = Arrays.asList(
            "duplicate-ear-test-1.ear/jee-example-web.war/WEB-INF/lib/commons-lang-2.5.jar",
            "duplicate-ear-test-2.ear/jee-example-web.war/WEB-INF/lib/commons-lang-2.5.jar",
            "duplicate-ear-test-3.ear/jee-example-web.war/WEB-INF/lib/commons-lang-2.5.jar"
    );

    private static final List<String> FOUND_PATHS_EHCACHE = Arrays.asList(
            "duplicate-ear-test-1.ear/lib/ehcache-1.6.2.jar",
            "duplicate-ear-test-2.ear/lib/ehcache-1.6.2.jar",
            "duplicate-ear-test-3.ear/lib/ehcache-1.6.2.jar"
    );

    private static final List<String> FOUND_PATHS_HIBERNATE_EHCACHE = Arrays.asList(
            "duplicate-ear-test-1.ear/lib/hibernate-ehcache-3.6.9.Final.jar",
            "duplicate-ear-test-2.ear/lib/hibernate-ehcache-3.6.9.Final.jar",
            "duplicate-ear-test-3.ear/lib/hibernate-ehcache-3.6.9.Final.jar"
    );
}
