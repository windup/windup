package org.jboss.windup.addon.config;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.addon.config.example.people.PersonConfigurationProvider;
import org.jboss.windup.addon.config.runner.DefaultEvaluationContext;
import org.jboss.windup.addon.config.spi.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextImpl;
import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class PersonConfigurationTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.addon:config"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addPackages(true, PersonConfigurationProvider.class.getPackage())
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.addon:config"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private PersonConfigurationProvider provider;

    @Inject
    private GraphTypeRegistry graphTypeRegistry;

    @Inject
    private SelectionFactory factory;

    @Test
    public void testRunWindup() throws Exception
    {
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry);
        GraphRewrite event = new GraphRewrite(context);

        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();

        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);

        event.getRewriteContext().put(SelectionFactory.class, factory);

        Configuration configuration = provider.getConfiguration(context);
        Subset.evaluate(configuration).perform(event, evaluationContext);

        Assert.assertTrue(provider.isPersonFound());
    }
}