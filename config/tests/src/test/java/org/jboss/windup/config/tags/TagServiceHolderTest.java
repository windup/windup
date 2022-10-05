package org.jboss.windup.config.tags;

import java.io.File;

import org.jboss.windup.config.selectables.*;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.True;

@RunWith(Arquillian.class)
public class TagServiceHolderTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap
                .create(AddonArchive.class)
                .addClasses(TestIterationPayloadTestRuleProvider.class, TestChildModel.class, TestParentModel.class)
                .addBeansXML()
                .addAsResource(new File("src/test/java/org/jboss/windup/config/tags/test2.tags.xml"))
                .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                );
        return archive;
    }


    @Inject
    private TagServiceHolder tagServiceHolder;

    @Test
    public void testTagsLoading() throws Exception {
        tagServiceHolder.loadTagDefinitions();
        final TagService tagService = tagServiceHolder.getTagService();
        Assert.assertNotNull(tagService.getTag("a-prime"));
        Assert.assertNotNull(tagService.findTag("a-prime"));
        Assert.assertNotNull(tagService.getTag("a1"));
        Assert.assertNull(tagService.findTag("non-existent"));
        try {
            tagService.getTag("non-existent");
            Assert.fail("Should fail: tagService.getTag(\"non-existent\")");
        } catch (Exception ex) {
        }
        Assert.assertNotNull(tagService.getOrCreateTag("to-be-created", false));
        Assert.assertNotNull(tagService.getTag("to-be-created"));
    }


    private Configuration getNoOpRule(GraphContext context) {
        return ConfigurationBuilder.begin().addRule()
                .when(new True())
                .perform(Log.message(Logger.Level.INFO, "Operation runs"));
    }
}
