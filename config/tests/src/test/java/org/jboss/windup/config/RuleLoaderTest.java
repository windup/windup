package org.jboss.windup.config;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
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
public class RuleLoaderTest
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
    private GraphContextFactory factory;
    @Inject
    private RuleLoader loader;

    @Test
    public void testRuleProviderWithFilter() throws IOException
    {
        try (GraphContext context = factory.create(true))
        {
            Predicate<RuleProvider> filter = (provider) -> provider.getMetadata().getPhase() == MigrationRulesPhase.class;

            RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), filter);
            Configuration configuration1 = loader.loadConfiguration(ruleLoaderContext).getConfiguration();
            boolean found1 = false;
            boolean found2 = false;
            for (Rule rule : configuration1.getRules())
            {
                if (rule.getId().equals(TestRuleProvider1Phase.class.getSimpleName()))
                {
                    found1 = true;
                }
                else if (rule.getId().equals(TestRuleProvider2Phase.class.getSimpleName()))
                {
                    found2 = true;
                }
            }
            Assert.assertTrue(found1);
            Assert.assertFalse(found2);
        }
    }

    @Singleton
    public static class TestRuleProvider1Phase extends AbstractRuleProvider
    {
        public TestRuleProvider1Phase()
        {
            super(MetadataBuilder.forProvider(TestRuleProvider1Phase.class, "TestRuleProvider1Phase"));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
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
                                return TestRuleProvider1Phase.class.getSimpleName();
                            }
                        });
        }
    }

    @Singleton
    public static class TestRuleProvider2Phase extends AbstractRuleProvider
    {
        public TestRuleProvider2Phase()
        {
            super(MetadataBuilder.forProvider(TestRuleProvider2Phase.class, "TestRuleProvider2Phase")
                        .setPhase(ArchiveExtractionPhase.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
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
                                return TestRuleProvider2Phase.class.getSimpleName();
                            }
                        });
        }
    }
}
