package org.jboss.windup.graph.typedgraph.setinprops;

import org.apache.tinkerpop.gremlin.structure.Vertex;
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

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;


@RunWith(Arquillian.class)
public class SetInPropertiesTest {
    @Inject
    private GraphContextFactory contextFactory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addPackage("org.jboss.windup.graph.typedgraph.setinprops");
        return archive;
    }

    private static void checkSet(Set<String> foundSet, int expectedNumOfEntries) {
        Assert.assertEquals(expectedNumOfEntries, foundSet.size());
        Assert.assertTrue(foundSet.contains("value1"));
        Assert.assertTrue(foundSet.contains("value2"));
        Assert.assertTrue(foundSet.contains("value3"));
    }

    private static <T extends TestSetPrefixModel> T prepareFrame(GraphContext context, Class<T> cls) {
        T mainModel = context.getFramed().addFramedVertex(cls);
        Set<String> set = prepareSet();
        mainModel.setSet(set);
        return mainModel;
    }

    private static Set<String> prepareSet() {
        Set<String> set = new HashSet<>();
        set.add("value1");
        set.add("value2");
        set.add("value3");
        return set;
    }

    @Test
    public void testSetHandling() throws Exception {
        try (GraphContext context = contextFactory.create(true)) {
            Assert.assertNotNull(context);
            prepareFrame(context, TestSetPrefixModel.class);

            Vertex v = new GraphService<>(context, TestSetPrefixModel.class).getUnique().getElement();

            Assert.assertNotNull(v);
            TestSetPrefixModel framed = (TestSetPrefixModel) context.getFramed().frameElement(v, TestSetPrefixModel.class);
            checkSet(framed.getSet(), 3);
            v.remove();
        }
    }

    @Test
    public void testSetWithBlankPrefixHandling() throws Exception {
        try (GraphContext context = contextFactory.create(true)) {
            TestSetBlankSubModel frame = prepareFrame(context, TestSetBlankSubModel.class);
            System.out.println("    Frame class: " + frame.getClass());

            Vertex v = new GraphService<>(context, TestSetBlankSubModel.class).getUnique().getElement();
            Assert.assertNotNull(v);

            v.property("still here", "does't matter");
            TestSetBlankSubModel framed = (TestSetBlankSubModel) context.getFramed().frameElement(v, TestSetBlankSubModel.class);
            checkSet(framed.getSet(), 5);
            for (String string : framed.getElement().keys()) {
                System.out.println("    Key: " + string);
            }
            Assert.assertTrue(framed.getSet().contains("still here"));
            v.remove();
        }
    }

    /**
     * This doesn't use submodel.
     */
    @Test
    public void testSetWithBlankPrefixHandling2() throws Exception {
        try (GraphContext context = contextFactory.create(true)) {
            TestSetBlankModel frame = context.getFramed().addFramedVertex(TestSetBlankModel.class);
            Set<String> set = prepareSet();
            frame.addAllNaturalSet(set);

            System.out.println("    Frame class: " + frame.getClass());
            for (Class<?> iface : frame.getClass().getInterfaces()) {
                System.out.println("      Implements: " + iface.getName());
            }

            Vertex v = new GraphService<>(context, TestSetBlankModel.class).getUnique().getElement();

            Assert.assertNotNull(v);
            v.property("still here", "does't matter");
            TestSetBlankSubModel framed = (TestSetBlankSubModel) context.getFramed().frameElement(v, TestSetBlankSubModel.class);
            checkSet(framed.getSet(), 5);
            for (String string : framed.getElement().keys()) {
                System.out.println("    Key: " + string);
            }
            Assert.assertTrue(framed.getSet().contains("still here"));
            v.remove();
        }
    }
}
