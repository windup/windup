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
import java.net.ConnectException;
import java.net.Socket;

/**
 * Tests that rexster is not deployed by Arquillian addon in case debug mode is OFF.
 */
@RunWith(Arquillian.class)
public class RexsterDefaultDeploymentNoDebugTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:simple")
    })
    public static AddonArchive getDeployment()
    {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addAsLocalServices(RexsterDefaultDeploymentNoDebugTest.class);

        return archive;
    }

    @Test(expected = ConnectException.class)
    public void testRexsterProperStart() throws IOException, InstantiationException, IllegalAccessException
    {
        Furnace furnace = FurnaceHolder.getFurnace();
        Imported<GraphContextFactory> factory = furnace.getAddonRegistry().getServices(GraphContextFactory.class);
        try (GraphContext context = factory.get().create())
        {
            Socket s = null;
            try
            {
                s = new Socket("localhost", 8182);
                Assert.fail("Rexster should not be registered when not in debug mode.");
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