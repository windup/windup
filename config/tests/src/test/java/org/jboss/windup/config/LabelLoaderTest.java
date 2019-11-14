package org.jboss.windup.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.builder.LabelProviderBuilder;
import org.jboss.windup.config.loader.LabelLoader;
import org.jboss.windup.config.loader.LabelProviderLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.metadata.LabelMetadataBuilder;
import org.jboss.windup.config.metadata.LabelProviderData;
import org.jboss.windup.config.metadata.LabelProviderMetadata;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.ArchiveExtractionPhase;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

@RunWith(Arquillian.class)
public class LabelLoaderTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML();
        return archive;
    }

    @Inject
    private LabelLoader loader;

    @Test
    public void testRuleProviderWithFilter() throws IOException
    {
        Predicate<RuleProvider> predicate = (provider) -> true;
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), predicate);

        List<LabelProvider> providers = loader.loadConfiguration(ruleLoaderContext).getProviders();
        boolean found1 = false;
        boolean found2 = false;
        for (LabelProvider provider : providers)
        {
            for (Label label : provider.getData().getLabels())
            {
                if (label.getId().equals(TestLabelProvider1.class.getSimpleName()))
                {
                    found1 = true;
                }
                else if (label.getId().equals(TestLabelProvider2.class.getSimpleName()))
                {
                    found2 = true;
                }
            }
        }
        Assert.assertTrue(found1);
        Assert.assertTrue(found2);
    }

    @Singleton
    public static class TestLabelProvider1 implements LabelProviderLoader
    {
        @Override
        public boolean isFileBased()
        {
            return false;
        }

        @Override
        public List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext)
        {
            LabelProviderData data = () -> {
                Label label = new Label(TestLabelProvider1.class.getSimpleName(), "label1Name", "label2Description");
                return Collections.singletonList(label);
            };

            LabelProviderMetadata metadata = new LabelMetadataBuilder("labelSet1ID", "labelSet1Description");

            LabelProvider provider = new LabelProviderBuilder(metadata, data);
            return Collections.singletonList(provider);
        }
    }

    @Singleton
    public static class TestLabelProvider2 implements LabelProviderLoader
    {
        @Override
        public boolean isFileBased()
        {
            return false;
        }

        @Override
        public List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext)
        {
            LabelProviderData data = () -> {
                Label label = new Label(TestLabelProvider2.class.getSimpleName(), "label2Name", "label2Description");
                return Collections.singletonList(label);
            };

            LabelProviderMetadata metadata = new LabelMetadataBuilder("labelSet2ID", "labelSet2Description");

            LabelProvider provider = new LabelProviderBuilder(metadata, data);
            return Collections.singletonList(provider);
        }
    }
}
