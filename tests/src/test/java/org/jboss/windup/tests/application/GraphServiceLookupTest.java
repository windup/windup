package org.jboss.windup.tests.application;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.tests.application.model.TestSampleModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class GraphServiceLookupTest
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
                    .addClass(TestSampleModel.class)
                    .addBeansXML()
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

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testServiceLookup() throws Exception
    {
        try (GraphContext graphContext = factory.create())
        {
            Assert.assertNotNull(graphContext);

            FileService fileModelService = new FileService(graphContext);
            Assert.assertNotNull(fileModelService);
            FileModel fileModel = fileModelService.create();
            Assert.assertNotNull(fileModel);
            Assert.assertTrue(fileModel instanceof FileModel);

            Service<TestSampleModel> sampleModelService = new GraphService<>(graphContext, TestSampleModel.class);
            Assert.assertNotNull(sampleModelService);
            TestSampleModel sampleModel = sampleModelService.create();
            Assert.assertNotNull(sampleModel);
            Assert.assertTrue(sampleModel instanceof TestSampleModel);
        }
    }
}
