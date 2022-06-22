package org.jboss.windup.config.metadata;

import java.nio.file.Paths;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class RulesetMetadataTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClasses(ExecutedProviders.class,
                        MetadataTestRuleProvider1.class,
                        MetadataTestRuleProvider2.class,
                        MetadataTestRuleProvider3.class,
                        MetadataTestRuleProvider5.class,
                        MetadataTestRulesetMetadata.class);
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    @Test
    public void testMetadataPickedUp() throws Exception {
        try (GraphContext context = contextFactory.create(true)) {
            WindupConfiguration windupConfig = new WindupConfiguration();
            windupConfig.setGraphContext(context);

            windupConfig.setRuleProviderFilter(
                    new RuleProviderWithDependenciesPredicate(MetadataTestRuleProvider5.class));
            windupConfig.addInputPath(Paths.get("src/test/resources/empty.war"));
            windupConfig.setOutputDirectory(Paths.get("target/WindupReport"));

            processor.execute(windupConfig);

            Assert.assertEquals(4, ExecutedProviders.getProviders().size());
        }
    }
}