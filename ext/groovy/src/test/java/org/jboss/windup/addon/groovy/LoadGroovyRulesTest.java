package org.jboss.windup.addon.groovy;

import org.jboss.windup.ext.groovy.GroovyConfigurationProvider;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Rule;

@RunWith(Arquillian.class)
/**
 * 
 */
@Ignore
public class LoadGroovyRulesTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.windup.engine.ext:groovy"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.windup.engine.ext:groovy"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Inject
    private GroovyConfigurationProvider provider;

    @Test
    public void testLoadGroovyConfigs() throws Exception
    {
        Configuration configuration = provider.getConfiguration(context);
        List<Rule> rules = configuration.getRules();

        Assert.assertEquals(1, rules.size());
    }
}