package org.jboss.windup.rules.apps.java.service;

import java.nio.file.Path;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.rules.apps.java.model.LineMappingModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LineMappingServiceTest {

    @AddonDependencies({ @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi") })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testLineMappingService() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            LineMappingService service = new LineMappingService(context);

            LineMappingModel model = service.create(new int[] { 1, 2, 3, 4 });

            Map<Integer, Integer> map = service.getMapping(model);

            Assert.assertEquals(2, (int) map.get(1));
            Assert.assertEquals(4, (int) map.get(3));
        }
    }
}