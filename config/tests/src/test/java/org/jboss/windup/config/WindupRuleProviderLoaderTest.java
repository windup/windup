package org.jboss.windup.config;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.loader.RuleProviderLoader;
import org.jboss.windup.config.metadata.MetadataBuilder;
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
public class WindupRuleProviderLoaderTest {

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
    private GraphContextFactory factory;
    @Inject
    private Imported<RuleProviderLoader> loaders;

    @Test
    public void testRuleProviderWithFilter() throws IOException {
        boolean foundProvider1 = false;
        boolean foundProvider2 = false;

        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext();
        for (RuleProviderLoader loader : loaders) {
            for (RuleProvider provider : loader.getProviders(ruleLoaderContext)) {
                if (provider instanceof TestRuleProvider1) {
                    Assert.assertTrue(provider.getMetadata().getOrigin()
                            .contains("org.jboss.windup.config.WindupRuleProviderLoaderTest$TestRuleProvider1"));
                    Assert.assertTrue(provider.getMetadata().getOrigin().contains("_DEFAULT_"));
                    foundProvider1 = true;
                } else if (provider instanceof TestRuleProvider2) {
                    Assert.assertTrue(provider.getMetadata().getOrigin()
                            .contains("org.jboss.windup.config.WindupRuleProviderLoaderTest$TestRuleProvider2"));
                    Assert.assertTrue(provider.getMetadata().getOrigin().contains("_DEFAULT_"));
                    foundProvider2 = true;
                }
            }
        }
        Assert.assertTrue(foundProvider1);
        Assert.assertTrue(foundProvider2);
    }

    @Singleton
    public static class TestRuleProvider1 extends AbstractRuleProvider {
        public TestRuleProvider1() {
            super(MetadataBuilder.forProvider(TestRuleProvider1.class, "TestRuleProvider1"));
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
                            return TestRuleProvider1.class.getSimpleName();
                        }
                    });
        }
    }

    @Singleton
    public static class TestRuleProvider2 extends AbstractRuleProvider {
        public TestRuleProvider2() {
            super(MetadataBuilder.forProvider(TestRuleProvider2.class, "TestRuleProvider2"));
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
                            return TestRuleProvider2.class.getSimpleName();
                        }
                    });
        }
    }
}
