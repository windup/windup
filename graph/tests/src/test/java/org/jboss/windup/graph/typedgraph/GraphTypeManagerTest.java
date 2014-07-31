package org.jboss.windup.graph.typedgraph;

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
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import static org.jboss.windup.graph.model.WindupVertexFrame.TYPE_PROP;

@RunWith(Arquillian.class)
public class GraphTypeManagerTest
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
                    .addClasses(FooModel.class, FooSubModel.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Test
    public void testGraphTypeHandling() throws Exception
    {
        Assert.assertNotNull(context);

        FooModel initialModelType = context.getFramed().addVertex(null, FooModel.class);

        GraphService.addTypeToModel(context, initialModelType, FooSubModel.class);

        Iterable<Vertex> vertices = context.getFramed().query()
                    .has(TYPE_PROP, Text.CONTAINS, FooModel.class.getAnnotation(TypeValue.class).value())
                    .vertices();

        int numberFound = 0;
        for (Vertex v : vertices)
        {
            numberFound++;
            WindupVertexFrame framed = context.getFramed().frame(v, WindupVertexFrame.class);

            Assert.assertTrue(framed instanceof FooModel);
            Assert.assertTrue(framed instanceof FooSubModel);
        }
        Assert.assertEquals(1, numberFound);
    }
}
