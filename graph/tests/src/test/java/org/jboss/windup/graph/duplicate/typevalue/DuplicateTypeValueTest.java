package org.jboss.windup.graph.duplicate.typevalue;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DuplicateTypeValueTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClasses(TestSimpleModel.class, TestSimple2Model.class);
    }

    @Inject
    private GraphContextFactory factory;

    @Test(expected = Exception.class)
    public void testDuplicateTypeValue() throws Exception {
        try (GraphContext context = factory.create(true)) {
            Assert.assertNotNull(context);
            context.getFramed().addFramedVertex(TestSimpleModel.class);
        }
    }
}
