package org.jboss.windup.tests.application;

import java.io.File;
import java.util.Iterator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.PropertiesModel;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;
import org.jboss.windup.rules.apps.javaee.service.WebXmlService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureSourceModeTest extends WindupArchitectureTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.utils:utils"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
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
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java-ee"),
                                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Test
    public void testRunWindupSourceMode() throws Exception
    {
        try (GraphContext context = createGraphContext())
        {
            // The test-files folder in the project root dir.
            super.runTest(context, "../test-files/src_example", true);

            validateWebXmlReferences(context);
            validatePropertiesModels(context);
        }
    }

    /**
     * Validate that a web.xml file was found, and that the metadata was extracted correctly
     */
    private void validateWebXmlReferences(GraphContext context)
    {
        WebXmlService webXmlService = new WebXmlService(context);
        Iterator<WebXmlModel> models = webXmlService.findAll().iterator();

        // There should be at least one file
        Assert.assertTrue(models.hasNext());
        WebXmlModel model = models.next();

        // and only one file
        Assert.assertFalse(models.hasNext());

        Assert.assertEquals("Sample Display Name", model.getDisplayName());

        int numberFound = 0;
        for (EnvironmentReferenceModel envRefModel : model.getEnvironmentReferences())
        {
            Assert.assertEquals("jdbc/myJdbc", envRefModel.getName());
            Assert.assertEquals("javax.sql.DataSource", envRefModel.getReferenceType());
            numberFound++;
        }

        // there is only one env-ref
        Assert.assertEquals(1, numberFound);
    }

    /**
     * Validate that the expected Properties Models were found
     */
    private void validatePropertiesModels(GraphContext context)
    {
        GraphService<PropertiesModel> service = new GraphService<>(context, PropertiesModel.class);

        int numberFound = 0;
        for (PropertiesModel model : service.findAll())
        {
            numberFound++;

            Assert.assertEquals("value1", model.getProperty("example1"));
            Assert.assertEquals("anothervalue", model.getProperty("anotherproperty"));
            Assert.assertEquals("1234", model.getProperty("timetaken"));
        }

        Assert.assertEquals(1, numberFound);
    }
}
