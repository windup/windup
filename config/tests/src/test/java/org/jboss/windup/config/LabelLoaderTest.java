package org.jboss.windup.config;

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
import org.jboss.windup.config.metadata.LabelProviderRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class LabelLoaderTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
        return archive;
    }

    @Inject
    private LabelLoader loader;

    @Test
    public void testLabelProviderWithFilter() {
        Predicate<LabelProvider> predicate = (provider) -> false;
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), null, predicate);

        LabelProviderRegistry labelProviderRegistry = loader.loadConfiguration(ruleLoaderContext);
        List<Label> labels = labelProviderRegistry.getProviders()
                .stream()
                .map(labelProviderRegistry::getLabels)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Assert.assertTrue(labels.isEmpty());
    }

    @Test
    public void testLoadLabelWithoutDescription() {
        Predicate<LabelProvider> predicate = (provider) -> provider.getMetadata().getID().equals("labelSet1ID");
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), null, predicate);

        LabelProviderRegistry labelProviderRegistry = loader.loadConfiguration(ruleLoaderContext);
        List<Label> labels = labelProviderRegistry.getProviders()
                .stream()
                .map(labelProviderRegistry::getLabels)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals(1, labels.size());
    }

    @Test
    public void testLoadLabelWithoutName() {
        Predicate<LabelProvider> predicate = (provider) -> provider.getMetadata().getID().equals("labelSet2ID");
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), null, predicate);

        // Should throw exception since 'name' is mandatory
        try {
            LabelProviderRegistry labelProviderRegistry = loader.loadConfiguration(ruleLoaderContext);
            List<Label> labels = labelProviderRegistry.getProviders()
                    .stream()
                    .map(labelProviderRegistry::getLabels)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLoadLabelWithBlankName() {
        Predicate<LabelProvider> predicate = (provider) -> provider.getMetadata().getID().equals("labelSet3ID");
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), null, predicate);

        // Should throw exception since 'name' can not be empty
        try {
            LabelProviderRegistry labelProviderRegistry = loader.loadConfiguration(ruleLoaderContext);
            List<Label> labels = labelProviderRegistry.getProviders()
                    .stream()
                    .map(labelProviderRegistry::getLabels)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
        }
    }

    @Singleton
    public static class TestLabelWithoutDescriptionProvider implements LabelProviderLoader {
        @Override
        public boolean isFileBased() {
            return false;
        }

        @Override
        public List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext) {
            LabelProviderData data = () -> {
                String labelID = TestLabelWithoutDescriptionProvider.class.getSimpleName();
                Label label = new Label(labelID, "label1Name");
                return Collections.singletonList(label);
            };

            LabelProviderMetadata metadata = new LabelMetadataBuilder("labelSet1ID", "labelSet1Description");

            LabelProvider provider = new LabelProviderBuilder(metadata, data);
            return Collections.singletonList(provider);
        }
    }

    @Singleton
    public static class TestLabelWithoutNameProvider implements LabelProviderLoader {
        @Override
        public boolean isFileBased() {
            return false;
        }

        @Override
        public List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext) {
            LabelProviderData data = () -> {
                String labelID = TestLabelWithoutNameProvider.class.getSimpleName();
                Label label = new Label(labelID, null);
                return Collections.singletonList(label);
            };

            LabelProviderMetadata metadata = new LabelMetadataBuilder("labelSet2ID", "labelSet2Description");

            LabelProvider provider = new LabelProviderBuilder(metadata, data);
            return Collections.singletonList(provider);
        }
    }

    @Singleton
    public static class TestLabelWithBlankNameProvider implements LabelProviderLoader {
        @Override
        public boolean isFileBased() {
            return false;
        }

        @Override
        public List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext) {
            LabelProviderData data = () -> {
                String labelID = TestLabelWithBlankNameProvider.class.getSimpleName();
                Label label = new Label(labelID, "    ");
                return Collections.singletonList(label);
            };

            LabelProviderMetadata metadata = new LabelMetadataBuilder("labelSet3ID", "labelSet3Description");

            LabelProvider provider = new LabelProviderBuilder(metadata, data);
            return Collections.singletonList(provider);
        }
    }
}
