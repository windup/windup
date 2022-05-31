package org.jboss.windup.graph.typedgraph.map;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class FrameMapHandlerTest {
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClasses(TestMapMainModel.class, TestMapValueModel.class);
        return archive;
    }

    @Test
    public void testMapHandling() throws Exception {
        try (GraphContext context = factory.create(true)) {
            Assert.assertNotNull(context);

            TestMapMainModel mainModel = context.getFramed().addFramedVertex(TestMapMainModel.class);
            TestMapValueModel value1 = context.getFramed().addFramedVertex(TestMapValueModel.class);
            value1.setProperty("value1");
            TestMapValueModel value2 = context.getFramed().addFramedVertex(TestMapValueModel.class);
            value2.setProperty("value2");
            TestMapValueModel value3 = context.getFramed().addFramedVertex(TestMapValueModel.class);
            value3.setProperty("value3");

            Map<String, TestMapValueModel> map = new HashMap<>();
            map.put("key1", value1);
            map.put("key2", value2);
            map.put("key3", value3);

            mainModel.setMap(map);

            Iterable<Vertex> vertices = context.getQuery(TestMapMainModel.class).toList(TestMapMainModel.class)
                    .stream()
                    .map(TestMapMainModel::getElement)
                    .collect(Collectors.toList());

            int numberFound = 0;
            for (Vertex v : vertices) {
                numberFound++;
                TestMapMainModel framed = (TestMapMainModel) context.getFramed().frameElement(v, WindupVertexFrame.class);

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
}
