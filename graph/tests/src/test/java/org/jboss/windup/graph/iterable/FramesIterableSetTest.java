package org.jboss.windup.graph.iterable;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.duplicate.typevalue.TestSimpleModel;
import org.jboss.windup.graph.iterables.FramesSetIterable;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Testing the FramesIterableSet
 */
@RunWith(Arquillian.class)
public class FramesIterableSetTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClasses(TestSimpleModel.class);

        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testIterableSetEmpty() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            List<WindupVertexFrame> models = fillInTestDataAndReturn(context);
            FramesSetIterable iterable = new FramesSetIterable(new ArrayList<>());
            checkForDuplicates(iterable);
            Assert.assertEquals(0,getIterableSize(iterable));
        }
    }

    @Test
    public void testIterableSetWithoutDuplicates() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            List<WindupVertexFrame> models = fillInTestDataAndReturn(context);
            FramesSetIterable iterable = new FramesSetIterable(models);
            checkForDuplicates(iterable);
            Assert.assertEquals(4,getIterableSize(iterable));
        }
    }

    @Test
    public void testIterableSetWithDuplicates() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            List<WindupVertexFrame> models = fillInTestDataAndReturn(context);
            models.add(models.get(0));
            models.add(models.get(0));
            models.add(models.get(1));
            models.add(models.get(2));
            FramesSetIterable<WindupVertexFrame> iterable = new FramesSetIterable(models);
            checkForDuplicates(iterable);
            Assert.assertEquals(4,getIterableSize(iterable));
        }
    }

    private void checkForDuplicates(FramesSetIterable<WindupVertexFrame> iterable) {
        Set<String> ids = new HashSet<>();
        for(WindupVertexFrame frame : iterable) {
            String frameId = frame.asVertex().getId().toString();
            if(ids.contains(frameId)){
                Assert.fail("FramesSetIterable should not return multiple vertices with same ID. However, it contained twice ID " + frameId);
            }
        }
    }

    private int getIterableSize(Iterable<WindupVertexFrame> iterable) {
        int size = 0;
        for(WindupVertexFrame frame : iterable) {
            size++;
        }
        return size;
    }

    private List<WindupVertexFrame> fillInTestDataAndReturn(GraphContext context) {
        WindupVertexFrame model1= context.getFramed().addVertex(null, TestSimpleModel.class);
        WindupVertexFrame model2= context.getFramed().addVertex(null, TestSimpleModel.class);
        WindupVertexFrame model3= context.getFramed().addVertex(null, TestSimpleModel.class);
        WindupVertexFrame model4= context.getFramed().addVertex(null, TestSimpleModel.class);
        List<WindupVertexFrame> models = new ArrayList<>();
        models.add(model1);
        models.add(model2);
        models.add(model3);
        models.add(model4);
        return models;
    }
}
