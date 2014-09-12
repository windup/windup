package org.jboss.windup.graph.test;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.typedgraph.TestFooModel;
import org.jboss.windup.graph.typedgraph.TestFooSubModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tinkerpop.blueprints.util.wrappers.event.listener.StubGraphChangedListener;

@RunWith(Arquillian.class)
public class EventGraphTest
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
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Test
    public void testEventGraph() throws Exception
    {
        Assert.assertNotNull(context);
        context.init(null);

        StubGraphChangedListener stubGraphListener = new StubGraphChangedListener();
        context.getGraph().addListener(stubGraphListener);

        TestFooModel initialModelType = context.getFramed().addVertex(null, TestFooModel.class);

        // There should be one added vertex
        Assert.assertEquals(1, stubGraphListener.addVertexEventRecorded());

        // reset all stats to zero
        stubGraphListener.reset();

        //records as a property change.
        initialModelType.setProp1("ex");
        
        Assert.assertEquals(1, stubGraphListener.vertexPropertyChangedEventRecorded());
    }
}
