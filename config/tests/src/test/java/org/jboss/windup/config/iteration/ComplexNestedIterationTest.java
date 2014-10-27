package org.jboss.windup.config.iteration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.iteration.RuleIterationOverAllSpecifiedTest.TestRuleIterationOverAllSpecifiedProvider;
import org.jboss.windup.config.iteration.RuleIterationOverAllSpecifiedTest.TestRuleIterationOverAllSpecifiedWithExceptionProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class ComplexNestedIterationTest
{

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
                                TestRuleIterationOverAllSpecifiedProvider.class,
                                TestRuleIterationOverAllSpecifiedWithExceptionProvider.class,
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
    public void testImplicitNestedIteration() throws Exception
    {
        op1FoundFrames.clear();
        op2FoundFrames.clear();
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {

            TestSimple1Model vertex = context.getFramed().addVertex(null, TestSimple1Model.class);
            context.getFramed().addVertex(null, TestSimple2Model.class);
            context.getFramed().addVertex(null, TestSimple2Model.class);

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            TestComplexNestedImplicitIteration provider = new TestComplexNestedImplicitIteration();
            Configuration configuration = provider.getConfiguration(context);

            // this should call perform()
            RuleSubset.create(configuration).perform(event, evaluationContext);

            Assert.assertEquals(1, op1FoundFrames.size());
            Assert.assertEquals(2, op2FoundFrames.size());
        }

    }

    private final List<WindupVertexFrame> op1FoundFrames = new ArrayList<>();
    private final List<WindupVertexFrame> op2FoundFrames = new ArrayList<>();

    public class TestComplexNestedImplicitIteration extends WindupRuleProvider
    {
        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            Configuration configuration = ConfigurationBuilder.begin()
                        .addRule()
                        .when(
                                    Query.find(TestSimple1Model.class).as("model1_list1")
                                    .or(Query.find(TestSimple2Model.class).as("model2_list1"))
                        )
                        .perform(new AbstractIterationOperation<WindupVertexFrame>()
                        {
                            {
                                setInputVariableName("model1_list1");
                            }
                            
                            public void perform(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload) {
                                op1FoundFrames.add(payload);
                            }
                        }.and(new AbstractIterationOperation<WindupVertexFrame>()
                        {
                            {
                                setInputVariableName("model2_list1");
                            }
                            
                            public void perform(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload) {
                                op2FoundFrames.add(payload);
                            }
                        }));
            return configuration;
        }

    }

}
