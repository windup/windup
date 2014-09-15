package org.jboss.windup.tests.application;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureMediumBinaryModeTest extends WindupArchitectureTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.ext:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(WindupArchitectureTest.class)
                    .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Test
    public void testRunWindupMedium() throws Exception
    {
        final String path = "../test-files/Windup1x-javaee-example.war";

        try (GraphContext context = getFactory().create())
        {
            super.runTest(context, path, false);
            validateManifestEntries(context);
        }
    }

    private void validateManifestEntries(GraphContext context) throws Exception
    {
        JarManifestService jarManifestService = new JarManifestService(context);
        Iterable<JarManifestModel> manifests = jarManifestService.findAll();

        int numberFound = 0;
        boolean warManifestFound = false;
        for (JarManifestModel manifest : manifests)
        {
            if (manifest.getArchive().getFileName().equals("Windup1x-javaee-example.war"))
            {
                Assert.assertEquals("1.0", manifest.asVertex().getProperty("Manifest-Version"));
                Assert.assertEquals("Plexus Archiver", manifest.asVertex().getProperty("Archiver-Version"));
                Assert.assertEquals("Apache Maven", manifest.asVertex().getProperty("Created-By"));
                warManifestFound = true;
            }

            numberFound++;
        }
        Assert.assertEquals(9, numberFound);
        Assert.assertTrue(warManifestFound);
    }
}
