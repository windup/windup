package org.jboss.windup.tests.application.newreports;

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
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.jboss.windup.util.ZipUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RunWith(Arquillian.class)
public class NewReports_WindupArchitectureExplodedAppTest extends WindupArchitectureTest {

    final TemporaryFolder tmp = new TemporaryFolder();
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
        tmp.create();
        final File explodedAppDir = tmp.newFolder(EXPLODED_APP_DIR);
        ZipUtil.unzipToFolder(new File("../test-files/spring-small-example.war"), explodedAppDir);

        final Path outputPath = getDefaultPath();
        try (GraphContext context = createGraphContext(outputPath)) {
            Map<String, Object> explodedAppOption = new HashMap<>();
            explodedAppOption.put(ExplodedAppInputOption.NAME, true);
            super.runTest(context, false, Collections.singletonList(explodedAppDir.toString()), null, false, Collections.emptyList(),
                    Collections.emptyList(), explodedAppOption);
            validateJarDependencyGraphReport(context);
        } finally {
            tmp.delete();
        }
    }

    private void validateJarDependencyGraphReport(GraphContext context) throws IOException {
        File dependenciesJson = new ReportService(context).getWindupUIApiDirectory()
                .resolve(DependenciesRuleProvider.PATH + ".json")
                .toFile();

        ApplicationDependenciesDto[] appDependenciesDtoList = new ObjectMapper().readValue(dependenciesJson, ApplicationDependenciesDto[].class);
        Assert.assertEquals(1, appDependenciesDtoList.length);

        // Verify total number of dependencies
        Assert.assertEquals(15, appDependenciesDtoList[0].getDependencies().size());

        // Verify commons-logging-1.1.1.jar
        Optional<ApplicationDependenciesDto.DependencyDto> dependencyDto = appDependenciesDtoList[0].getDependencies().stream()
                .filter(dto -> dto.getName().equals("commons-logging-1.1.1.jar"))
                .findFirst();
        Assert.assertTrue(dependencyDto.isPresent());

        // Verify standard-1.1.2.jar
        dependencyDto = appDependenciesDtoList[0].getDependencies().stream()
                .filter(dto -> dto.getName().equals("standard-1.1.2.jar"))
                .findFirst();
        Assert.assertTrue(dependencyDto.isPresent());
    }
}
