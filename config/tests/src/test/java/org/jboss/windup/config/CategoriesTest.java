package org.jboss.windup.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.WindupRuleLoader;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.test.utils.EnumeratedRuleProviderFilter;
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
public class CategoriesTest
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
        .addPackage(EnumeratedRuleProviderFilter.class.getPackage())
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
        testProvider(TestTagsUnsetRuleProvider.class, "");
    }

    @Test
    public void testNoTags() throws IOException
    {
        testProvider(TestTagsEmptyRuleProvider.class, "");
    }

    @Test
    public void test1Tags() throws IOException
    {
        testProvider(TestTags1RuleProvider.class, "Foo");
    }

    @Test
    public void test2Tags() throws IOException
    {
        testProvider(TestTags2RuleProvider.class, "Foo, Bar");
    }

    @Test
    public void testEnhanceUnsetTags() throws IOException
    {
        testProvider(TestTagsEnhanceUnsetProvider.class, "");
    }

    @Test
    public void testEnhanceEmptyTags() throws IOException
    {
        testProvider(TestTagsEnhanceEmptyProvider.class, "");
    }

    @Test
    public void testEnhanceStringTags() throws IOException
    {
        testProvider(TestTagsEnhanceStringRuleProvider.class, "Foo");
    }

    @Test
    public void testEnhanceJoinedTags() throws IOException
    {
        testProvider(TestTagsEnhanceJoinedStringRuleProvider.class, "Foo, Bar");
    }

    @Test
    public void testEnhanceCollectionTags() throws IOException
    {
            testProvider(TestTagsEnhanceCollectionRuleProvider.class, "Foo, Bar");
    }



    private void testProvider(final Class<? extends WindupRuleProvider> provider, final String expectedCatsStr) throws IOException
    {
        String name = TestTagsUnsetRuleProvider.class.getSimpleName();

        //final Set<String> expectedCats = new HashSet(Arrays.asList(expectedCatsStr.split("\\s*,\\s*+")));
        final Set<String> expectedCats = new HashSet(Arrays.asList(StringUtils.split(expectedCatsStr, ", ")));

        try (GraphContext context = factory.create())
        {
            Predicate<WindupRuleProvider> rulesToRun = new EnumeratedRuleProviderFilter(provider);

            Configuration conf = loader.loadConfiguration(context, rulesToRun).getConfiguration();

            Assert.assertTrue("Rule loaded", !conf.getRules().isEmpty());

            // As wierd as it is, the object itself bears the behavior of a map, which is referred to as "context".
            Assert.assertTrue("Rule instanceof Context", conf.getRules().get(0) instanceof Context);
            Context subRule = (Context) conf.getRules().get(0);

            Object tag = subRule.get(RuleMetadata.TAGS);
            Assert.assertTrue("TAGS is set in the Context: " + tag, tag != null);
            Assert.assertTrue("Tags is a Set: " + tag.getClass(), tag instanceof Set);
            Set<String> cats = (Set<String>)tag;
            Assert.assertEquals("" + expectedCats.size() + " tags in " + name, expectedCats.size(), cats.size());
            for (String expCat : expectedCats)
                Assert.assertTrue(name + " has tag " + expCat, cats.contains(expCat));
        }
        catch(Exception ex) {
            if(ex instanceof InvocationTargetException)
                throw new WindupException("" + ((InvocationTargetException)ex).getTargetException(), ex);
            throw ex;
        }
    }



    // RuleProviders

    public static class TestTagsUnsetRuleProvider extends TestTagsRuleProviderBase
    {
        @Override
        public Collection<String> getCategories()
        {
            return Collections.EMPTY_LIST;
        }
    }

    public static class TestTagsEmptyRuleProvider extends TestTagsRuleProviderBase
    {
        @Override
        public Collection<String> getCategories()
        {
            return Collections.EMPTY_LIST;
        }
    }

    public static class TestTags1RuleProvider extends TestTagsRuleProviderBase
    {
        @Override
        public Collection<String> getCategories()
        {
            return Arrays.asList("Foo");
        }
    }

    public static class TestTags2RuleProvider extends TestTagsRuleProviderBase
    {
        @Override
        public Collection<String> getCategories()
        {
            return Arrays.asList("Foo", "Bar");
        }
    }

    public static class TestTagsEnhanceStringRuleProvider extends TestTagsRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
            super.enhanceMetadata(context);
            context.put(RuleMetadata.TAGS, "Foo");
        }
    }

    public static class TestTagsEnhanceJoinedStringRuleProvider extends TestTagsRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
            super.enhanceMetadata(context);
            context.put(RuleMetadata.TAGS, "Foo, Bar");
        }
    }

    public static class TestTagsEnhanceCollectionRuleProvider extends TestTagsRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
            super.enhanceMetadata(context);
            context.put(RuleMetadata.TAGS, Arrays.asList("Foo", "Bar"));
        }
    }

    public static class TestTagsEnhanceEmptyProvider extends TestTagsRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
            super.enhanceMetadata(context);
            context.put(RuleMetadata.TAGS, "");
        }
    }

    public static class TestTagsEnhanceUnsetProvider extends TestTagsRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
        }
    }



    // A base for those above.
    public static class TestTagsRuleProviderBase extends WindupRuleProvider
    {
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
            .addRule(new Rule(){

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
    }// TestTagsRuleProviderBase

}
