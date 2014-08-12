package org.jboss.windup.graph.typedgraph;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.InMemoryVertexFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@RunWith(Arquillian.class)
public class InMemoryFrameTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClasses(TestFooModel.class, TestFooSubModel.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Test
    public void testInMemoryFrame() throws Exception
    {
        Assert.assertNotNull(context);

        GraphService<TestFooModel> fooModelService = context.getService(TestFooModel.class);

        TestFooModel inMemoryModel = fooModelService.createInMemory();
        inMemoryModel.setProp1("prop1");
        inMemoryModel.setProp2("prop2");
        inMemoryModel.setProp3("prop3");

        Iterable<Vertex> vertices = context
                    .getFramed()
                    .query()
                    .has(WindupVertexFrame.TYPE_PROP, 
                                TestFooModel.class.getAnnotation(TypeValue.class).value())
                    .vertices();

        // we should have zero results, as this was only created in memory
        Assert.assertFalse(vertices.iterator().hasNext());

        InMemoryVertexFrame inMemoryFrame = (InMemoryVertexFrame) inMemoryModel;
        inMemoryFrame.attachToGraph();

        vertices = context
                    .getFramed()
                    .query()
                    .has(WindupVertexFrame.TYPE_PROP, 
                                TestFooModel.class.getAnnotation(TypeValue.class).value())
                    .vertices();

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
