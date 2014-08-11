package org.jboss.windup.graph.typedgraph.graphservice;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
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
import org.jboss.windup.graph.test.graphservice.model.TestMergeModel;
import org.jboss.windup.graph.test.graphservice.model.TestMergeBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


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
            //.addClasses(TestMergeModel.class)
            .addPackage("org.jboss.windup.graph.test.graphservice.model")
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
    

    
    // TODO: Move to Graph API's tests.
    @Test
    public void testMerge() throws Exception
    {
        try {
            GraphService<TestMergeModel> gsMerge = new GraphService<>( context, TestMergeModel.class );
            
            final TestMergeBean bean = new TestMergeBean();
            bean.setProp1("val1");
            bean.setProp2("val2");
            TestMergeModel merged = gsMerge.merge( bean );
            
            Assert.assertNotNull("Merged vertex is not null", merged.asVertex());
            Assert.assertEquals("val1", merged.asVertex().getProperty("prop1") );
            Assert.assertEquals("val2", merged.asVertex().getProperty("prop2") );
        }
        catch( Exception ex )
        {
            throw new Exception(ex);
        }
    }
    
}
