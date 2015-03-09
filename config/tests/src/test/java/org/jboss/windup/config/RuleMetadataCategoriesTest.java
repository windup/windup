package org.jboss.windup.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.WindupRuleLoader;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.jboss.windup.engine.predicates.EnumeratedRuleProviderPredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

@RunWith(Arquillian.class)
public class RuleMetadataCategoriesTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.windup.utils:utils"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addPackage(EnumeratedRuleProviderPredicate.class.getPackage())
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config")
                    );
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Inject
    private WindupRuleLoader loader;

    @Test
    public void testUnsetTags() throws IOException
    {
        testProvider(TestTagsUnsetRuleProvider.class);
    }

    @Test
    public void testNoTags() throws IOException
    {
        testProvider(TestTagsEmptyRuleProvider.class);
    }

    @Test
    public void test1Tags() throws IOException
    {
        testProvider(TestTags1RuleProvider.class, "Foo");
    }

    @Test
    public void test2Tags() throws IOException
    {
        testProvider(TestTags2RuleProvider.class, "Foo", "Bar");
    }

    private void testProvider(final Class<? extends RuleProvider> provider, final String... expectedTags) throws IOException
    {
        Set<String> expected;
        if (expectedTags == null)
        {
            expected = new HashSet<>();
        }
        else
        {
            expected = new HashSet<>();
            for (String t : expectedTags)
            {
                expected.add(t);
            }
        }

        try (GraphContext context = factory.create())
        {
            Predicate<RuleProvider> rulesToRun = new EnumeratedRuleProviderPredicate(provider);

            Configuration config = loader.loadConfiguration(context, rulesToRun).getConfiguration();

            Assert.assertTrue("Rule loaded", !config.getRules().isEmpty());
            Assert.assertTrue("Rule instanceof Context", config.getRules().get(0) instanceof Context);

            Context rule = (Context) config.getRules().get(0);

            @SuppressWarnings("unchecked")
            Set<String> tags = (Set<String>) rule.get(RuleMetadata.TAGS);
            Assert.assertNotNull(tags);
            Assert.assertTrue(tags instanceof Set);
            Assert.assertEquals(expected.size(), tags.size());
            for (String tag : expected)
            {
                Assert.assertTrue(tags.contains(tag));
            }
        }
        catch (Exception ex)
        {
            if (ex instanceof InvocationTargetException)
                throw new WindupException("" + ((InvocationTargetException) ex).getTargetException(), ex);
            throw ex;
        }
    }

    /*
     * Test RuleProviders
     */
    public static class TestTagsUnsetRuleProvider extends TestTagsRuleProviderBase
    {
        public TestTagsUnsetRuleProvider()
        {
            super(MetadataBuilder.forProvider(TestTagsUnsetRuleProvider.class));
        }
    }

    public static class TestTagsEmptyRuleProvider extends TestTagsRuleProviderBase
    {
        public TestTagsEmptyRuleProvider()
        {
            super(MetadataBuilder.forProvider(TestTagsEmptyRuleProvider.class).addTags("", "", ""));
        }
    }

    public static class TestTags1RuleProvider extends TestTagsRuleProviderBase
    {
        public TestTags1RuleProvider()
        {
            super(MetadataBuilder.forProvider(TestTags1RuleProvider.class).addTags("Foo"));
        }
    }

    public static class TestTags2RuleProvider extends TestTagsRuleProviderBase
    {
        public TestTags2RuleProvider()
        {
            super(MetadataBuilder.forProvider(TestTags2RuleProvider.class).addTags("Foo", "Bar"));
        }
    }

    public abstract static class TestTagsRuleProviderBase extends AbstractRuleProvider
    {
        public TestTagsRuleProviderBase(RuleProviderMetadata metadata)
        {
            super(metadata);
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
                        .addRule(new Rule()
                        {

                            @Override
                            public String getId()
                            {
                                return this.getClass().getSimpleName();
                            }

                            @Override
                            public boolean evaluate(Rewrite rewrite, EvaluationContext context)
                            {
                                return true;
                            }

                            @Override
                            public void perform(Rewrite rewrite, EvaluationContext context)
                            {
                            }

                        });
        }
    }

}