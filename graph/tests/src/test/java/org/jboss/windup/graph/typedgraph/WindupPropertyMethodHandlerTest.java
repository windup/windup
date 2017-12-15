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

import org.apache.tinkerpop.gremlin.structure.Vertex;

@RunWith(Arquillian.class)
public class WindupPropertyMethodHandlerTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClasses(TestFooModel.class, TestFooSubModel.class);
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testInMemoryFrame() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            Assert.assertNotNull(context);

            GraphService<TestFooModel> fooModelService = new GraphService<>(context, TestFooModel.class);

            TestFooModel inMemoryModel = fooModelService.create();
            inMemoryModel.setProp1("prop1").setProp2("prop2").setProp3("prop3");

            Iterable<Vertex> vertices = context.getQuery().type(TestFooModel.class).vertices();

            int numberFound = 0;
            for (Vertex v : vertices)
            {
                numberFound++;
                TestFooModel framed = (TestFooModel) context.getFramed().frame(v, WindupVertexFrame.class);

                Assert.assertTrue(framed instanceof TestFooModel);
                Assert.assertEquals("prop1", framed.getProp1());
                Assert.assertEquals("prop2", framed.getProp2());
                Assert.assertEquals("prop3", framed.getProp3());
            }
            Assert.assertEquals(1, numberFound);
        }
    }
}
