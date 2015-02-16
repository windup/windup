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
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.WindupRuleProviderLoader;
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
public class WindupRuleProviderLoaderTest
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
    private Imported<WindupRuleProviderLoader> loaders;

    @Test
    public void testRuleProviderWithFilter() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            boolean foundProvider1 = false;
            boolean foundProvider2 = false;

            for (WindupRuleProviderLoader loader : loaders)
            {
                for (WindupRuleProvider provider : loader.getProviders(context))
                {
                    if (provider instanceof TestRuleProvider1)
                    {
                        Assert.assertEquals("_DEFAULT_:org.jboss.windup.config.WindupRuleProviderLoaderTest.TestRuleProvider1", provider.getOrigin());
                        foundProvider1 = true;
                    }
                    else if (provider instanceof TestRuleProvider2)
                    {
                        Assert.assertEquals("_DEFAULT_:org.jboss.windup.config.WindupRuleProviderLoaderTest.TestRuleProvider2", provider.getOrigin());
                        foundProvider2 = true;
                    }
                }
            }
            Assert.assertTrue(foundProvider1);
            Assert.assertTrue(foundProvider2);
        }
    }

    @Singleton
    public static class TestRuleProvider1 extends WindupRuleProvider
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
                                return TestRuleProvider1.class.getSimpleName();
                            }
                        });

        }
    }

    @Singleton
    public static class TestRuleProvider2 extends WindupRuleProvider
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
                                return TestRuleProvider2.class.getSimpleName();
                            }
                        });

        }
    }
}
