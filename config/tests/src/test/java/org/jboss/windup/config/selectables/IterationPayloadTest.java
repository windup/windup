package org.jboss.windup.config.selectables;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import org.apache.tinkerpop.gremlin.structure.Vertex;

@RunWith(Arquillian.class)
public class IterationPayloadTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap
                .create(AddonArchive.class)
                .addClasses(TestIterationPayloadTestRuleProvider.class, TestChildModel.class, TestParentModel.class)
                .addBeansXML();
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Inject
    private TestIterationPayloadTestRuleProvider provider;

    @Inject
    private NestedIterationRuleProvider nestedIterationRuleProvider;

    @Test
    public void testIterationVariableResolving() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            GraphRewrite event = new GraphRewrite(context);
            final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
            final DefaultParameterValueStore values = new DefaultParameterValueStore();
            evaluationContext.put(ParameterValueStore.class, values);

            fillData(context);

            RuleSubset.create(provider.getConfiguration(null)).perform(event, evaluationContext);

            Assert.assertEquals(3, provider.getChildCount());
            Assert.assertEquals(2, provider.getParentCount());
            Assert.assertEquals(3, provider.getActualChildCount());
            Assert.assertEquals(3, provider.getActualParentCount());
        }
    }

    @Test
    public void testNestedIteration() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            GraphRewrite event = new GraphRewrite(context);
            final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
            final DefaultParameterValueStore values = new DefaultParameterValueStore();
            evaluationContext.put(ParameterValueStore.class, values);

            fillData(context);

            RuleSubset.create(nestedIterationRuleProvider.getConfiguration(null)).perform(event, evaluationContext);

            Assert.assertEquals(2, nestedIterationRuleProvider.outerVertices.size());
            Assert.assertEquals(3, nestedIterationRuleProvider.innerVertices.size());
            Assert.assertEquals(6, nestedIterationRuleProvider.iterationCount);
        }
    }

    private void fillData(GraphContext context) {
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
    }

    public static class NestedIterationRuleProvider extends AbstractRuleProvider {
        private Set<Vertex> outerVertices = new HashSet<>();
        private Set<Vertex> innerVertices = new HashSet<>();
        private int iterationCount = 0;

        public NestedIterationRuleProvider() {
            super(MetadataBuilder.forProvider(NestedIterationRuleProvider.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(TestParentModel.class).as("outer").and(Query.fromType(TestChildModel.class).as("inner")))
                    .perform(Iteration.over("outer").as("outer_item")
                            .perform(
                                    Iteration.over("inner").as("inner_item").perform(
                                            new AbstractIterationOperation<TestChildModel>() {
                                                @Override
                                                public void perform(GraphRewrite event, EvaluationContext context,
                                                                    TestChildModel payload) {
                                                    WindupVertexFrame outerFrame = Variables.instance(event)
                                                            .findSingletonVariable("outer_item");
                                                    outerVertices.add(outerFrame.getElement());
                                                    innerVertices.add(payload.getElement());
                                                    iterationCount++;
                                                }
                                            }).endIteration())
                            .endIteration());
        }
    }
}
