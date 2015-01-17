package org.jboss.windup.config;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.WindupRuleLoader;
import org.jboss.windup.config.phase.ArchiveExtraction;
import org.jboss.windup.config.phase.MigrationRules;
import org.jboss.windup.config.phase.RulePhase;
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
public class WindupConfigurationLoaderTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContextFactory factory;
    @Inject
    private WindupRuleLoader loader;

    @Test
    public void testRuleProviderWithFilter() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            Predicate<WindupRuleProvider> predicate = new Predicate<WindupRuleProvider>()
            {
                @Override
                public boolean accept(WindupRuleProvider arg0)
                {
                    return arg0.getPhase() == MigrationRules.class;
                }
            };

            GraphRewrite event = new GraphRewrite(context);
            Configuration configuration1 = loader.loadConfiguration(context, predicate).getConfiguration();
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
    public static class TestRuleProvider1Phase extends WindupRuleProvider
    {
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
                                return TestRuleProvider1Phase.class.getSimpleName();
                            }
                        });

        }
    }

    @Singleton
    public static class TestRuleProvider2Phase extends WindupRuleProvider
    {
        @Override
        public Class<? extends RulePhase> getPhase()
        {
            return ArchiveExtraction.class;
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
                                return TestRuleProvider2Phase.class.getSimpleName();
                            }
                        });

        }
    }
}
