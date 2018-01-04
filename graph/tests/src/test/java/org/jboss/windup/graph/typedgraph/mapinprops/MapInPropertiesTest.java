package org.jboss.windup.graph.typedgraph.mapinprops;

import java.util.HashMap;
import java.util.Map;

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

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.service.GraphService;

@RunWith(Arquillian.class)
public class MapInPropertiesTest
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
            .addPackage("org.jboss.windup.graph.typedgraph.mapinprops");
        return archive;
    }

    @Inject
    private GraphContextFactory contextFactory;

    @Test
    public void testMapHandling() throws Exception
    {
        try (GraphContext context = contextFactory.create())
        {
            Assert.assertNotNull(context);
            prepareFrame(context, TestMapPrefixModel.class);

            Vertex v = new GraphService<>(context, TestMapPrefixModel.class).getUnique().asVertex();
            Assert.assertNotNull(v);
            TestMapPrefixModel framed = (TestMapPrefixModel) context.getFramed().frame(v, TestMapPrefixModel.class);
            checkMap(framed.getMap(), 3);
            context.getFramed().removeVertex(v);
        }
    }

    @Test
    public void testMapWithBlankPrefixHandling() throws Exception
    {
        try (GraphContext context = contextFactory.create())
        {
            TestMapBlankSubModel frame = prepareFrame(context, TestMapBlankSubModel.class);
            System.out.println("    Frame class: " + frame.getClass());

            Vertex v = new GraphService<>(context, TestMapBlankSubModel.class).getUnique().asVertex();
            Assert.assertNotNull(v);

            v.setProperty("preexistingKey", "still here");
            TestMapBlankSubModel framed = (TestMapBlankSubModel) context.getFramed().frame(v, TestMapBlankSubModel.class);
            checkMap(framed.getMap(), 5);
            framed.asVertex().getPropertyKeys();
            for (String string : framed.asVertex().getPropertyKeys())
            {
                System.out.println("    Key: " + string);
            }
            Assert.assertEquals("still here", framed.getMap().get("preexistingKey"));
            context.getFramed().removeVertex(v);
        }
    }

    /**
     * This doesn't use submodel.
     */
    @Test
    public void testMapWithBlankPrefixHandling2() throws Exception
    {
        try (GraphContext context = contextFactory.create())
        {
            TestMapBlankModel frame = context.getFramed().addVertex(null, TestMapBlankModel.class);
            Map<String, String> map = prepareMap();
            frame.putNaturalMap(map);

            System.out.println("    Frame class: " + frame.getClass());
            for (Class<?> iface : frame.getClass().getInterfaces())
            {
                System.out.println("      Implements: " + iface.getName());
            }

            Vertex v = new GraphService<>(context, TestMapBlankModel.class).getUnique().asVertex();
            Assert.assertNotNull(v);
            v.setProperty("preexistingKey", "still here");
            TestMapBlankSubModel framed = (TestMapBlankSubModel) context.getFramed().frame(v, TestMapBlankSubModel.class);
            checkMap(framed.getMap(), 5);
            framed.asVertex().getPropertyKeys();
            for (String string : framed.asVertex().getPropertyKeys())
            {
                System.out.println("    Key: " + string);
            }
            Assert.assertEquals("still here", framed.getMap().get("preexistingKey"));
            context.getFramed().removeVertex(v);
        }
    }

    private static void checkMap(Map<String, String> foundMap, int expectedNumOfEntries)
    {
        Assert.assertEquals(expectedNumOfEntries, foundMap.size());
        Assert.assertEquals("value1", foundMap.get("key1"));
        Assert.assertEquals("value2", foundMap.get("key2"));
        Assert.assertEquals("value3", foundMap.get("key3"));
    }

    private static <T extends TestMapPrefixModel> T prepareFrame(GraphContext context, Class<T> cls)
    {
        T mainModel = context.getFramed().addVertex(null, cls);
        Map<String, String> map = prepareMap();
        mainModel.setMap(map);
        return mainModel;
    }

    private static Map<String, String> prepareMap()
    {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        return map;
    }
}
