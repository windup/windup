package org.jboss.windup.addon.groovy;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.loader.WindupConfigurationProviderLoader;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Rule;

@RunWith(Arquillian.class)
/**
 * 
 */
public class LoadGroovyRulesTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.windup.ext:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Inject
    private Furnace furnace;

    @Test
    public void testGroovyConfigurationProviderFactory() throws Exception
    {
        Assert.assertNotNull(furnace);

        Imported<WindupConfigurationProviderLoader> loaders = furnace.getAddonRegistry().getServices(
                    WindupConfigurationProviderLoader.class);

        Assert.assertNotNull(loaders);

        List<WindupConfigurationProvider> providers = new ArrayList<WindupConfigurationProvider>();
        for (WindupConfigurationProviderLoader loader : loaders)
        {
            providers.addAll(loader.getProviders());
        }

        Assert.assertTrue(providers.size() > 0);
        
        for (WindupConfigurationProvider provider : providers)
        {
            Configuration configuration = provider.getConfiguration(context);
            for (Rule rule : configuration.getRules())
            {
                System.out.println(rule);
            }
        }
        
        System.out.println("Done.");
    }
}
