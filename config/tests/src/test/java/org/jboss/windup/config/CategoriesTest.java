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
    public void testUnsetCategory() throws IOException
    {
        testProvider(TestCategoryUnsetRuleProvider.class, "");
    }

    @Test
    public void testNoCategory() throws IOException
    {
        testProvider(TestCategoryEmptyRuleProvider.class, "");
    }

    @Test
    public void test1Category() throws IOException
    {
        testProvider(TestCategory1RuleProvider.class, "Foo");
    }

    @Test
    public void test2Category() throws IOException
    {
        testProvider(TestCategory2RuleProvider.class, "Foo, Bar");
    }

    @Test
    public void testEnhanceUnsetCategory() throws IOException
    {
        testProvider(TestCategoryEnhanceUnsetProvider.class, "");
    }

    @Test
    public void testEnhanceEmptyCategory() throws IOException
    {
        testProvider(TestCategoryEnhanceEmptyProvider.class, "");
    }

    @Test
    public void testEnhanceStringCategory() throws IOException
    {
        testProvider(TestCategoryEnhanceStringRuleProvider.class, "Foo");
    }

    @Test
    public void testEnhanceJoinedCategory() throws IOException
    {
        testProvider(TestCategoryEnhanceJoinedStringRuleProvider.class, "Foo, Bar");
    }

    @Test
    public void testEnhanceCollectionCategory() throws IOException
    {
            testProvider(TestCategoryEnhanceCollectionRuleProvider.class, "Foo, Bar");
    }



    private void testProvider(final Class<? extends WindupRuleProvider> provider, final String expectedCatsStr) throws IOException
    {
        String name = TestCategoryUnsetRuleProvider.class.getSimpleName();

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

            Object category = subRule.get(RuleMetadata.CATEGORY);
            Assert.assertTrue("CATEGORY is set in the Context: " + category, category != null);
            Assert.assertTrue("Category is a Set: " + category.getClass(), category instanceof Set);
            Set<String> cats = (Set<String>)category;
            Assert.assertEquals("" + expectedCats.size() + " categories in " + name, expectedCats.size(), cats.size());
            for (String expCat : expectedCats)
                Assert.assertTrue(name + " has category " + expCat, cats.contains(expCat));
        }
        catch(Exception ex) {
            if(ex instanceof InvocationTargetException)
                throw new WindupException("" + ((InvocationTargetException)ex).getTargetException(), ex);
            throw ex;
        }
    }



    // RuleProviders

    public static class TestCategoryUnsetRuleProvider extends TestCategoryRuleProviderBase
    {
        @Override
        public Collection<String> getCategories()
        {
            return Collections.EMPTY_LIST;
        }
    }

    public static class TestCategoryEmptyRuleProvider extends TestCategoryRuleProviderBase
    {
        @Override
        public Collection<String> getCategories()
        {
            return Collections.EMPTY_LIST;
        }
    }

    public static class TestCategory1RuleProvider extends TestCategoryRuleProviderBase
    {
        @Override
        public Collection<String> getCategories()
        {
            return Arrays.asList("Foo");
        }
    }

    public static class TestCategory2RuleProvider extends TestCategoryRuleProviderBase
    {
        @Override
        public Collection<String> getCategories()
        {
            return Arrays.asList("Foo", "Bar");
        }
    }

    public static class TestCategoryEnhanceStringRuleProvider extends TestCategoryRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
            super.enhanceMetadata(context);
            context.put(RuleMetadata.CATEGORY, "Foo");
        }
    }

    public static class TestCategoryEnhanceJoinedStringRuleProvider extends TestCategoryRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
            super.enhanceMetadata(context);
            context.put(RuleMetadata.CATEGORY, "Foo, Bar");
        }
    }

    public static class TestCategoryEnhanceCollectionRuleProvider extends TestCategoryRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
            super.enhanceMetadata(context);
            context.put(RuleMetadata.CATEGORY, Arrays.asList("Foo", "Bar"));
        }
    }

    public static class TestCategoryEnhanceEmptyProvider extends TestCategoryRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
            super.enhanceMetadata(context);
            context.put(RuleMetadata.CATEGORY, "");
        }
    }

    public static class TestCategoryEnhanceUnsetProvider extends TestCategoryRuleProviderBase
    {
        @Override
        public void enhanceMetadata(Context context)
        {
        }
    }



    // A base for those above.
    public static class TestCategoryRuleProviderBase extends WindupRuleProvider
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
    }// TestCategoryRuleProviderBase

}
