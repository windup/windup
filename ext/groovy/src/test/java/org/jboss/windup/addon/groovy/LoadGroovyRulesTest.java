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
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.loader.WindupRuleProviderLoader;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.RuleBuilder;
import org.ocpsoft.rewrite.context.Context;

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
    public void testGroovyRuleProviderFactory() throws Exception
    {
        Assert.assertNotNull(furnace);

        Imported<WindupRuleProviderLoader> loaders = furnace.getAddonRegistry().getServices(
                    WindupRuleProviderLoader.class);

        Assert.assertNotNull(loaders);

        List<WindupRuleProvider> allProviders = new ArrayList<WindupRuleProvider>();
        for (WindupRuleProviderLoader loader : loaders)
        {
            allProviders.addAll(loader.getProviders());
        }

        boolean foundScriptPath = false;
        for (WindupRuleProvider provider : allProviders)
        {
            Context context = RuleBuilder.define();
            provider.enhanceMetadata(context);
            System.out.println(context.get(RuleMetadata.ORIGIN));
            if (((String) context.get(RuleMetadata.ORIGIN)).contains("org/jboss/windup/addon/groovy/GroovyExampleRule.windup.groovy"))
            {
                foundScriptPath = true;
                break;
            }
        }
        Assert.assertTrue("Script path should have been set in Rule Metatada", foundScriptPath);
        Assert.assertTrue(allProviders.size() > 0);
    }
}
