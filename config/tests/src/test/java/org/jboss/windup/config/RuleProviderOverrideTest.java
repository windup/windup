package org.jboss.windup.config;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.Technology;
import org.jboss.windup.exec.rulefilters.SourceAndTargetPredicate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

@RunWith(Arquillian.class)
public class RuleProviderOverrideTest {

    @Inject
    private RuleLoader loader;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Test
    public void testOverride() {
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext();
        Configuration configuration = loader.loadConfiguration(ruleLoaderContext).getConfiguration();
        int count = 0;
        boolean foundTestOverrideProvider = false;
        boolean foundTestOriginalWithTargetProvider = false;
        boolean foundTestBarTargetWithTargetProvider = false;
        for (Rule rule : configuration.getRules()) {
            count++;
            if (rule.toString().contains("(RuleOverride)")) foundTestOverrideProvider = true;
            if (rule.toString().contains("(RuleOverrideWithTarget)")) foundTestOriginalWithTargetProvider = true;
            if (rule.toString().contains("(TestBarTargetWithTargetProvider)")) foundTestBarTargetWithTargetProvider = true;
        }
        Assert.assertTrue("RuleOverride", foundTestOverrideProvider);
        Assert.assertTrue("RuleOverrideWithTarget", foundTestOriginalWithTargetProvider);
        Assert.assertTrue("TestBarTargetWithTargetProvider", foundTestBarTargetWithTargetProvider);
        Assert.assertEquals(3, count);
    }

    @Test
    public void testOverrideWithTarget() {
        final SourceAndTargetPredicate targetPredicate = new SourceAndTargetPredicate(Collections.emptyList(), List.of("test-target"));
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), targetPredicate);
        Configuration configuration = loader.loadConfiguration(ruleLoaderContext).getConfiguration();
        int count = 0;
        boolean foundTestOverrideProvider = false;
        boolean foundTestOverrideWithTargetProvider = false;
        for (Rule rule : configuration.getRules()) {
            count++;
            if (rule.toString().contains("(RuleOverride)")) foundTestOverrideProvider = true;
            if (rule.toString().contains("(RuleOverrideWithTarget)")) foundTestOverrideWithTargetProvider = true;
        }
        Assert.assertTrue("RuleOverride", foundTestOverrideProvider);
        Assert.assertTrue("RuleOverrideWithTarget", foundTestOverrideWithTargetProvider);
        Assert.assertEquals(2, count);
    }

    @Test
    public void testOverrideWithAnotherTarget() {
        final SourceAndTargetPredicate targetPredicate = new SourceAndTargetPredicate(Collections.emptyList(), List.of("another-target"));
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), targetPredicate);
        Configuration configuration = loader.loadConfiguration(ruleLoaderContext).getConfiguration();
        int count = 0;
        boolean foundTestOverrideProvider = false;
        boolean foundTestOverrideWithTargetProvider = false;
        for (Rule rule : configuration.getRules()) {
            count++;
            if (rule.toString().contains("(RuleOverride)")) foundTestOverrideProvider = true;
            if (rule.toString().contains("(OriginalRuleWithTarget)")) foundTestOverrideWithTargetProvider = true;
        }
        Assert.assertTrue("RuleOverride", foundTestOverrideProvider);
        Assert.assertTrue("OriginalRuleWithTarget", foundTestOverrideWithTargetProvider);
        Assert.assertEquals(2, count);
    }

    // https://issues.redhat.com/browse/WINDUP-3928
    @Test
    public void testOverridingRulesetExecutedWithoutOriginalRulesetInScopeForAnalysis() {
        final SourceAndTargetPredicate targetPredicate = new SourceAndTargetPredicate(Collections.emptyList(), List.of("bar-target"));
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), targetPredicate);
        Configuration configuration = loader.loadConfiguration(ruleLoaderContext).getConfiguration();
        int count = 0;
        boolean foundTestOverrideProvider = false;
        boolean foundTestOverrideWithTargetProvider = false;
        boolean foundTestBarTargetWithTargetProvider = false;
        for (Rule rule : configuration.getRules()) {
            count++;
            if (rule.toString().contains("(RuleOverride)")) foundTestOverrideProvider = true;
            if (rule.toString().contains("(OriginalRuleWithTarget)")) foundTestOverrideWithTargetProvider = true;
            if (rule.toString().contains("(TestBarTargetWithTargetProvider)")) foundTestBarTargetWithTargetProvider = true;
        }
        Assert.assertTrue("RuleOverride", foundTestOverrideProvider);
        Assert.assertTrue("OriginalRuleWithTarget", foundTestOverrideWithTargetProvider);
        Assert.assertTrue("TestBarTargetWithTargetProvider", foundTestBarTargetWithTargetProvider);
        Assert.assertEquals(3, count);
    }

    @Test
    public void testOverridingRulesetExecutedWithOriginalRulesetInScopeForAnalysis() {
        final SourceAndTargetPredicate targetPredicate = new SourceAndTargetPredicate(Collections.emptyList(), List.of("foo-target"));
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), targetPredicate);
        Configuration configuration = loader.loadConfiguration(ruleLoaderContext).getConfiguration();
        int count = 0;
        boolean foundTestOverrideProvider = false;
        boolean foundTestOverrideWithTargetProvider = false;
        boolean foundTestFooTargetedWithTargetProvider = false;
        for (Rule rule : configuration.getRules()) {
            count++;
            if (rule.toString().contains("(RuleOverride)")) foundTestOverrideProvider = true;
            if (rule.toString().contains("(OriginalRuleWithTarget)")) foundTestOverrideWithTargetProvider = true;
            if (rule.toString().contains("(TestFooTargetedWithTargetProvider)")) foundTestFooTargetedWithTargetProvider = true;
        }
        Assert.assertTrue("RuleOverride", foundTestOverrideProvider);
        Assert.assertTrue("OriginalRuleWithTarget", foundTestOverrideWithTargetProvider);
        Assert.assertTrue("TestFooTargetedWithTargetProvider", foundTestFooTargetedWithTargetProvider);
        Assert.assertEquals(3, count);
    }

    @Singleton
    public static class TestOriginalProvider extends AbstractRuleProvider {
        public TestOriginalProvider() {
            super(MetadataBuilder.forProvider(TestOriginalProvider.class, "TestRuleProvider"));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule(new Rule() {
                        @Override
                        public void perform(Rewrite event, EvaluationContext context) {
                        }

                        @Override
                        public boolean evaluate(Rewrite event, EvaluationContext context) {
                            return true;
                        }

                        @Override
                        public String getId() {
                            return TestOriginalProvider.class.getSimpleName();
                        }

                        @Override
                        public String toString() {
                            return "OriginalRule";
                        }
                    });
        }
    }
    @Singleton
    public static class TestOverrideProvider extends AbstractRuleProvider {
        public TestOverrideProvider() {
            super(MetadataBuilder.forProvider(TestOverrideProvider.class, "TestRuleProvider").setOverrideProvider(true));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule(new Rule() {
                        @Override
                        public void perform(Rewrite event, EvaluationContext context) {
                        }

                        @Override
                        public boolean evaluate(Rewrite event, EvaluationContext context) {
                            return true;
                        }

                        @Override
                        public String getId() {
                            return TestOriginalProvider.class.getSimpleName();
                        }

                        @Override
                        public String toString() {
                            return "RuleOverride";
                        }
                    });
        }
    }

    @Singleton
    public static class TestOriginalWithTargetProvider extends AbstractRuleProvider {
        public TestOriginalWithTargetProvider() {
            super(MetadataBuilder.forProvider(TestOriginalWithTargetProvider.class, "TestRuleProviderWithTarget"));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule(new Rule() {
                        @Override
                        public void perform(Rewrite event, EvaluationContext context) {
                        }

                        @Override
                        public boolean evaluate(Rewrite event, EvaluationContext context) {
                            return true;
                        }

                        @Override
                        public String getId() {
                            return TestOriginalWithTargetProvider.class.getSimpleName();
                        }

                        @Override
                        public String toString() {
                            return "OriginalRuleWithTarget";
                        }
                    });
        }
    }

    @Singleton
    @RuleMetadata(targetTechnologies = {@Technology(id = "test-target")})
    public static class TestOverrideWithTargetProvider extends AbstractRuleProvider {
        public TestOverrideWithTargetProvider() {
            super(MetadataBuilder.forProvider(TestOverrideWithTargetProvider.class, "TestRuleProviderWithTarget").setOverrideProvider(true));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule(new Rule() {
                        @Override
                        public void perform(Rewrite event, EvaluationContext context) {
                        }

                        @Override
                        public boolean evaluate(Rewrite event, EvaluationContext context) {
                            return true;
                        }

                        @Override
                        public String getId() {
                            return TestOriginalWithTargetProvider.class.getSimpleName();
                        }

                        @Override
                        public String toString() {
                            return "RuleOverrideWithTarget";
                        }
                    });
        }
    }

    @Singleton
    @RuleMetadata(targetTechnologies = {@Technology(id = "foo-target")})
    public static class TestFooTargetedWithTargetProvider extends AbstractRuleProvider {
        public TestFooTargetedWithTargetProvider() {
            super(MetadataBuilder.forProvider(TestFooTargetedWithTargetProvider.class, "TestFooTargetedWithTargetProvider"));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule(new Rule() {
                        @Override
                        public void perform(Rewrite event, EvaluationContext context) {
                        }

                        @Override
                        public boolean evaluate(Rewrite event, EvaluationContext context) {
                            return true;
                        }

                        @Override
                        public String getId() {
                            return TestFooTargetedWithTargetProvider.class.getSimpleName();
                        }

                        @Override
                        public String toString() {
                            return "TestFooTargetedWithTargetProvider";
                        }
                    });
        }
    }

    @Singleton
    @RuleMetadata(targetTechnologies = {@Technology(id = "bar-target")})
    public static class TestBarTargetWithTargetProvider extends AbstractRuleProvider {
        public TestBarTargetWithTargetProvider() {
            super(MetadataBuilder.forProvider(TestBarTargetWithTargetProvider.class, "TestFooTargetedWithTargetProvider").setOverrideProvider(true));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule(new Rule() {
                        @Override
                        public void perform(Rewrite event, EvaluationContext context) {
                        }

                        @Override
                        public boolean evaluate(Rewrite event, EvaluationContext context) {
                            return true;
                        }

                        @Override
                        public String getId() {
                            return TestFooTargetedWithTargetProvider.class.getSimpleName();
                        }

                        @Override
                        public String toString() {
                            return "TestBarTargetWithTargetProvider";
                        }
                    });
        }
    }
}
