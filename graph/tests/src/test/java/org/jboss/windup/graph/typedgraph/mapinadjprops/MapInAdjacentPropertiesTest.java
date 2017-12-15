package org.jboss.windup.graph.typedgraph.mapinadjprops;

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
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@RunWith(Arquillian.class)
public class MapInAdjacentPropertiesTest
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
                    .addClasses(MapMainModel.class);
        return archive;
    }

    @Inject
    private GraphContextFactory contextFactory;

    @Test
    public void testMapHandling() throws Exception
    {
        Assert.assertNotNull(contextFactory);

        try (GraphContext context = contextFactory.create())
        {
            MapMainModel mainModel = context.getFramed().addVertex(null, MapMainModel.class);

            // Map 1
            Map<String, String> map = new HashMap<>();
            map.put("key1", "value1");
            map.put("key2", "value2");
            map.put("key3", "value3");
            mainModel.setMap(map);

            // Map 2
            Map<String, String> map2 = new HashMap<>();
            map2.put("keyA", "valueA");
            map2.put("keyB", "valueB");
            map2.put("keyC", "valueC");
            mainModel.setMap2(map2);

            // Query for the 1 MapMainModel's
            String typeVal = MapMainModel.class.getAnnotation(TypeValue.class).value();
            Iterable<Vertex> vertices = context.getFramed().query()
                        .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, typeVal).vertices();

            int numberFound = 0;
            for (Vertex v : vertices)
            {
                // final Set<String> propertyKeys = v.getVertices( Direction.OUT, "map").iterator().next().getPropertyKeys();

                numberFound++;
                MapMainModel framed = (MapMainModel) context.getFramed().frame(v, WindupVertexFrame.class);

                Assert.assertTrue(framed instanceof MapMainModel);

                // Map 1
                Map<String, String> foundMap = framed.getMap();
                Assert.assertEquals(3, foundMap.size());
                Assert.assertEquals("value1", foundMap.get("key1"));
                Assert.assertEquals("value2", foundMap.get("key2"));
                Assert.assertEquals("value3", foundMap.get("key3"));

                // Map 2
                Map<String, String> foundMap2 = framed.getMap2();
                Assert.assertEquals(3, foundMap2.size());
                Assert.assertEquals("valueA", foundMap2.get("keyA"));
                Assert.assertEquals("valueB", foundMap2.get("keyB"));
                Assert.assertEquals("valueC", foundMap2.get("keyC"));
            }
            Assert.assertEquals(1, numberFound);
        }
    }
}
