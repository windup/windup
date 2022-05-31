package org.jboss.windup.tests.application;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.metadata.RulesetMetadata;
import org.jboss.windup.rules.apps.java.JavaRulesetMetadata;
import org.jboss.windup.rules.apps.legacy.java.JavaEERulesetMetadata;
import org.jboss.windup.rules.apps.xml.XmlRulesetMetadata;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class WindupRulesetMetadataTest {

    @Inject
    private Imported<RulesetMetadata> ruleMetadata;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
    }

    @Test
    public void testRuleMetadata() {
        Assert.assertNotNull(ruleMetadata);

        int count = 0;
        boolean foundJavaRulesetMeta = false;
        boolean foundJavaEERulesetMeta = false;
        boolean foundXMLRulesetMeta = false;
        for (RulesetMetadata m : this.ruleMetadata) {
            count++;

            if (JavaRulesetMetadata.RULE_SET_ID.equals(m.getID())) {
                foundJavaRulesetMeta = true;
            } else if (JavaEERulesetMetadata.RULE_SET_ID.equals(m.getID())) {
                foundJavaEERulesetMeta = true;
            } else if (XmlRulesetMetadata.RULE_SET_ID.equals(m.getID())) {
                foundXMLRulesetMeta = true;
            }
        }

        Assert.assertEquals(4, count);
        Assert.assertTrue(foundJavaRulesetMeta);
        Assert.assertTrue(foundJavaEERulesetMeta);
        Assert.assertTrue(foundXMLRulesetMeta);
    }
}
