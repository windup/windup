package org.jboss.windup.graph.typedgraph;

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
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class WindupPropertyMethodHandlerTest {

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
                .addClasses(TestFooModel.class, TestFooSubModel.class);
        return archive;
    }

    @Test
    public void testPropertyHandler() throws Exception {
        try (GraphContext context = factory.create(true)) {
            Assert.assertNotNull(context);

            GraphService<TestFooModel> fooModelService = new GraphService<>(context, TestFooModel.class);

            TestFooModel testFooModel = fooModelService.create();
            testFooModel.setProp1("prop1");
            testFooModel.setProp2("prop2");
            testFooModel.setProp3("prop3");

            Iterable<Vertex> vertices = context.getQuery(TestFooModel.class).toList(TestFooModel.class).stream()
                    .map(TestFooModel::getElement)
                    .collect(Collectors.toList());

            int numberFound = 0;
            for (Vertex v : vertices) {
                numberFound++;
                TestFooModel framed = (TestFooModel) context.getFramed().frameElement(v, WindupVertexFrame.class);

                Assert.assertTrue(framed instanceof TestFooModel);
                Assert.assertEquals("prop1", framed.getProp1());
                Assert.assertEquals("prop2", framed.getProp2());
                Assert.assertEquals("prop3", framed.getProp3());
            }
            Assert.assertEquals(1, numberFound);
        }
    }
}
