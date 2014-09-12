package org.jboss.windup.graph.typedgraph.map;

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
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tinkerpop.blueprints.Vertex;

@RunWith(Arquillian.class)
public class FrameMapHandlerTest
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
                    .addClasses(TestMapMainModel.class, TestMapValueModel.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Test
    public void testMapHandling() throws Exception
    {
        Assert.assertNotNull(context);
        context.init(null);

        TestMapMainModel mainModel = context.getFramed().addVertex(null, TestMapMainModel.class);
        TestMapValueModel value1 = context.getFramed().addVertex(null, TestMapValueModel.class);
        value1.setProperty("value1");
        TestMapValueModel value2 = context.getFramed().addVertex(null, TestMapValueModel.class);
        value2.setProperty("value2");
        TestMapValueModel value3 = context.getFramed().addVertex(null, TestMapValueModel.class);
        value3.setProperty("value3");

        Map<String, TestMapValueModel> map = new HashMap<>();
        map.put("key1", value1);
        map.put("key2", value2);
        map.put("key3", value3);

        mainModel.setMap(map);

        Iterable<Vertex> vertices = context.getQuery().type(TestMapMainModel.class).vertices();

        int numberFound = 0;
        for (Vertex v : vertices)
        {
            numberFound++;
            TestMapMainModel framed = (TestMapMainModel) context.getFramed().frame(v, WindupVertexFrame.class);

            Assert.assertTrue(framed instanceof TestMapMainModel);

            Map<String, TestMapValueModel> foundMap = framed.getMap();
            Assert.assertEquals(3, foundMap.size());

            Assert.assertEquals("value1", foundMap.get("key1").getProperty());
            Assert.assertEquals("value2", foundMap.get("key2").getProperty());
            Assert.assertEquals("value3", foundMap.get("key3").getProperty());
        }
        Assert.assertEquals(1, numberFound);
    }
}
