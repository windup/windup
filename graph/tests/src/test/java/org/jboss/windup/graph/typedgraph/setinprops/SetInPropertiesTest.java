package org.jboss.windup.graph.typedgraph.setinprops;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class SetInPropertiesTest
{
    @Deployment
    @AddonDependencies({
        @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
        @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
            .addBeansXML()
            .addPackage("org.jboss.windup.graph.typedgraph.setinprops");
        return archive;
    }

    @Inject
    private GraphContextFactory contextFactory;

    @Test
    public void testSetHandling() throws Exception
    {
        try (GraphContext context = contextFactory.create())
        {
            Assert.assertNotNull(context);
            prepareFrame(context, TestSetPrefixModel.class);

            Vertex v = new GraphService<>(context, TestSetPrefixModel.class).getUnique().asVertex();

            Assert.assertNotNull(v);
            TestSetPrefixModel framed = (TestSetPrefixModel) context.getFramed().frame(v, TestSetPrefixModel.class);
            checkSet(framed.getSet(), 3);
            context.getFramed().removeVertex(v);
        }
    }

    @Test
    public void testSetWithBlankPrefixHandling() throws Exception
    {
        try (GraphContext context = contextFactory.create())
        {
            TestSetBlankSubModel frame = prepareFrame(context, TestSetBlankSubModel.class);
            System.out.println("    Frame class: " + frame.getClass());

            Vertex v = new GraphService<>(context, TestSetBlankSubModel.class).getUnique().asVertex();
            Assert.assertNotNull(v);

            v.setProperty("still here", "does't matter");
            TestSetBlankSubModel framed = (TestSetBlankSubModel) context.getFramed().frame(v, TestSetBlankSubModel.class);
            checkSet(framed.getSet(), 5);
            framed.asVertex().getPropertyKeys();
            for (String string : framed.asVertex().getPropertyKeys())
            {
                System.out.println("    Key: " + string);
            }
            Assert.assertTrue(framed.getSet().contains("still here"));
            context.getFramed().removeVertex(v);
        }
    }

    /**
     * This doesn't use submodel.
     */
    @Test
    public void testSetWithBlankPrefixHandling2() throws Exception
    {
        try (GraphContext context = contextFactory.create())
        {
            TestSetBlankModel frame = context.getFramed().addVertex(null, TestSetBlankModel.class);
            Set<String> set = prepareSet();
            frame.addAllNaturalSet(set);

            System.out.println("    Frame class: " + frame.getClass());
            for (Class<?> iface : frame.getClass().getInterfaces())
            {
                System.out.println("      Implements: " + iface.getName());
            }

            Vertex v = new GraphService<>(context, TestSetBlankModel.class).getUnique().asVertex();

            Assert.assertNotNull(v);
            v.setProperty("still here", "does't matter");
            TestSetBlankSubModel framed = (TestSetBlankSubModel) context.getFramed().frame(v, TestSetBlankSubModel.class);
            checkSet(framed.getSet(), 5);
            framed.asVertex().getPropertyKeys();
            for (String string : framed.asVertex().getPropertyKeys())
            {
                System.out.println("    Key: " + string);
            }
            Assert.assertTrue(framed.getSet().contains("still here"));
            context.getFramed().removeVertex(v);
        }
    }

    private static void checkSet(Set<String> foundSet, int expectedNumOfEntries)
    {
        Assert.assertEquals(expectedNumOfEntries, foundSet.size());
        Assert.assertTrue(foundSet.contains("value1"));
        Assert.assertTrue(foundSet.contains("value2"));
        Assert.assertTrue(foundSet.contains("value3"));
    }

    private static <T extends TestSetPrefixModel> T prepareFrame(GraphContext context, Class<T> cls)
    {
        T mainModel = context.getFramed().addVertex(null, cls);
        Set<String> set = prepareSet();
        mainModel.setSet(set);
        return mainModel;
    }

    private static Set<String> prepareSet()
    {
        Set<String> set = new HashSet<>();
        set.add("value1");
        set.add("value2");
        set.add("value3");
        return set;
    }
}
