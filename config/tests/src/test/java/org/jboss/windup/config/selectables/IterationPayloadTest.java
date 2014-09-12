package org.jboss.windup.config.selectables;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class IterationPayloadTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addClasses(TestIterationPayloadTestRuleProvider.class, TestChildModel.class, TestParentModel.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Inject
    private TestIterationPayloadTestRuleProvider provider;

    @Test
    public void testIterationVariableResolving()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);

        GraphService<TestParentModel> parentService = new GraphService<>(context, TestParentModel.class);
        GraphService<TestChildModel> childService = new GraphService<>(context, TestChildModel.class);

        TestParentModel parent1 = parentService.create();
        parent1.setName("parent1");
        TestParentModel parent2 = parentService.create();
        parent1.setName("parent2");

        TestChildModel parent1child1 = childService.create();
        parent1child1.setParent(parent1);
        parent1child1.setName("parent1child1");
        TestChildModel parent1child2 = childService.create();
        parent1child2.setParent(parent2);
        parent1child2.setName("parent1child2");

        TestChildModel parent2child1 = childService.create();
        parent2child1.setParent(parent1);
        parent2child1.setName("parent2child1");

        RuleSubset.create(provider.getConfiguration(context)).perform(event, evaluationContext);

        Assert.assertEquals(3, provider.getChildCount());
        Assert.assertEquals(2, provider.getParentCount());
        Assert.assertEquals(3, provider.getActualChildCount());
        Assert.assertEquals(3, provider.getActualParentCount());

    }
}
