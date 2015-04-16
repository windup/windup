package org.jboss.windup.tooling;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class ExecutionBuilderTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup:windup-tooling"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap
                    .create(AddonArchive.class)
                    .addBeansXML();
    }

    @Inject
    private ExecutionBuilder builder;

    @Inject
    private TestProvider testProvider;

    @Test
    public void testExecutionBuilder()
    {
        Assert.assertNotNull(builder);
        Assert.assertNotNull(testProvider);

        Path input = Paths.get("../../test-files/src_example");
        Path output = getDefaultPath();

        ExecutionResults results = builder.begin(Paths.get("."))
                    .setInput(input)
                    .setOutput(output)
                    .includePackage("org.windup.examples.ejb.messagedriven")
                    .ignore("\\.class$")
                    .execute();

        Assert.assertNotNull(results.getClassifications());
        Assert.assertNotNull(results.getHints());
        Assert.assertTrue(results.getHints().iterator().hasNext());
        Assert.assertTrue(results.getClassifications().iterator().hasNext());
    }

    private Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup").resolve("execbuildertest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Singleton
    public static class TestProvider extends AbstractRuleProvider
    {
        public TestProvider()
        {
            super(MetadataBuilder.forProvider(TestProvider.class));
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
                        .addRule()
                        .when(JavaClass.references("javax.{*}"))
                        .perform(Hint.withText("References javax.*").withEffort(43)
                                    .and(Classification.as("References some javax stuff")));
        }
    }
}
