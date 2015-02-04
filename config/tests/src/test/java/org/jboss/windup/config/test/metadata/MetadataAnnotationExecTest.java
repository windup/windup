package org.jboss.windup.config.test.metadata;

import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.testutils.rulefilters.EnumerationOfRulesFilter;
import org.jboss.windup.testutils.rulefilters.RuleFilter;
import org.jboss.windup.util.Logging;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for the Victims ruleset.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class MetadataAnnotationExecTest
{
    private static final Logger log = Logging.get(MetadataAnnotationExecTest.class);

    @Deployment
    @Dependencies({
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
        @AddonDependency(name = "org.jboss.windup.utils:utils"),
        @AddonDependency(name = "org.jboss.windup.config:windup-config"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:rules-base"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
        @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
            .addBeansXML()
            //.addClasses(TestMetadataAnnotationExecRuleProvider.class)
            .addPackage(org.jboss.windup.config.test.metadata.MetadataAnnotationExecTest.class.getPackage())
            .addPackage(RuleFilter.class.getPackage())
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-base"),
                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec")
            );
        return archive;
    }


    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    @Test
    public void testMetadataPickedUp()
    {
        try (GraphContext grCtx = contextFactory.create())
        {
            // Create a project.


            // Windup config.
            WindupConfiguration wc = new WindupConfiguration();
            wc.setGraphContext(grCtx);
            // Only run the tested rules and those they need.
            wc.setRuleProviderFilter(
                new EnumerationOfRulesFilter(TestMetadataAnnotationExecRuleProvider.class)
            );
            wc.setInputPath(Paths.get("src/test/resources/empty.war"));
            wc.setOutputDirectory(Paths.get("target/WindupReport"));

            // Run.
            processor.execute(wc);

            // The validation happens inside of the RuleProvider.
        }
        catch (Throwable ex)
        {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

}
