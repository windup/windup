package org.jboss.windup.reporting.xslt.jaxb;

import org.jboss.windup.reporting.xslt.*;
import java.nio.file.Path;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class XFrameToJaxbTest
{
    @Deployment
    @Dependencies({
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
            .addBeansXML()
            .addClass(XFrameToJaxbTest.class)
            //.addAsResource(new File("src/test/resources/reports"))
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
            );
        return archive;
    }

    @Test
    public void testFrameToJaxb() throws Exception
    {

    }

}

