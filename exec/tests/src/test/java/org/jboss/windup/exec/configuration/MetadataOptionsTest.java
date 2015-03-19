package org.jboss.windup.exec.configuration;

import java.util.Collection;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.Technology;
import org.jboss.windup.exec.configuration.options.ExcludeTagsOption;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;
import org.jboss.windup.exec.configuration.options.SourceOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">jsightler</a>
 */
@RunWith(Arquillian.class)
public class MetadataOptionsTest
{
    @Deployment
    @Dependencies({
        @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
        @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap
            .create(ForgeArchive.class)
            .addBeansXML()
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
            );

        return archive;
    }

    @Inject
    private Furnace furnace;
    @Inject
    private SourceOption sourceOption;
    @Inject
    private TargetOption targetOption;
    @Inject
    private IncludeTagsOption includeTagsOption;
    @Inject
    private ExcludeTagsOption excludeTagsOption;

    @Test
    public void testSourceOption() throws Exception
    {
        Collection<?> availableValues = sourceOption.getAvailableValues();

        Assert.assertTrue(availableValues.contains("sourceTech1"));
        Assert.assertTrue(availableValues.contains("sourceTech2"));
    }

    @Test
    public void testTargetOption() throws Exception
    {
        Collection<?> availableValues = targetOption.getAvailableValues();

        Assert.assertTrue(availableValues.contains("targetTech1"));
        Assert.assertTrue(availableValues.contains("targetTech1"));
    }

    @Test
    public void testIncludeTags() throws Exception {
        Collection<?> availableValues = includeTagsOption.getAvailableValues();

        Assert.assertTrue(availableValues.contains("tag1"));
        Assert.assertTrue(availableValues.contains("tag2"));
        Assert.assertTrue(availableValues.contains("tag3"));
    }

    @Test
    public void testExcludeTags() throws Exception {
        Collection<?> availableValues = excludeTagsOption.getAvailableValues();

        Assert.assertTrue(availableValues.contains("tag1"));
        Assert.assertTrue(availableValues.contains("tag2"));
        Assert.assertTrue(availableValues.contains("tag3"));
    }

    @RuleMetadata(
        sourceTechnologies = {
            @Technology(id = "sourceTech1", versionRange = "[0, ]"),
            @Technology(id = "sourceTech2", versionRange = "[0, ]")
        },
        targetTechnologies = {
            @Technology(id = "targetTech1", versionRange = "[0, ]"),
            @Technology(id = "targetTech2", versionRange = "[0, ]")
        },
        tags = { "tag1", "tag2", "tag3" })
    public static class MetadataRuleProvider extends AbstractRuleProvider
    {
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin();
        }
    }
}
