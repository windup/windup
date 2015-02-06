package org.jboss.windup.tests.application;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.WindupRulesetMetadata;
import org.jboss.windup.rules.apps.java.JavaRulesetMetadata;
import org.jboss.windup.rules.apps.legacy.java.JavaEERulesetMetadata;
import org.jboss.windup.rules.apps.xml.XmlRulesetMetadata;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupRulesetMetadataTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.ext:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java-ee"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private Imported<WindupRulesetMetadata> ruleMetadata;

    @Test
    public void testRuleMetadata()
    {
        Assert.assertNotNull(ruleMetadata);

        int count = 0;
        boolean foundJavaRulesetMeta = false;
        boolean foundJavaEERulesetMeta = false;
        boolean foundXMLRulesetMeta = false;
        for (WindupRulesetMetadata m : this.ruleMetadata)
        {
            count++;

            if (m instanceof JavaRulesetMetadata)
            {
                foundJavaRulesetMeta = true;
                Assert.assertEquals(JavaRulesetMetadata.RULE_SET_ID, m.getRuleSetID());
            }
            else if (m instanceof JavaEERulesetMetadata)
            {
                foundJavaEERulesetMeta = true;
                Assert.assertEquals(JavaEERulesetMetadata.RULE_SET_ID, m.getRuleSetID());
            }
            else if (m instanceof XmlRulesetMetadata)
            {
                foundXMLRulesetMeta = true;
                Assert.assertEquals(XmlRulesetMetadata.RULE_SET_ID, m.getRuleSetID());
            }
        }

        Assert.assertEquals(4, count);
        Assert.assertTrue(foundJavaRulesetMeta);
        Assert.assertTrue(foundJavaEERulesetMeta);
        Assert.assertTrue(foundXMLRulesetMeta);
    }
}
