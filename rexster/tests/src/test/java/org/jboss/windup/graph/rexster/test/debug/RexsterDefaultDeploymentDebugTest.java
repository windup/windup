package org.jboss.windup.graph.rexster.test.debug;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.Socket;

/**
 * Tests that rexster is properly deployed thanks to Arquillian addon
 */
@RunWith(Arquillian.class)
public class RexsterDefaultDeploymentDebugTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:simple")
    })
    public static AddonArchive getDeployment()
    {
        System.setProperty("maven.surefire.debug", "true");
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addAsLocalServices(RexsterDefaultDeploymentDebugTest.class);

        return archive;
    }

    @Test
    public void testRexsterProperStart() throws IOException, InstantiationException, IllegalAccessException
    {
        Furnace furnace = FurnaceHolder.getFurnace();
        Imported<GraphContextFactory> factory = furnace.getAddonRegistry().getServices(GraphContextFactory.class);
        //GraphContext creation will start the rexster
        try (GraphContext context = factory.get().create())
        {
            Socket s = null;
            try
            {
                s = new Socket("localhost", 8182);
            }
            catch (Exception e)
            {
                Assert.fail("Rexster is not listening on localhost:8182");
            }
            finally
            {
                if (s != null)
                    try
                    {
                        s.close();
                    }
                    catch (Exception e)
                    {
                    }
            }

        }

    }
}