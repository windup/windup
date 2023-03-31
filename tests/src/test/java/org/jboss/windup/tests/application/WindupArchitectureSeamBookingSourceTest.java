package org.jboss.windup.tests.application;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(Arquillian.class)
public class WindupArchitectureSeamBookingSourceTest extends WindupArchitectureTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class);
    }

    @Test
    public void testRunWindupSeamBookingSourceMode() throws Exception {
        try (GraphContext context = createGraphContext()) {
            // The test-files folder in the project root dir.
            super.runTest(context, true, "../test-files/seam-booking-5.2", true);

            TechnologyTagService technologyTagService = new TechnologyTagService(context);
            List<TechnologyTagModel> technologyTagModels = technologyTagService.findAll();
            Assert.assertEquals("size", 7, technologyTagModels.size());
            AtomicBoolean foundEjbJar = new AtomicBoolean(false);
            AtomicBoolean foundJpaJar = new AtomicBoolean(false);
            AtomicBoolean foundWebXMLJar = new AtomicBoolean(false);
            AtomicBoolean foundProperties = new AtomicBoolean(false);
            AtomicBoolean foundJBossEJBXML = new AtomicBoolean(false);
            AtomicBoolean foundJavaSource = new AtomicBoolean(false);
            AtomicBoolean foundMavenXML = new AtomicBoolean(false);
            technologyTagModels.forEach(technologyTagModel -> {
                if ("EJB XML".equals(technologyTagModel.getName()) && "3.0".equals(technologyTagModel.getVersion())) foundEjbJar.set(true);
                if ("JPA XML".equals(technologyTagModel.getName()) && "1.0".equals(technologyTagModel.getVersion())) foundJpaJar.set(true);
                if ("Web XML".equals(technologyTagModel.getName()) && "2.5".equals(technologyTagModel.getVersion())) foundWebXMLJar.set(true);
                if ("Properties".equals(technologyTagModel.getName()) && technologyTagModel.getVersion() == null) foundProperties.set(true);
                if ("JBoss EJB XML".equals(technologyTagModel.getName()) && technologyTagModel.getVersion() == null) foundJBossEJBXML.set(true);
                if ("Java Source".equals(technologyTagModel.getName()) && technologyTagModel.getVersion() == null) foundJavaSource.set(true);
                if ("Maven XML".equals(technologyTagModel.getName()) && technologyTagModel.getVersion() == null) foundMavenXML.set(true);
            });
            Assert.assertTrue("Not found EJB XML tag", foundEjbJar.get());
            Assert.assertTrue("Not found JPA XML tag", foundJpaJar.get());
            Assert.assertTrue("Not found Web XML tag", foundJpaJar.get());
            Assert.assertTrue("Not found Properties tag", foundProperties.get());
            Assert.assertTrue("Not found JBoss EJB XML tag", foundJBossEJBXML.get());
            Assert.assertTrue("Not found Java Source tag", foundJavaSource.get());
            Assert.assertTrue("Not found Maven XML tag", foundProperties.get());
        }
    }

}
