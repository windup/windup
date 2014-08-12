package org.jboss.windup.graph.duplicate.typevalue;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class DuplicateTypeValueTest
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
            .addClasses(TestSimpleModel.class, TestSimpleModel2.class)
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
            );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Ignore("Until WINDUP-167 is done.")
    @Test
    public void testDuplicateTypeValue() throws Exception
    {
        Assert.assertNotNull(context);

        try {
            // This is where Windup gets initialized, hence here the model scanning happens.
            TestSimpleModel simpleModel = context.getFramed().addVertex(null, TestSimpleModel.class);
            TestSimpleModel2 simpleModel2 = context.getFramed().addVertex(null, TestSimpleModel2.class);
            Assert.fail("Usage of duplicated @TypeValue went unnoticed.");
        }
        catch(WindupException ex){
            Assert.assertTrue(ex.getMessage().toLowerCase().contains("duplicate")); // Unsafe.
            // This is ok
        }
        catch(Exception ex){
            throw new WindupException("Unexpected exception: " + ex.getMessage(), ex);
        }
        
    }
}
