package org.jboss.windup.config.metadata;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.engine.predicates.EnumeratedRuleProviderPredicate;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class TagsMetadataTest {

    @Inject
    private GraphContextFactory factory;
    @Inject
    private RuleLoader loader;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addPackage(EnumeratedRuleProviderPredicate.class.getPackage());
        return archive;
    }

    @Test
    public void testUnsetTags() throws Exception {
        testProvider(TestTagsUnsetRuleProvider.class);
    }

    @Test
    public void testNoTags() throws Exception {
        testProvider(TestTagsEmptyRuleProvider.class);
    }

    @Test
    public void test1Tags() throws Exception {
        testProvider(TestTags1RuleProvider.class, "Foo");
    }

    @Test
    public void test2Tags() throws Exception {
        testProvider(TestTags2RuleProvider.class, "Foo", "Bar");
    }

    private void testProvider(final Class<? extends RuleProvider> provider, final String... expectedTags) throws Exception {
        Set<String> expected;
        if (expectedTags == null) {
            expected = new HashSet<>();
        } else {
            expected = new HashSet<>();
            for (String t : expectedTags) {
                expected.add(t);
            }
        }

        Predicate<RuleProvider> rulesToRun = new EnumeratedRuleProviderPredicate(provider);

        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), rulesToRun);
        Configuration config = loader.loadConfiguration(ruleLoaderContext).getConfiguration();

        Assert.assertTrue("Rule loaded", !config.getRules().isEmpty());
        Assert.assertTrue("Rule instanceof Context", config.getRules().get(0) instanceof Context);

        Context rule = (Context) config.getRules().get(0);

        @SuppressWarnings("unchecked")
        Set<String> tags = (Set<String>) rule.get(RuleMetadataType.TAGS);
        Assert.assertNotNull(tags);
        Assert.assertTrue(tags instanceof Set);
        Assert.assertEquals(expected.size(), tags.size());
        for (String tag : expected) {
            Assert.assertTrue(tags.contains(tag));
        }
    }

    /*
     * Test RuleProviders
     */
    public static class TestTagsUnsetRuleProvider extends TestTagsRuleProviderBase {
        public TestTagsUnsetRuleProvider() {
            super(MetadataBuilder.forProvider(TestTagsUnsetRuleProvider.class));
        }
    }

    public static class TestTagsEmptyRuleProvider extends TestTagsRuleProviderBase {
        public TestTagsEmptyRuleProvider() {
            super(MetadataBuilder.forProvider(TestTagsEmptyRuleProvider.class).addTags("", "", ""));
        }
    }

    public static class TestTags1RuleProvider extends TestTagsRuleProviderBase {
        public TestTags1RuleProvider() {
            super(MetadataBuilder.forProvider(TestTags1RuleProvider.class).addTags("Foo"));
        }
    }

    public static class TestTags2RuleProvider extends TestTagsRuleProviderBase {
        public TestTags2RuleProvider() {
            super(MetadataBuilder.forProvider(TestTags2RuleProvider.class).addTags("Foo", "Bar"));
        }
    }

    public abstract static class TestTagsRuleProviderBase extends AbstractRuleProvider {
        public TestTagsRuleProviderBase(RuleProviderMetadata metadata) {
            super(metadata);
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule(new Rule() {

                        @Override
                        public String getId() {
                            return this.getClass().getSimpleName();
                        }

                        @Override
                        public boolean evaluate(Rewrite rewrite, EvaluationContext context) {
                            return true;
                        }

                        @Override
                        public void perform(Rewrite rewrite, EvaluationContext context) {
                        }

                    });
        }
    }

}