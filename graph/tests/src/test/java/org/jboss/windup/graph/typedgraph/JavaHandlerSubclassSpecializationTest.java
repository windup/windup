package org.jboss.windup.graph.typedgraph;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Covers: https://issues.jboss.org/browse/WINDUP-168
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class JavaHandlerSubclassSpecializationTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClasses(TestFooModel.class, TestFooSubModel.class);
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testSubclassMethodHandling() throws Exception {
        try (GraphContext context = factory.create(true)) {
            Assert.assertNotNull(context);

            TestFooModel model = context.getFramed().addFramedVertex(TestFooModel.class);
            TestFooSubModel subModel = GraphService.addTypeToModel(context, model, TestFooSubModel.class);
            TestFooModel asParent = subModel;

            TestFooModel reframed = (TestFooModel) context.getFramed().frameElement(model.getElement(), WindupVertexFrame.class);

            String s1 = model.testJavaMethod();
            String s2 = subModel.testJavaMethod();
            String s3 = asParent.testJavaMethod();
            String s4 = reframed.testJavaMethod();

            Assert.assertEquals("base", s1);
            Assert.assertEquals("subclass", s2);
            Assert.assertEquals("subclass", s3);
            Assert.assertEquals("subclass", s4);
        }
    }
}
