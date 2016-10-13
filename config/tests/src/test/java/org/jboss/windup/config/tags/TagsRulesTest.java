package org.jboss.windup.config.tags;

import java.io.File;
import org.jboss.windup.config.selectables.*;
import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.True;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
@Ignore
public class TagsRulesTest
{
    @Deployment
    @AddonDependencies({
        @AddonDependency(name = "org.jboss.windup.config:windup-config"),
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        final AddonArchive archive = ShrinkWrap
            .create(AddonArchive.class)
            .addClasses(TestIterationPayloadTestRuleProvider.class, TestChildModel.class, TestParentModel.class)
            .addBeansXML()
            .addAsResource(new File("src/test/java/org/jboss/windup/config/tags/test.tags.xml"), "/")
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
            );
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testTagsLoading() throws Exception
    {

        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {

            GraphRewrite event = new GraphRewrite(context);
            final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
            final DefaultParameterValueStore values = new DefaultParameterValueStore();
            evaluationContext.put(ParameterValueStore.class, values);

            RuleSubset.create(getNoOpRule(context)).perform(event, evaluationContext);

            //Assert.assertEquals();
        }
    }




    private Configuration getNoOpRule(GraphContext context)
    {
        return ConfigurationBuilder.begin().addRule()
            .when(new True())
            .perform(Log.message(Logger.Level.INFO, "Operation runs"));
    }
}
