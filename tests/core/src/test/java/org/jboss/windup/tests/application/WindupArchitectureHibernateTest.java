package org.jboss.windup.tests.application;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateMappingFileModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.HibernateMappingFileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureHibernateTest extends WindupArchitectureTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java-ee"),
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
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java-ee"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext graphContext;

    @Test
    public void testRunWindupHibernate() throws Exception
    {
        final String path = "../../test-files/hibernate-tutorial-web-3.3.2.GA.war";
        List<String> includeList = Collections.singletonList("nocodescanning");
        List<String> excludeList = Collections.emptyList();
        super.runTest(processor, graphContext, path, false, includeList, excludeList);
        validateHibernateFiles();
    }

    private void validateHibernateFiles()
    {
        HibernateConfigurationFileService cfgService = new HibernateConfigurationFileService(graphContext);

        int hibernateCfgFilesFound = 0;
        for (HibernateConfigurationFileModel model : cfgService.findAll())
        {
            Assert.assertEquals("3.0", model.getSpecificationVersion());
            hibernateCfgFilesFound++;
        }
        Assert.assertEquals(1, hibernateCfgFilesFound);

        HibernateMappingFileService mappingService = new HibernateMappingFileService(graphContext);
        boolean personHbmFound = false;
        boolean eventHbmFound = false;
        boolean itemHbmFound = false;
        int numberModelsFound = 0;
        Iterable<HibernateMappingFileModel> allMappingModels = mappingService.findAll();
        for (HibernateMappingFileModel model : allMappingModels)
        {
            numberModelsFound++;
            Assert.assertEquals("3.0", model.getSpecificationVersion());
            if (model.getFileName().equals("Person.hbm.xml"))
            {
                personHbmFound = true;
                Iterator<HibernateEntityModel> entities = model.getHibernateEntities().iterator();
                Assert.assertTrue(entities.hasNext());

                HibernateEntityModel entity = entities.next();
                Assert.assertEquals("3.0", entity.getSpecificationVersion());
                Assert.assertEquals("PERSON", entity.getTableName());
                Assert.assertEquals("org.hibernate.tutorial.domain.Person", entity.getJavaClass().getQualifiedName());

                Assert.assertFalse(entities.hasNext());
            }
            else if (model.getFileName().equals("Event.hbm.xml"))
            {
                eventHbmFound = true;

                Iterator<HibernateEntityModel> entities = model.getHibernateEntities().iterator();
                Assert.assertTrue(entities.hasNext());

                HibernateEntityModel entity = entities.next();
                Assert.assertEquals("3.0", entity.getSpecificationVersion());
                Assert.assertEquals("EVENTS", entity.getTableName());
                Assert.assertEquals("org.hibernate.tutorial.domain.Event", entity.getJavaClass().getQualifiedName());

                Assert.assertFalse(entities.hasNext());
            }
            else if (model.getFileName().equals("Item.hbm.xml"))
            {
                itemHbmFound = true;

                Iterator<HibernateEntityModel> entities = model.getHibernateEntities().iterator();
                Assert.assertTrue(entities.hasNext());

                HibernateEntityModel entity = entities.next();
                Assert.assertEquals("3.0", entity.getSpecificationVersion());
                Assert.assertEquals("Items", entity.getTableName());
                Assert.assertEquals("org.hibernate.test.cache.Item", entity.getJavaClass().getQualifiedName());

                Assert.assertFalse(entities.hasNext());
            }
        }
        Assert.assertTrue(personHbmFound);
        Assert.assertTrue(eventHbmFound);
        Assert.assertTrue(itemHbmFound);
        Assert.assertEquals(3, numberModelsFound);
    }
}
