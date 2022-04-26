package org.jboss.windup.exec.configuration;

import java.util.Collection;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.Technology;
import org.jboss.windup.exec.configuration.options.ExcludeTagsOption;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;
import org.jboss.windup.exec.configuration.options.SourceOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class MetadataOptionsTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap
                    .create(AddonArchive.class)
                    .addBeansXML();

        return archive;
    }

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
        Object[] availableValues = sourceOption.getAvailableValues().toArray();

        Assert.assertEquals("sourceTech1", availableValues[0]);
        Assert.assertEquals("sourceTech2", availableValues[1]);
    }

    @Test
    public void testTargetOption() throws Exception
    {
        Object[] availableValues = targetOption.getAvailableValues().toArray();

        Assert.assertEquals("targetTech1", availableValues[0]);
        Assert.assertEquals("targetTech2", availableValues[1]);
    }

    @Test
    public void testIncludeTags() throws Exception
    {
        Collection<?> availableValues = includeTagsOption.getAvailableValues();

        Assert.assertTrue(availableValues.contains("tag1"));
        Assert.assertTrue(availableValues.contains("tag2"));
        Assert.assertTrue(availableValues.contains("tag3"));
    }

    @Test
    public void testExcludeTags() throws Exception
    {
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
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
        {
            return ConfigurationBuilder.begin();
        }
    }
}
