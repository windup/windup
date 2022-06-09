package org.jboss.windup.graph.typedgraph.graphservice;

import java.util.ArrayList;
import java.util.Iterator;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.graph.typedgraph.TestFooModel;
import org.jboss.windup.graph.typedgraph.TestFooSubModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.typedgraph.TestIncidenceAaaModel;
import org.jboss.windup.graph.typedgraph.TestIncidenceAaaToBbbEdgeModel;
import org.jboss.windup.graph.typedgraph.TestIncidenceBbbModel;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assume;

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
        try (GraphContext context = factory.create(true))
        {
            Assert.assertNotNull(context);
            TestFooModel initialModelType = context.getFramed().addFramedVertex(TestFooModel.class);

            try
            {
                GraphService.addTypeToModel(context, initialModelType, TestFooSubModel.class);

                Iterable<Vertex> vertices = context.getQuery(TestFooModel.class).toList(TestFooModel.class).stream()
                        .map(TestFooModel::getElement)
                        .collect(Collectors.toList());

                int numberFound = 0;
                for (Vertex v : vertices)
                {
                    numberFound++;
                    WindupVertexFrame framed = context.getFramed().frameElement(v, WindupVertexFrame.class);

                    Assert.assertTrue(framed instanceof TestFooModel);
                    Assert.assertTrue(framed instanceof TestFooSubModel);
                }
                Assert.assertEquals(1, numberFound);
            }
            finally
            {
                initialModelType.remove();
            }
        }
    }

    @Test
    public void testGraphSearchWithoutCommit() throws Exception
    {
        try (GraphContext context = factory.create(true))
        {
            Assert.assertNotNull(context);

            TestFooModel foo1 = context.getFramed().addFramedVertex(TestFooModel.class);
            TestFooModel foo2 = context.getFramed().addFramedVertex(TestFooModel.class);
            TestFooModel foo3 = context.getFramed().addFramedVertex(TestFooModel.class);
            TestFooModel foo4 = context.getFramed().addFramedVertex(TestFooModel.class);

            try
            {
                GraphService.addTypeToModel(context, foo1, TestFooSubModel.class);
                GraphService.addTypeToModel(context, foo2, TestFooSubModel.class);

                Iterable<Vertex> vertices = context.getQuery(TestFooSubModel.class).toList(TestFooSubModel.class).stream()
                        .map(TestFooSubModel::getElement)
                        .collect(Collectors.toList());

                int numberFound = 0;
                for (Vertex v : vertices)
                {
                    numberFound++;
                    WindupVertexFrame framed = context.getFramed().frameElement(v, WindupVertexFrame.class);

                    Assert.assertTrue(framed instanceof TestFooModel);
                }
                Assert.assertEquals(2, numberFound);
            }
            finally
            {
                foo1.remove();
                foo2.remove();
                foo3.remove();
                foo4.remove();
            }
        }
    }

    @Test
    public void testModelCreation() throws Exception
    {
        try (GraphContext context = factory.create(true))
        {
            Assert.assertNotNull(context);

            Service<TestFooSubModel> graphService = new GraphService<>(context, TestFooSubModel.class);

            // test there is no vertex of such type
            Iterable<TestFooSubModel> foundAll = graphService.findAll();
            Assert.assertFalse(foundAll.iterator().hasNext());

            TestFooSubModel model = graphService.create();
            model.setFoo("myFoo");

            // test findAll
            List<Vertex> vertices = context.getGraph().traversal().V().has(WindupVertexFrame.TYPE_PROP, "Foo").toList();
            //query.has(WindupVertexFrame.TYPE_PROP, Cmp.EQUAL, "Foo");
            //Iterable<TestFooSubModel> verticesFoundByContext = query.vertices(TestFooSubModel.class);


            Iterator<TestFooSubModel> iterator = (Iterator<TestFooSubModel>)context.getFramed().frame(vertices.iterator(), TestFooSubModel.class);
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
            model.remove();
        }
    }


    @Test
    public void testEdgeFrames() throws Exception
    {
        try (GraphContext graphContext = factory.create(true))
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
            TestIncidenceAaaToBbbEdgeModel edgeModel = graphContext.getFramed().addFramedEdge(aaa, bbb, TestIncidenceAaaToBbbEdgeModel.TYPE, TestIncidenceAaaToBbbEdgeModel.class);
            Assert.assertTrue(edgeModel.getElement().property(WindupFrame.TYPE_PROP).isPresent());

            graphContext.getGraphTypeManager().addTypeToElement(TestIncidenceAaaToBbbEdgeModel.class, edgeModel.getElement());
            Object discriminator = edgeModel.getElement().property(WindupFrame.TYPE_PROP).value();
            Assert.assertTrue(discriminator instanceof String);
            Assert.assertEquals(TestIncidenceAaaToBbbEdgeModel.TYPE, discriminator);

            edgeModel.setProp1("edge1");
            graphContext.commit();

            // then load the edge and check the connections,

            TestIncidenceAaaToBbbEdgeModel edgeModel2 = graphContext.getFramed().frameElement(edgeModel.getElement(), TestIncidenceAaaToBbbEdgeModel.class);
            Assert.assertNotNull(edgeModel2);
            Assert.assertEquals("edge1", edgeModel2.getProp1());
            Assert.assertNotNull(edgeModel2.getAaa());
            Assert.assertNotNull(edgeModel2.getAaa().getEdgesToBbb());
            Assert.assertTrue("Aaa should have edgesToBbb", edgeModel2.getAaa().getEdgesToBbb().iterator().hasNext());
            Assert.assertEquals("edge1", edgeModel2.getAaa().getEdgesToBbb().iterator().next().getProp1());
            Assert.assertTrue("Bbb should have edgesToAaa", edgeModel2.getBbb().getEdgesToAaa().iterator().hasNext());
            Assert.assertEquals("edge1", edgeModel2.getBbb().getEdgesToAaa().iterator().next().getProp1());

            // And that the type was correctly set.
            Assert.assertEquals(TestIncidenceAaaToBbbEdgeModel.TYPE, edgeModel2.getElement().property(WindupFrame.TYPE_PROP).value());
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
        try (GraphContext context = factory.create(true))
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
        Assert.assertNotNull(created.getElement());
        Assert.assertNotNull(created.getElement().properties(WindupFrame.TYPE_PROP));
        Assert.assertTrue(created instanceof TestFooSubModel);

        Iterator<VertexProperty<Object>> typeProperties = created.getElement().properties(WindupFrame.TYPE_PROP);
        List<String> types = new ArrayList<>();
        typeProperties.forEachRemaining(p -> types.add((String)p.value()));
        Assert.assertTrue(types.contains(TestFooSubModel.class.getAnnotation(TypeValue.class).value()));
    }

    @Test
    public void testGetOrCreateByProperties() throws Exception {
        try (GraphContext context = factory.create(true)) {
            Assert.assertNotNull(context);

            TestFooModel[] foos = new TestFooModel[5];
            for (int k = 0; k < 4; k++) {
                TestFooModel foo = context.getFramed().addFramedVertex(TestFooModel.class);
                foo.setProp1("" + k);
                foo.setProp2("" + (k / 2));
                foo.setProp3("" + (k % 2));
                foos[k] = foo;
            }

            try {
                GraphService<TestFooModel> service = new GraphService<>(context, TestFooModel.class);

                TestFooModel foo = service.getOrCreateByProperties("prop2", "1", "prop3", "1");
                Assert.assertNotNull(foo);
                Assert.assertEquals("3", foo.getProp1());

                foos[4] = service.getOrCreateByProperties("prop2", "2", "prop3", "0");
                Assert.assertNotNull(foos[4]);
                Assert.assertNull(foos[4].getProp1());
                Assert.assertEquals("2", foos[4].getProp2());

                try {
                    service.getOrCreateByProperties("prop2", "1", "prop3");
                    Assert.fail("Exception should be raised");
                } catch (RuntimeException e) {
                    Assert.assertEquals("Number of arguments should be even.", e.getMessage());
                }

            } finally {
                for (TestFooModel foo: foos) {
                    if (foo != null)
                        foo.remove();
                }
            }
        }
    }

}
