package org.jboss.windup.config.parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternBuilder;

@RunWith(Arquillian.class)
public class ParameterWiringTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.utils:utils"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addPackage(ParameterWiringTestModel.class.getPackage())
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testIterationVariableResolving() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {

            GraphRewrite event = new GraphRewrite(context);
            final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
            final DefaultParameterValueStore values = new DefaultParameterValueStore();
            evaluationContext.put(ParameterValueStore.class, values);

            GraphService<ParameterWiringTestModel> service = new GraphService<>(context, ParameterWiringTestModel.class);

            ParameterWiringTestModel model1 = service.create();
            model1.setValue("The quick brown fox jumped over the lazy dog.");

            ParameterWiringTestModel model2 = service.create();
            model2.setValue("The lazy dog slept under the quick brown fox.");

            ParameterWiringTestModel model3 = service.create();
            model3.setValue("The lazy fox jumped over the quick brown dog.");

            ParameterWiringTestModel model4 = service.create();
            model4.setValue("The lazy fox slept under the quick brown dog.");

            ParameterWiringTestModel model5 = service.create();
            model5.setValue("The quick brown fox jumped over the lazy fox.");

            ParameterWiringTestModel model6 = service.create();
            model6.setValue("The lazy fox slept under the quick brown fox.");

            ParameterWiringTestModel model7 = service.create();
            model7.setValue("The lazy fox slept under the lazy fox.");

            ParameterWiringTestModel model8 = service.create();
            model8.setValue("The quick brown fox slept under the quick brown fox.");

            ParameterWiringTestModel model9 = service.create();
            model9.setValue("The stupid fox slept under the stupid fox.");

            ParameterWiringTestRuleProvider provider = new ParameterWiringTestRuleProvider();
            RuleSubset.create(provider.getConfiguration(context)).perform(event, evaluationContext);

            Assert.assertEquals(2, provider.getMatchCount());
            Assert.assertTrue(provider.getResults().contains(model7));
            Assert.assertTrue(provider.getResults().contains(model8));
        }
    }

    private static class ParameterWiringTestRuleProvider extends WindupRuleProvider
    {
        private int matchCount;
        private List<ParameterWiringTestModel> results = new ArrayList<>();

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
                        .addRule()
                        .when(ParameterWiringTestModelCondition.matchesValue("{*}{adjective} {animal} {verb}{*}")
                                    .as("1")
                                    .and(ParameterWiringTestModelCondition.matchesValue("{*}{adjective} {animal}.")
                                                .from("1").as("result"))
                        )
                        .perform(Iteration.over("result").perform(
                                    new AbstractIterationOperation<ParameterWiringTestModel>()
                                    {
                                        @Override
                                        public void perform(GraphRewrite event, EvaluationContext context,
                                                    ParameterWiringTestModel payload)
                                        {
                                            matchCount++;
                                            results.add(payload);
                                        }
                                    })
                                    .endIteration()
                        )
                        .where("adjective").matches("lazy|quick brown")
                        .where("animal").matches("fox")
                        .where("verb").matches("\\w+");
        }

        public int getMatchCount()
        {
            return matchCount;
        }

        public List<ParameterWiringTestModel> getResults()
        {
            return results;
        }
    }

    @Test
    public void testIterationVariableResolving2() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {

            GraphRewrite event = new GraphRewrite(context);
            final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
            final DefaultParameterValueStore values = new DefaultParameterValueStore();
            evaluationContext.put(ParameterValueStore.class, values);

            GraphService<ParameterWiringTestModel> service = new GraphService<>(context, ParameterWiringTestModel.class);

            ParameterWiringTestModel model1 = service.create();
            model1.setValue("The quick brown fox jumped over the lazy dog.");

            ParameterWiringTestModel model2 = service.create();
            model2.setValue("The lazy dog slept under the quick brown fox.");

            ParameterWiringTestModel model3 = service.create();
            model3.setValue("The lazy fox jumped over the quick brown dog.");

            ParameterWiringTestModel model4 = service.create();
            model4.setValue("The lazy fox slept under the quick brown dog.");

            ParameterWiringTestModel model5 = service.create();
            model5.setValue("The quick brown fox jumped over the lazy fox.");

            ParameterWiringTestModel model6 = service.create();
            model6.setValue("The lazy fox slept under the quick brown fox.");

            ParameterWiringTestModel model7 = service.create();
            model7.setValue("The lazy fox slept under the lazy fox.");

            ParameterWiringTestModel model8 = service.create();
            model8.setValue("The quick brown fox slept under the quick brown fox.");

            ParameterWiringTestModel model9 = service.create();
            model9.setValue("The stupid fox slept under the stupid fox.");

            ParameterWiringTestRuleProvider2 provider = new ParameterWiringTestRuleProvider2();
            RuleSubset.create(provider.getConfiguration(context)).perform(event, evaluationContext);

            Assert.assertEquals(3, provider.getMatchCount());
            Assert.assertTrue(provider.getResults().contains(model7));
            Assert.assertTrue(provider.getResults().contains(model8));
            Assert.assertTrue(provider.getResults().contains(model9));

            Assert.assertEquals(3, provider.getResultParameterValues().size());
            Assert.assertTrue(provider.getResultParameterValues().contains("fox slept brown"));
            Assert.assertTrue(provider.getResultParameterValues().contains("fox slept lazy"));
            Assert.assertTrue(provider.getResultParameterValues().contains("fox slept stupid"));
        }
    }

    private static class ParameterWiringTestRuleProvider2 extends WindupRuleProvider
    {
        private int matchCount;
        private List<ParameterWiringTestModel> results = new ArrayList<>();
        private List<String> resultParameterValues = new ArrayList<>();

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
                        .addRule()
                        .when(ParameterWiringTestModelCondition.matchesValue("{*}{adjective} {animal} {verb}{*}")
                                    .as("1")
                                    .and(ParameterWiringTestModelCondition.matchesValue("{*}{adjective} {animal}.")
                                                .from("1").as("result"))
                        )
                        .perform(Iteration.over("result").perform(
                                    new ParameterizedIterationOperation<ParameterWiringTestModel>()
                                    {
                                        RegexParameterizedPatternBuilder builder = new RegexParameterizedPatternBuilder(
                                                    "{animal} {verb} {adjective}");

                                        @Override
                                        public void performParameterized(GraphRewrite event, EvaluationContext context,
                                                    ParameterWiringTestModel payload)
                                        {
                                            matchCount++;
                                            results.add(payload);
                                            String resultParameterValue = builder.build(event, context);
                                            resultParameterValues.add(resultParameterValue);
                                        }

                                        @Override
                                        public Set<String> getRequiredParameterNames()
                                        {
                                            return builder.getRequiredParameterNames();
                                        }

                                        @Override
                                        public void setParameterStore(ParameterStore store)
                                        {
                                            builder.setParameterStore(store);
                                        }
                                    })
                                    .endIteration()
                        )
                        .where("adjective").matches("\\b\\w+\\b")
                        .where("animal").matches("fox")
                        .where("verb").matches("\\b\\w+\\b");
        }

        public int getMatchCount()
        {
            return matchCount;
        }

        public List<ParameterWiringTestModel> getResults()
        {
            return results;
        }

        public List<String> getResultParameterValues()
        {
            return resultParameterValues;
        }
    }

    @Test
    public void testIterationVariableResolving3() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {

            GraphRewrite event = new GraphRewrite(context);
            final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
            final DefaultParameterValueStore values = new DefaultParameterValueStore();
            evaluationContext.put(ParameterValueStore.class, values);

            GraphService<ParameterWiringTestModel> service = new GraphService<>(context, ParameterWiringTestModel.class);

            ParameterWiringTestModel model1 = service.create();
            model1.setValue("The quick brown fox jumped over the lazy dog.");

            ParameterWiringTestModel model2 = service.create();
            model2.setValue("The lazy dog slept under the quick brown fox.");

            ParameterWiringTestModel model3 = service.create();
            model3.setValue("The lazy fox jumped over the quick brown dog.");

            ParameterWiringTestModel model4 = service.create();
            model4.setValue("The lazy fox slept under the quick brown dog.");

            ParameterWiringTestModel model5 = service.create();
            model5.setValue("The quick brown fox jumped over the lazy fox.");

            ParameterWiringTestModel model6 = service.create();
            model6.setValue("The lazy fox slept under the quick brown fox.");

            ParameterWiringTestModel model7 = service.create();
            model7.setValue("The lazy fox slept under the lazy fox.");

            ParameterWiringTestModel model8 = service.create();
            model8.setValue("The quick brown fox slept under the quick brown fox.");

            ParameterWiringTestModel model9 = service.create();
            model9.setValue("The stupid fox slept under the stupid fox.");

            ParameterWiringTestModel model10 = service.create();
            model10.setValue("stupid.");

            ParameterWiringTestModel model11 = service.create();
            model11.setValue("smart.");

            ParameterWiringTestModel model12 = service.create();
            model12.setValue("some lazy.");

            ParameterWiringTestModel model13 = service.create();
            model13.setValue("some brown.");

            ParameterWiringTestModel model14 = service.create();
            model14.setValue("some stupid.");

            ParameterWiringTestRuleProvider3 provider = new ParameterWiringTestRuleProvider3();
            RuleSubset.create(provider.getConfiguration(context)).perform(event, evaluationContext);

            Assert.assertEquals(1, provider.getMatchCount());
            Assert.assertEquals(model14, provider.getResults().iterator().next());
        }
    }

    private static class ParameterWiringTestRuleProvider3 extends WindupRuleProvider
    {
        private int matchCount;
        private List<ParameterWiringTestModel> results = new ArrayList<>();

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder
                        .begin()
                        .addRule()
                        .when(ParameterWiringTestModelCondition
                                    .matchesValue("{*} {adjective} {animal} {verb}{*}")
                                    .as("1")
                                    .and(ParameterWiringTestModelCondition
                                                .matchesValue("{*} {adjective} {otheranimal}.")
                                                .from("1").as("2"))
                                    .and(ParameterWiringTestModelCondition
                                                .matchesValue("{*} {adjective} {animal} {*} {isstupid} {otheranimal}.")
                                                .from("2").as("3"))
                                    .and(ParameterWiringTestModelCondition.matchesValue("{isstupid}.").as("4"))
                                    .and(ParameterWiringTestModelCondition.matchesValue("some {isstupid}.")
                                                .as("result"))
                        )
                        .perform(Iteration.over("result").perform(
                                    new AbstractIterationOperation<ParameterWiringTestModel>()
                                    {
                                        @Override
                                        public void perform(GraphRewrite event, EvaluationContext context,
                                                    ParameterWiringTestModel payload)
                                        {
                                            matchCount++;
                                            results.add(payload);
                                        }
                                    })
                                    .endIteration()
                        )
                        .where("adjective").matches("\\w+")
                        .where("animal").matches("fox")
                        .where("verb").matches("\\w+")
                        .where("isstupid").matches(".*");
        }

        public int getMatchCount()
        {
            return matchCount;
        }

        public List<ParameterWiringTestModel> getResults()
        {
            return results;
        }
    }

    @Test
    public void testIterationVariableResolving4() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {

            GraphRewrite event = new GraphRewrite(context);
            final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
            final DefaultParameterValueStore values = new DefaultParameterValueStore();
            evaluationContext.put(ParameterValueStore.class, values);

            GraphService<ParameterWiringTestModel> service = new GraphService<>(context, ParameterWiringTestModel.class);

            ParameterWiringTestModel model1 = service.create();
            model1.setValue("The quick brown fox jumped over the lazy dog.");

            ParameterWiringTestModel model2 = service.create();
            model2.setValue("The lazy dog slept under the quick brown fox.");

            ParameterWiringTestModel model3 = service.create();
            model3.setValue("The lazy fox jumped over the quick brown dog.");

            ParameterWiringTestModel model4 = service.create();
            model4.setValue("The lazy fox slept under the quick brown dog.");

            ParameterWiringTestModel model5 = service.create();
            model5.setValue("The quick brown fox jumped over the lazy fox.");

            ParameterWiringTestRuleProvider4 provider = new ParameterWiringTestRuleProvider4();
            RuleSubset.create(provider.getConfiguration(context)).perform(event, evaluationContext);

            Assert.assertEquals(4, provider.getMatchCount());
            Assert.assertTrue(provider.getResults().contains(model1));
            Assert.assertTrue(provider.getResults().contains(model3));
            Assert.assertTrue(provider.getResults().contains(model4));
            Assert.assertTrue(provider.getResults().contains(model5));
        }
    }

    private static class ParameterWiringTestRuleProvider4 extends WindupRuleProvider
    {
        private int matchCount;
        private List<ParameterWiringTestModel> results = new ArrayList<>();

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder
                        .begin()
                        .addRule()
                        .when(ParameterWiringTestModelCondition.matchesValue("{*} {adjective} {animal} {verb}{*}").as(
                                    "result"))
                        .perform(Iteration.over("result").perform(
                                    new AbstractIterationOperation<ParameterWiringTestModel>()
                                    {
                                        @Override
                                        public void perform(GraphRewrite event, EvaluationContext context,
                                                    ParameterWiringTestModel payload)
                                        {
                                            matchCount++;
                                            results.add(payload);
                                        }
                                    })
                                    .endIteration()
                        ).where("animal").matches("fox");
        }

        public int getMatchCount()
        {
            return matchCount;
        }

        public List<ParameterWiringTestModel> getResults()
        {
            return results;
        }
    }
}
