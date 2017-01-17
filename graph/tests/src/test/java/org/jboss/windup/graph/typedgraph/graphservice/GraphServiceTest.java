package org.jboss.windup.graph.typedgraph.graphservice;

import com.thinkaurelius.titan.core.TitanGraph;
import java.util.Iterator;

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
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.graph.typedgraph.TestFooModel;
import org.jboss.windup.graph.typedgraph.TestFooSubModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Cmp;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.List;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.typedgraph.TestIncidenceAaaModel;
import org.jboss.windup.graph.typedgraph.TestIncidenceAaaToBbbEdgeModel;
import org.jboss.windup.graph.typedgraph.TestIncidenceBbbModel;
import org.junit.Assume;
import org.junit.Ignore;

@RunWith(Arquillian.class)
public class GraphServiceTest
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
                    .addClasses(TestFooModel.class, TestFooSubModel.class, TestIncidenceAaaModel.class, TestIncidenceBbbModel.class, TestIncidenceAaaToBbbEdgeModel.class);
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testGraphTypeHandling() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            Assert.assertNotNull(context);
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
    }

    @Test
    public void testGraphSearchWithoutCommit() throws Exception
    {
        try (GraphContext context = factory.create())
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
    }

    @Test
    public void testModelCreation() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            Assert.assertNotNull(context);

            Service<TestFooSubModel> graphService = new GraphService<>(context, TestFooSubModel.class);

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


    @Test
    public void testEdgeFrames() throws Exception
    {
        try (GraphContext graphContext = factory.create())
        {
            // Connect two vertexes with an edge,
            TestIncidenceAaaModel aaa = graphContext.create(TestIncidenceAaaModel.class);
            Assume.assumeNotNull(aaa);
            aaa.setProp1("a1");
            TestIncidenceBbbModel bbb = graphContext.create(TestIncidenceBbbModel.class);
            Assume.assumeNotNull(bbb);
            bbb.setProp1("b1");

            Assume.assumeNotNull(graphContext.getFramed());

            // This would put a String into "w:winduptype", we need at least List<String>.
            //TestIncidenceAaaToBbbEdgeModel edgeModel = graphContext.getFramed().addEdge(new Object(), aaa.asVertex(), bbb.asVertex(), TestIncidenceAaaToBbbEdgeModel.TYPE, TestIncidenceAaaToBbbEdgeModel.class);
            String label = TestIncidenceAaaToBbbEdgeModel.TYPE;
            Edge edge = graphContext.getFramed().addEdge(new Object(), aaa.asVertex(), bbb.asVertex(), label);
            Assert.assertNull(edge.getProperty(WindupFrame.TYPE_PROP));

            graphContext.getGraphTypeManager().addTypeToElement(TestIncidenceAaaToBbbEdgeModel.class, edge);
            Object discriminator = (edge.getProperty(WindupFrame.TYPE_PROP));
            Assert.assertTrue(discriminator instanceof String);
            Assert.assertEquals(TestIncidenceAaaToBbbEdgeModel.TYPE, discriminator);

            TestIncidenceAaaToBbbEdgeModel edgeModel = graphContext.getFramed().frame(edge, TestIncidenceAaaToBbbEdgeModel.class);

            edgeModel.setProp1("edge1");
            graphContext.commit();

            // then load the edge and check the connections,
            TestIncidenceAaaToBbbEdgeModel edgeModel2 = graphContext.getFramed().getEdge(edgeModel.asEdge().getId(), TestIncidenceAaaToBbbEdgeModel.class);
            Assert.assertNotNull(edgeModel2);
            Assert.assertEquals("edge1", edgeModel2.getProp1());
            Assert.assertNotNull(edgeModel2.getAaa());
            Assert.assertNotNull(edgeModel2.getAaa().getEdgesToBbb());
            Assert.assertTrue("Aaa should have edgesToBbb", edgeModel2.getAaa().getEdgesToBbb().iterator().hasNext());
            Assert.assertEquals("edge1", edgeModel2.getAaa().getEdgesToBbb().iterator().next().getProp1());
            Assert.assertTrue("Bbb should have edgesToAaa", edgeModel2.getBbb().getEdgesToAaa().iterator().hasNext());
            Assert.assertEquals("edge1", edgeModel2.getBbb().getEdgesToAaa().iterator().next().getProp1());

            // And that the type was correctly set.
            Assert.assertEquals(TestIncidenceAaaToBbbEdgeModel.TYPE, edgeModel2.asEdge().getProperty(WindupFrame.TYPE_PROP));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Test
    public void testServiceDeletagesInGraphContext() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            TestFooModel created = context.create(TestFooSubModel.class);

            checkObject(created);

            created = context.getUnique(TestFooSubModel.class);
            checkObject(created);

            Iterable<TestFooSubModel> findAll = context.findAll(TestFooSubModel.class);
            Assert.assertNotNull(findAll);
            final Iterator<TestFooSubModel> iterator = findAll.iterator();
            Assert.assertTrue(iterator.hasNext());
            created = iterator.next();
            Assert.assertFalse(iterator.hasNext());

            checkObject(created);
        }
    }

    private void checkObject(TestFooModel created)
    {
        Assert.assertNotNull(created);
        Assert.assertNotNull(created.asVertex());
        Assert.assertNotNull(created.asVertex().getProperty(WindupFrame.TYPE_PROP));
        Assert.assertTrue(created instanceof TestFooSubModel);
        Assert.assertTrue(((List)created.asVertex().getProperty(WindupFrame.TYPE_PROP))
                .contains(TestFooSubModel.class.getAnnotation(TypeValue.class).value()));
    }

}
