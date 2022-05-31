package org.jboss.windup.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationLoader;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;

@RunWith(Arquillian.class)
public class ReadXMLConfigurationTest {

    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Test
    public void testRunWindup() throws Exception {
        final Path folder = File.createTempFile("windupGraph", "").toPath();
        try (final GraphContext context = factory.create(folder, true)) {
            RuleLoaderContext ruleLoaderContext = new RuleLoaderContext();
            final ConfigurationLoader loader = ConfigurationLoader.create(ruleLoaderContext);
            final Configuration configuration = loader.loadConfiguration(ruleLoaderContext);

            final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();

            final DefaultParameterValueStore values = new DefaultParameterValueStore();
            evaluationContext.put(ParameterValueStore.class, values);

            Subset.evaluate(configuration).perform(new GraphRewrite(context), evaluationContext);
        }
    }
}