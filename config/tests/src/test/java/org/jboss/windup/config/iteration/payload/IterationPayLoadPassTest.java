package org.jboss.windup.config.iteration.payload;

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
import org.jboss.windup.config.iteration.TestSimple1Model;
import org.jboss.windup.config.iteration.TestSimple2Model;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
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

@RunWith(Arquillian.class)
public class IterationPayLoadPassTest
{
    public static int modelCounter = 0;

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
                                TestPayloadModel.class,
                                TestSimple2Model.class,
                                IterationPayLoadPassTest.class,
                                TestIterationPayLoadNotPassProvider.class,
                                TestIterationPayLoadPassProvider.class)
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
    public void testPayloadPass()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        TestPayloadModel vertex = context.getFramed().addVertex(null, TestPayloadModel.class);
        context.getFramed().addVertex(null, TestPayloadModel.class);
        context.getFramed().addVertex(null, TestPayloadModel.class);

        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
        windupCfg.setInputPath("/tmp/testpath");
        windupCfg.setSourceMode(true);

        TestIterationPayLoadPassProvider provider = new TestIterationPayLoadPassProvider();
        Configuration configuration = provider.getConfiguration(context);

        // this should call perform()
        RuleSubset.create(configuration).perform(event, evaluationContext);
        Assert.assertEquals(3, modelCounter);
        modelCounter = 0;

    }

    @Test(expected = Exception.class)
    public void testPayloadNotPass()
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

        TestIterationPayLoadNotPassProvider provider = new TestIterationPayLoadNotPassProvider();
        Configuration configuration = provider.getConfiguration(context);

        // this should call perform()
        RuleSubset.create(configuration).perform(event, evaluationContext);
        Assert.assertEquals(3, modelCounter);
        modelCounter = 0;

    }

    public class TestIterationPayLoadPassProvider extends WindupRuleProvider
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
                        .when(Query.find(TestPayloadModel.class).as("list_variable"))
                        .perform(Iteration
                                    .over("list_variable").as("single_var")
                                    .perform(new AbstractIterationOperation<TestPayloadModel>()
                                    {
                                        @Override
                                        public void perform(GraphRewrite event, EvaluationContext context,
                                                    TestPayloadModel model)
                                        {
                                            modelCounter++;
                                            Assert.assertNotNull(model);
                                        }
                                    })
                                    .endIteration()
                        );
            return configuration;
        }

    }

    public class TestIterationPayLoadNotPassProvider extends WindupRuleProvider
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
            Configuration configuration = ConfigurationBuilder
                        .begin()
                        .addRule()
                        .when(Query.find(TestSimple2Model.class).as("do_not_perform")
                                    .and(Query.find(TestPayloadModel.class).as("list_variable")))
                        .perform(Iteration //first iteration
                                    .over("list_variable")
                                    .as("single_var")
                                    .perform(Iteration.over("do_not_perform") //second iteration
                                                .perform(new AbstractIterationOperation<TestPayloadModel>("single_var")
                                                {
                                                    @Override
                                                    public void perform(GraphRewrite event, EvaluationContext context,
                                                                TestPayloadModel model)
                                                    {
                                                        //should access the outer iteration, not the inner one
                                                        modelCounter++;
                                                        Assert.assertNotNull(model);
                                                    }
                                                }).endIteration())

                                    .endIteration()
                        );
            return configuration;
        }

    }

}