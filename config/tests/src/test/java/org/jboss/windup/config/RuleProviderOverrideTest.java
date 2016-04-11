package org.jboss.windup.config;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.config.metadata.MetadataBuilder;
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
public class RuleProviderOverrideTest
{

    @Inject
    private GraphContextFactory factory;
    @Inject
    private RuleLoader loader;

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Test
    public void testOverride() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            Predicate<RuleProvider> predicate = new Predicate<RuleProvider>()
            {
                @Override
                public boolean accept(RuleProvider provider)
                {
                    return true;
                }
            };

            Configuration configuration = loader.loadConfiguration(context, predicate).getConfiguration();
            int count = 0;
            for (Rule rule : configuration.getRules())
            {
                count++;
                Assert.assertTrue("Override", rule.toString().contains("RuleOverride"));
            }
            Assert.assertEquals(1, count);
        }
    }

    @Singleton
    public static class TestOriginalProvider extends AbstractRuleProvider
    {
        public TestOriginalProvider()
        {
            super(MetadataBuilder.forProvider(TestOriginalProvider.class, "TestRuleProvider"));
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
                    .addRule(new Rule()
                    {
                        @Override
                        public void perform(Rewrite event, EvaluationContext context)
                        {
                        }

                        @Override
                        public boolean evaluate(Rewrite event, EvaluationContext context)
                        {
                            return true;
                        }

                        @Override
                        public String getId()
                        {
                            return TestOriginalProvider.class.getSimpleName();
                        }

                        @Override
                        public String toString() {
                            return "OriginalRule";
                        }
                    });
        }

        @Singleton
        public static class TestOverrideProvider extends AbstractRuleProvider {
            public TestOverrideProvider() {
                super(MetadataBuilder.forProvider(TestOverrideProvider.class, "TestRuleProvider").setOverrideProvider(true));
            }

            @Override
            public Configuration getConfiguration(GraphContext context) {
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
    }
}
