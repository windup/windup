package org.jboss.windup.config.iteration;

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
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * Testing the Iteration.over(SomeType.class) approach.
 * 
 * @author mbriskar
 * 
 */
@RunWith(Arquillian.class)
public class RuleIterationOverTypesTest
{
    public static int TestSimple2ModelCounter = 0;
    public static int TestSimple1ModelCounter = 0;

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClasses(
                                TestRuleIterationOverTypesProvider.class,
                                TestRuleIterationOverTypesWithExceptionProvider.class,
                                TestSimple1Model.class,
                                TestSimple2Model.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    private DefaultEvaluationContext createEvalContext(GraphRewrite event)
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

    @Test
    public void testTypeSelection()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        TestSimple1Model vertex = context.getFramed().addVertex(null, TestSimple1Model.class);
        context.getFramed().addVertex(null, TestSimple2Model.class);
        context.getFramed().addVertex(null, TestSimple2Model.class);

        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
        windupCfg.setInputPath("/tmp/testpath");
        windupCfg.setSourceMode(true);

        TestRuleIterationOverTypesProvider provider = new TestRuleIterationOverTypesProvider();
        Configuration configuration = provider.getConfiguration(context);

        // this should call perform()
        RuleSubset.create(configuration).perform(event, evaluationContext);
        Assert.assertEquals(TestSimple1ModelCounter, 1);
        Assert.assertEquals(TestSimple2ModelCounter, 2);
        vertex.asVertex().remove();
        // this should call otherwise()
        RuleSubset.create(configuration).perform(event, evaluationContext);
        Assert.assertEquals(TestSimple1ModelCounter, 1);
        Assert.assertEquals(TestSimple2ModelCounter, 4);

    }

    @Test(expected = Exception.class)
    public void testTypeSelectionWithException()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        TestSimple1Model vertex = context.getFramed().addVertex(null, TestSimple1Model.class);
        context.getFramed().addVertex(null, TestSimple2Model.class);
        context.getFramed().addVertex(null, TestSimple2Model.class);

        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
        windupCfg.setInputPath("/tmp/testpath");
        windupCfg.setSourceMode(true);

        TestRuleIterationOverTypesWithExceptionProvider provider = new TestRuleIterationOverTypesWithExceptionProvider();
        Configuration configuration = provider.getConfiguration(context);

        // this should call perform()
        RuleSubset.create(configuration).perform(event, evaluationContext);
        Assert.assertEquals(TestSimple1ModelCounter, 1);
        Assert.assertEquals(TestSimple2ModelCounter, 2);
        vertex.asVertex().remove();
        // this should call otherwise()
        RuleSubset.create(configuration).perform(event, evaluationContext);
        Assert.assertEquals(TestSimple1ModelCounter, 1);
        Assert.assertEquals(TestSimple2ModelCounter, 4);

    }

    public class TestRuleIterationOverTypesProvider extends WindupRuleProvider
    {

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.MIGRATION_RULES;
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            Configuration configuration = ConfigurationBuilder.begin()
            .addRule()
            .when(Query.find(TestSimple2Model.class))
            .perform(Iteration
                .over(TestSimple2Model.class)
                .perform(new GraphOperation()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context)
                    {
                        TestSimple2ModelCounter++;
                    }
                })
                .endIteration()
            )
            .addRule()
            .when(Query.find(TestSimple1Model.class))
            .perform(Iteration
                .over(TestSimple1Model.class)
                .perform(new GraphOperation()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context)
                    {
                        TestSimple1ModelCounter++;
                    }
                })
                .endIteration()
            );
            return configuration;
        }

    }
    
    public class TestRuleIterationOverTypesWithExceptionProvider extends WindupRuleProvider
    {
        
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.MIGRATION_RULES;
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            Configuration configuration = ConfigurationBuilder.begin()
            .addRule()
            .when(Query.find(TestSimple2Model.class))
            .perform(Iteration
                .over(TestSimple1Model.class)
                .perform(new GraphOperation()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context)
                    {
                    }
                })
                .endIteration()
            );
            return configuration;
        }

    }

}