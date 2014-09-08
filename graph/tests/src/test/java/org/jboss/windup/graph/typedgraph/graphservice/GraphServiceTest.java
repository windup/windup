package org.jboss.windup.graph.typedgraph.graphservice;

import java.util.Iterator;

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
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.graph.typedgraph.TestFooModel;
import org.jboss.windup.graph.typedgraph.TestFooSubModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Cmp;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;


@RunWith(Arquillian.class)
public class GraphServiceTest
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

    @After
    public void tearDown()
    {
        context.disconnectFromGraph();
    }


    @Test
    public void testGraphTypeHandling() throws Exception
    {
        Assert.assertNotNull(context);
        context.init(null);

        TestFooModel initialModelType = context.getFramed().addVertex(null, TestFooModel.class);

        try
        {
            GraphService.addTypeToModel(context, initialModelType, TestFooSubModel.class);

            Iterable<Vertex> vertices = context.getQuery().type(TestFooModel.class).vertices();

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
        context.init(null);

        TestFooModel foo1 = context.getFramed().addVertex(null, TestFooModel.class);
        TestFooModel foo2 = context.getFramed().addVertex(null, TestFooModel.class);
        TestFooModel foo3 = context.getFramed().addVertex(null, TestFooModel.class);
        TestFooModel foo4 = context.getFramed().addVertex(null, TestFooModel.class);

        try
        {
            GraphService.addTypeToModel(context, foo1, TestFooSubModel.class);
            GraphService.addTypeToModel(context, foo2, TestFooSubModel.class);

            Iterable<Vertex> vertices = context.getQuery().type(TestFooSubModel.class).vertices();

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

    @Test
    public void testModelCreation()
    {
        Assert.assertNotNull(context);
        context.init(null);

        Service<TestFooSubModel> graphService = context.getService(TestFooSubModel.class);

        // test there is no vertex of such type
        Iterable<TestFooSubModel> foundAll = graphService.findAll();
        Assert.assertFalse(foundAll.iterator().hasNext());

        TestFooSubModel model = graphService.create();
        model.setFoo("myFoo");

        // test findAll
        FramedGraphQuery query = context.getFramed().query();
        query.has(WindupVertexFrame.TYPE_PROP, Cmp.EQUAL, "Foo");
        Iterable<TestFooSubModel> verticesFoundByContext = query.vertices(TestFooSubModel.class);
        Iterator<TestFooSubModel> iterator = verticesFoundByContext.iterator();
        Assert.assertTrue(iterator.hasNext());
        TestFooSubModel model2 = iterator.next();
        Assert.assertEquals("myFoo", model2.getFoo());
        Assert.assertFalse(iterator.hasNext());

        Iterable<TestFooSubModel> verticesFoundByGraphService = graphService.findAll();
        iterator = verticesFoundByGraphService.iterator();
        Assert.assertTrue(iterator.hasNext());
        model2 = iterator.next();
        Assert.assertEquals("myFoo", model2.getFoo());
        Assert.assertFalse(iterator.hasNext());

        model2 = graphService.getUnique();
        Assert.assertEquals("myFoo", model2.getFoo());

        model2 = graphService.getUniqueByProperty("fooProperty", "myFoo");
        Assert.assertNotNull(model2);
        context.getFramed().removeVertex(model.asVertex());
    }
}
