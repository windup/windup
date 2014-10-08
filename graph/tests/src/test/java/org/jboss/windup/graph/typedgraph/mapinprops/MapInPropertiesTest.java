package org.jboss.windup.graph.typedgraph.mapinprops;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.Util;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@RunWith(Arquillian.class)
public class MapInPropertiesTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.utils:utils"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    // .addClasses(TestMapPrefixModel.class)
                    // .addClasses(TestMapBlankSubModel.class)
                    .addPackage("org.jboss.windup.graph.typedgraph.mapinprops")
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
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

            Iterable<Vertex> vertices = context.getFramed().query()
                        .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TestMapPrefixModel.class.getAnnotation(TypeValue.class).value()).vertices();
            Vertex v = Util.getSingle(vertices);
            Assert.assertNotNull(v);
            TestMapPrefixModel framed = (TestMapPrefixModel) context.getFramed().frame(v, TestMapPrefixModel.class);
            checkMap(framed.getMap(), 3);
            context.getFramed().removeVertex(v);
        }
    }

    @Test
    // @Ignore("WINDUP-168")
    public void testMapWithBlankPrefixHandling() throws Exception
    {
        try (GraphContext context = contextFactory.create())
        {
            TestMapBlankSubModel frame = prepareFrame(context, TestMapBlankSubModel.class);
            System.out.println("    Frame class: " + frame.getClass());

            Iterable<Vertex> vertices = context.getFramed().query()
                        .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TestMapBlankSubModel.class.getAnnotation(TypeValue.class).value())
                        .vertices();
            Vertex v = Util.getSingle(vertices);
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

            Iterable<Vertex> vertices = context.getFramed().query()
                        .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TestMapBlankModel.class.getAnnotation(TypeValue.class).value()).vertices();
            Vertex v = Util.getSingle(vertices);
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
