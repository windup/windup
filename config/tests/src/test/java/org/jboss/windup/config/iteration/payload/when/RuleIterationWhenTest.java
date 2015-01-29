package org.jboss.windup.config.iteration.payload.when;

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
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.FileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class RuleIterationWhenTest
{
    private static final String SECOND_NAME = "second_name";
    private static final String NAME = "name";
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
                                TestWhenProvider.class,
                                TestWhenModel.class)
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
    public void testTypeSelection() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {

            TestWhenModel vertex = context.getFramed().addVertex(null, TestWhenModel.class);
            vertex.setName(NAME);
            vertex.setSecondName(NAME);
            vertex = context.getFramed().addVertex(null, TestWhenModel.class);
            vertex.setName(SECOND_NAME);
            vertex.setSecondName(SECOND_NAME);
            vertex = context.getFramed().addVertex(null, TestWhenModel.class);
            vertex.setName(NAME);
            vertex.setSecondName(SECOND_NAME);
            vertex = context.getFramed().addVertex(null, TestWhenModel.class);
            vertex.setName(SECOND_NAME);
            vertex.setSecondName(NAME);

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
            FileService fileModelService = new FileService(context);
            windupCfg.setInputPath(fileModelService.createByFilePath(OperatingSystemUtils.createTempDir()
                        .getAbsolutePath()));

            TestWhenProvider provider = new TestWhenProvider();
            Configuration configuration = provider.getConfiguration(context);

            // this should call perform()
            RuleSubset.create(configuration).perform(event, evaluationContext);
            Assert.assertEquals(1, provider.getMatchCount());
            Assert.assertEquals(0, provider.getTripleCondition());
            Assert.assertEquals(1, provider.getOuterConditionCounter());
            Assert.assertEquals(1, provider.getSameConditionCounter());
            Assert.assertEquals(provider.getModel().getName(), NAME);
            Assert.assertEquals(provider.getModel().getSecondName(), SECOND_NAME);
        }
    }

    public class TestWhenProvider extends WindupRuleProvider
    {

        private int matchCount = 0;
        private int tripleCondition = 0;
        private int outerConditionCounter = 0;
        private int sameConditionCounter = 0;
        private TestWhenModel model;

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            Configuration configuration = ConfigurationBuilder.begin()
                        .addRule()
                        .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.NAME, NAME))
                        .perform(Iteration.over()
                                    .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.SECOND_NAME, SECOND_NAME))
                                    .perform(
                                    new GraphOperation()
                                    {
                                        @Override
                                        public void perform(GraphRewrite event, EvaluationContext context)
                                        {
                                            matchCount++;
                                        }
                                    }.and(new AbstractIterationOperation<TestWhenModel>() {

                                        @Override
                                        public void perform(GraphRewrite event, EvaluationContext context,
                                                    TestWhenModel payload)
                                        {
                                          model=payload;
                                        }
                                    
                                    })).endIteration()
                                    
                        )
                        
                        .addRule()
                        .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.NAME, NAME))
                        .perform(Iteration.over()
                                    .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.SECOND_NAME, SECOND_NAME))
                                    .perform(Iteration.over()
                                                .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.SECOND_NAME, NAME))
                                                .perform( new GraphOperation()
                                                {
                                                    @Override
                                                    public void perform(GraphRewrite event, EvaluationContext context)
                                                    {
                                                        tripleCondition++;
                                                    }
                                                }).endIteration()
                                                ).endIteration()
                                    
                        )
                        
                        .addRule()
                        .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.NAME, NAME).as("outer_variable"))
                        .perform(Iteration.over()
                                    .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.SECOND_NAME, SECOND_NAME))
                                    .perform(Iteration.over("outer_variable")
                                                .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.SECOND_NAME, NAME))
                                                .perform( new GraphOperation()
                                                {
                                                    @Override
                                                    public void perform(GraphRewrite event, EvaluationContext context)
                                                    {
                                                        outerConditionCounter++;
                                                    }
                                                }).endIteration()
                                                ).endIteration()
                                    
                        )
                        .addRule()
                        .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.NAME, NAME).as("outer_variable"))
                        .perform(Iteration.over()
                                    .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.SECOND_NAME, SECOND_NAME))
                                    .perform(Iteration.over()
                                                .when(Query.fromType(TestWhenModel.class).withProperty(TestWhenModel.SECOND_NAME, SECOND_NAME))
                                                .perform( new GraphOperation()
                                                {
                                                    @Override
                                                    public void perform(GraphRewrite event, EvaluationContext context)
                                                    {
                                                        sameConditionCounter++;
                                                    }
                                                }).endIteration()
                                                ).endIteration()
                                    
                        )
                        ;
            return configuration;
        }
        
        public int getMatchCount() {
            return matchCount;
        }
        public int getTripleCondition() {
            return tripleCondition;
        }
        public int getOuterConditionCounter() {
            return outerConditionCounter;
        }
        
        public TestWhenModel getModel() {
            return this.model;
        }
        
        public int getSameConditionCounter() {
            return sameConditionCounter;
        }

    }

  
}