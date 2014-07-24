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
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@RunWith(Arquillian.class)
public class GraphTypeManagerTest
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
    public void testGraphTypeHandling() throws Exception
    {
        Assert.assertNotNull(context);

        TestFooModel initialModelType = context.getFramed().addVertex(null, TestFooModel.class);

        try
        {
            GraphService.addTypeToModel(context, initialModelType, TestFooSubModel.class);

            Iterable<Vertex> vertices = context.getFramed().query().has(WindupVertexFrame.TYPE_FIELD, Text.CONTAINS,
                        TestFooModel.class.getAnnotation(TypeValue.class).value()).vertices();

            int numberFound = 0;
            for (Vertex v : vertices)
            {
                numberFound++;
                WindupVertexFrame framed = context.getFramed().frame(v, WindupVertexFrame.class);

                Assert.assertTrue(framed instanceof TestFooModel);
                Assert.assertTrue(framed instanceof TestFooSubModel);
            }
            Assert.assertEquals(1, numberFound);
        }
        finally
        {
            context.getGraph().removeVertex(initialModelType.asVertex());
        }
    }

    @Test
    public void testGraphSearchWithoutCommit() throws Exception
    {
        Assert.assertNotNull(context);

        TestFooModel foo1 = context.getFramed().addVertex(null, TestFooModel.class);
        TestFooModel foo2 = context.getFramed().addVertex(null, TestFooModel.class);
        TestFooModel foo3 = context.getFramed().addVertex(null, TestFooModel.class);
        TestFooModel foo4 = context.getFramed().addVertex(null, TestFooModel.class);

        try
        {
            GraphService.addTypeToModel(context, foo1, TestFooSubModel.class);
            GraphService.addTypeToModel(context, foo2, TestFooSubModel.class);

            Iterable<Vertex> vertices = context.getFramed().query()
                        .has("type", Text.CONTAINS, TestFooSubModel.class.getAnnotation(TypeValue.class).value())
                        .vertices();

            int numberFound = 0;
            for (Vertex v : vertices)
            {
                numberFound++;
                WindupVertexFrame framed = context.getFramed().frame(v, WindupVertexFrame.class);

                Assert.assertTrue(framed instanceof TestFooModel);
            }
            Assert.assertEquals(2, numberFound);
        }
        finally
        {
            context.getGraph().removeVertex(foo1.asVertex());
            context.getGraph().removeVertex(foo2.asVertex());
            context.getGraph().removeVertex(foo3.asVertex());
            context.getGraph().removeVertex(foo4.asVertex());
        }
    }
}
