package org.jboss.windup.rules.apps.javaee;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AbstractTest
{
    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addClass(AbstractTest.class).addBeansXML();
    }
}
