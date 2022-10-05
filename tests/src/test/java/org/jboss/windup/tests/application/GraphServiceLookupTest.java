package org.jboss.windup.tests.application;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
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
public class GraphServiceLookupTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addClass(TestSampleModel.class)
                .addBeansXML();
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testServiceLookup() throws Exception {
        try (GraphContext graphContext = factory.create(true)) {
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
