package org.jboss.windup.config;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.runner.DefaultEvaluationContext;
import org.jboss.windup.graph.GraphApiCompositeClassLoaderProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextImpl;
import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationLoader;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class ReadXMLConfigurationTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;

    @Inject
    private GraphTypeRegistry graphTypeRegistry;

    @Test
    public void testRunWindup() throws Exception
    {
        final File folder = File.createTempFile("windupGraph", "");
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry,
                    graphApiCompositeClassLoaderProvider);
        final ConfigurationLoader loader = ConfigurationLoader.create(context);
        final Configuration configuration = loader.loadConfiguration(context);

        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();

        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);

        Subset.evaluate(configuration).perform(new GraphRewrite(context), evaluationContext);
    }
}