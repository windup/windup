package org.jboss.windup.tests.application.newreports;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.KeepWorkDirsOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(Arquillian.class)
public class WindupArchitectureTestKeepArchivesAndGraphTest extends WindupArchitectureTest {

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
    public void testRunWindupDiscard() throws Exception {
        final String path = "../test-files/Windup1x-javaee-example.war";
        final Path outputPath = getDefaultPath();

        try (GraphContext context = createGraphContext(outputPath)) {
            super.runTest(context, false, path, false, Collections.singletonList("filter.out.everything"));
        }
        // check if archives are still there
        Path archivesPath = outputPath.resolve("archives");
        boolean archivesStillThere = Files.exists(archivesPath);
        Assert.assertFalse("Archives should not be present at: " + archivesPath, archivesStillThere);
    }

    @Test
    public void testRunWindupKeep() throws Exception {
        final String path = "../test-files/Windup1x-javaee-example.war";
        final Path outputPath = getDefaultPath();

        try (GraphContext context = createGraphContext(outputPath)) {
            Map<String, Object> keepOption = new HashMap<>();
            keepOption.put(KeepWorkDirsOption.NAME, true);
            super.runTest(context, false, Collections.singletonList(path), null, false, Collections.singletonList("filter.out.everything"),
                    Collections.singletonList("filter.out.everything"), keepOption);
        }
        // check if archives are still there
        Path archivesPath = outputPath.resolve("archives");
        boolean archivesStillThere = Files.exists(archivesPath);
        Assert.assertTrue("Archives should be present at: " + archivesPath, archivesStillThere);
    }
}
