package org.jboss.windup.tests.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.configuration.options.ExplodedAppInputOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.data.dto.ApplicationDependenciesDto;
import org.jboss.windup.reporting.data.rules.DependenciesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.testutil.html.TestDependencyGraphReportUtil;
import org.jboss.windup.util.ZipUtil;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:marcorizzi82@gmail.com">Marco Rizzi</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Arquillian.class)
public class WindupArchitectureExplodedAppTest extends WindupArchitectureTest {

    private static final String EXPLODED_APP_DIR = "exploded-app-directory";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-tattletale"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
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
    public void testRunWindupExplodedApp() throws Exception {
        final TemporaryFolder tmp = new TemporaryFolder();
        tmp.create();
        final File explodedAppDir = tmp.newFolder(EXPLODED_APP_DIR);
        ZipUtil.unzipToFolder(new File("../test-files/spring-small-example.war"), explodedAppDir);

        final Path outputPath = getDefaultPath();
        try (GraphContext context = createGraphContext(outputPath)) {
            Map<String, Object> explodedAppOption = new HashMap<>();
            explodedAppOption.put(ExplodedAppInputOption.NAME, true);
            super.runTest(context, true, Collections.singletonList(explodedAppDir.toString()), null, false, Collections.emptyList(),
                    Collections.emptyList(), explodedAppOption);
            validateJarDependencyGraphReport(context);
        } finally {
            tmp.delete();
        }
    }

    private void validateJarDependencyGraphReport(GraphContext graphContext) {
        Path dependencyReport = getApplicationDependencyGraphReportPath(graphContext);
        Assert.assertNotNull(dependencyReport);
        TestDependencyGraphReportUtil dependencyGraphReportUtil = new TestDependencyGraphReportUtil();
        dependencyGraphReportUtil.loadPage(dependencyReport);
        Assert.assertEquals(21, dependencyGraphReportUtil.getNumberOfArchivesInTheGraph());
        Assert.assertEquals(1, dependencyGraphReportUtil.getNumberOfArchivesInTheGraphByName(EXPLODED_APP_DIR));
        Assert.assertEquals(1, dependencyGraphReportUtil.getNumberOfArchivesInTheGraphByName("commons-logging-1.1.1.jar"));
        Assert.assertEquals(1, dependencyGraphReportUtil.getNumberOfArchivesInTheGraphByName("standard-1.1.2.jar"));
        Assert.assertEquals(15, dependencyGraphReportUtil.getNumberOfRelationsInTheGraph());

    }

    @Test
    public void testRunWindupExplodedApp_newReports() throws Exception {
        final TemporaryFolder tmp = new TemporaryFolder();
        tmp.create();
        final File explodedAppDir = tmp.newFolder(EXPLODED_APP_DIR);
        ZipUtil.unzipToFolder(new File("../test-files/spring-small-example.war"), explodedAppDir);

        final Path outputPath = getDefaultPath();
        try (GraphContext context = createGraphContext(outputPath)) {
            Map<String, Object> explodedAppOption = new HashMap<>();
            explodedAppOption.put(ExplodedAppInputOption.NAME, true);
            super.runTest(context, false, Collections.singletonList(explodedAppDir.toString()), null, false, Collections.emptyList(),
                    Collections.emptyList(), explodedAppOption);

            File dependenciesJson = new ReportService(context).getApiDataDirectory()
                    .resolve(DependenciesRuleProvider.PATH + ".json")
                    .toFile();

            ApplicationDependenciesDto[] appDependenciesDtoList = new ObjectMapper().readValue(dependenciesJson, ApplicationDependenciesDto[].class);
            Assert.assertEquals(1, appDependenciesDtoList.length);
            Assert.assertEquals(15, appDependenciesDtoList[0].dependencies.size());

            Optional<ApplicationDependenciesDto.DependencyDto> dependencyDto = appDependenciesDtoList[0].dependencies.stream()
                    .filter(dto -> dto.name.equals("commons-logging-1.1.1.jar"))
                    .findFirst();
            Assert.assertTrue(dependencyDto.isPresent());

            dependencyDto = appDependenciesDtoList[0].dependencies.stream()
                    .filter(dto -> dto.name.equals("standard-1.1.2.jar"))
                    .findFirst();
            Assert.assertTrue(dependencyDto.isPresent());
        } finally {
            tmp.delete();
        }
    }
}
