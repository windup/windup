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

/**
 * Covers: https://issues.jboss.org/browse/WINDUP-168
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@RunWith(Arquillian.class)
public class JavaHandlerSubclassSpecializationTest
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
    public void testSubclassMethodHandling() throws Exception
    {
        Assert.assertNotNull(context);
        context.init(null);
        
        TestFooModel model = context.getFramed().addVertex(null, TestFooModel.class);
        TestFooSubModel subModel = GraphService.addTypeToModel(context, model, TestFooSubModel.class);
        TestFooModel asParent = subModel;

        TestFooModel reframed = (TestFooModel) context.getFramed().frame(model.asVertex(), WindupVertexFrame.class);

        String s1 = model.testJavaMethod();
        String s2 = subModel.testJavaMethod();
        String s3 = asParent.testJavaMethod();
        String s4 = reframed.testJavaMethod();

        Assert.assertEquals("base", s1);
        Assert.assertEquals("subclass", s2);
        Assert.assertEquals("subclass", s3);
        Assert.assertEquals("subclass", s4);
    }
}
