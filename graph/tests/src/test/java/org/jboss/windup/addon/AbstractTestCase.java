package org.jboss.windup.addon;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;

/**
 * This is here for reference. Static getDeployment() doesn't allow leveraging subclassing. FORGE-1790
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class AbstractTestCase
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Test
    public void foo()
    {

    }
}