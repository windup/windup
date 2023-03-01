package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.data.dto.ApplicationDependenciesDto;
import org.jboss.windup.reporting.data.rules.DependenciesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RunWith(Arquillian.class)
public class NewReports_WindupArchitectureDependencyTest extends WindupArchitectureTest {

    private static final String[] FOUND_PATH_LIB = {
            "application-with-dependencies.ear/lib/example-0-1.0.0.jar"
    };

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
            @AddonDependency(name = "org.jboss.windup.tests:test-util")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Test
    public void testRunWindupDependencies() throws Exception {
        try (GraphContext context = createGraphContext()) {
            List<String> packages = Collections.emptyList();
            super.runTest(context, false, "../test-files/application-with-dependencies.ear", false, packages);
            validateDependenciesReport(context);
        }
    }

    private void validateDependenciesReport(GraphContext context) throws IOException {
        File dependenciesJson = new ReportService(context).getApiDataDirectory()
                .resolve(DependenciesRuleProvider.PATH + ".json")
                .toFile();

        ApplicationDependenciesDto[] appDependenciesDtoList = new ObjectMapper().readValue(dependenciesJson, ApplicationDependenciesDto[].class);
        Assert.assertEquals(1, appDependenciesDtoList.length);

        Optional<ApplicationDependenciesDto.DependencyDto> dependencyDto = appDependenciesDtoList[0].getDependencies().stream()
                .filter(dto -> dto.getName().equals("example-0-1.0.0.jar"))
                .findFirst();

        Assert.assertTrue(dependencyDto.isPresent());
        Assert.assertEquals("example-0:test:1.0.0", dependencyDto.get().getMavenIdentifier());
        Assert.assertEquals("9e9944d81b31d376643f100775aba3d0b83210ef", dependencyDto.get().getSha1());
        Assert.assertEquals("1.0.0", dependencyDto.get().getVersion());
        Assert.assertNull(dependencyDto.get().getOrganization());
        Assert.assertTrue(dependencyDto.get().getFoundPaths().containsAll(Arrays.asList(FOUND_PATH_LIB)));
    }

}
