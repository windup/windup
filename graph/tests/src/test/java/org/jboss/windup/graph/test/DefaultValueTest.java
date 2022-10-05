package org.jboss.windup.graph.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.typedgraph.DefaultValueTestModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class DefaultValueTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClasses(DefaultValueTestModel.class);
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testDefaultValue() throws Exception {
        try (GraphContext context = factory.create(true)) {
            Assert.assertNotNull(context);
            DefaultValueTestModel initialModelType = context.getFramed().addFramedVertex(DefaultValueTestModel.class);
            Assert.assertFalse(initialModelType.getDefaultFalseValue());
            Assert.assertTrue(initialModelType.getDefaultTrueValue());
        }
    }
}
