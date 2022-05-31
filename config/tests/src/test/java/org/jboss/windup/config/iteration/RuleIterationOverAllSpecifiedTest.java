package org.jboss.windup.config.iteration;

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
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.rules.files.FileMapping;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import javax.inject.Inject;
import java.nio.file.Path;

@RunWith(Arquillian.class)
public class RuleIterationOverAllSpecifiedTest {
    public static int TestSimple2ModelCounter = 0;
    public static int TestSimple1ModelCounter = 0;
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClasses(TestSimple1Model.class, TestSimple2Model.class);
    }

    private DefaultEvaluationContext createEvalContext(GraphRewrite event) {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

    @Test
    public void testTypeSelection() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            TestSimple1Model vertex = context.getFramed().addFramedVertex(TestSimple1Model.class);
            context.getFramed().addFramedVertex(TestSimple2Model.class);
            context.getFramed().addFramedVertex(TestSimple2Model.class);

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addFramedVertex(WindupConfigurationModel.class);
            FileService fileModelService = new FileService(context);
            windupCfg.addInputPath(fileModelService.createByFilePath(OperatingSystemUtils.createTempDir()
                    .getAbsolutePath()));

            TestRuleIterationOverAllSpecifiedProvider provider = new TestRuleIterationOverAllSpecifiedProvider();
            Configuration configuration = provider.getConfiguration(null);

            // this should call perform()
            RuleSubset.create(configuration).perform(event, evaluationContext);
            Assert.assertEquals(1, TestSimple1ModelCounter);
            Assert.assertEquals(2, TestSimple2ModelCounter);
            vertex.remove();
            // this should call otherwise()
            RuleSubset.create(configuration).perform(event, evaluationContext);
            Assert.assertEquals(1, TestSimple1ModelCounter);
            Assert.assertEquals(4, TestSimple2ModelCounter);
        }
    }

    @Test(expected = Exception.class)
    public void testTypeSelectionWithException() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            TestSimple1Model vertex = context.getFramed().addFramedVertex(TestSimple1Model.class);
            context.getFramed().addFramedVertex(TestSimple2Model.class);
            context.getFramed().addFramedVertex(TestSimple2Model.class);

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addFramedVertex(WindupConfigurationModel.class);
            FileService fileModelService = new FileService(context);
            windupCfg.addInputPath(fileModelService
                    .createByFilePath(OperatingSystemUtils.createTempDir().getAbsolutePath()));

            TestRuleIterationOverAllSpecifiedWithExceptionProvider provider = new TestRuleIterationOverAllSpecifiedWithExceptionProvider();
            Configuration configuration = provider.getConfiguration(null);

            // this should call perform()
            RuleSubset.create(configuration).perform(event, evaluationContext);
            Assert.assertEquals(TestSimple1ModelCounter, 1);
            Assert.assertEquals(TestSimple2ModelCounter, 2);
            vertex.remove();
            // this should call otherwise()
            RuleSubset.create(configuration).perform(event, evaluationContext);
            Assert.assertEquals(TestSimple1ModelCounter, 1);
            Assert.assertEquals(TestSimple2ModelCounter, 4);
        }
    }

    public class TestRuleIterationOverAllSpecifiedProvider extends AbstractRuleProvider {
        public TestRuleIterationOverAllSpecifiedProvider() {
            super(MetadataBuilder.forProvider(TestRuleIterationOverAllSpecifiedProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(TestSimple2Model.class).as("list_variable"))
                    .perform(Iteration
                            .over("list_variable").as("single_var")
                            .perform(new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    Variables varStack = Variables.instance(event);
                                    TestSimple2Model singleVariable =
                                            Iteration.getCurrentPayload(
                                                    varStack,
                                                    TestSimple2Model.class,
                                                    "single_var");
                                    if (singleVariable != null) {
                                        TestSimple2ModelCounter++;
                                    }
                                }
                            })
                            .endIteration()
                    )
                    .addRule()
                    .when(Query.fromType(TestSimple1Model.class).as("list_variable"))
                    .perform(Iteration
                            .over("list_variable").as("single_var2")
                            .perform(new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    Variables varStack = Variables.instance(event);
                                    TestSimple1Model singleVariable =
                                            Iteration.getCurrentPayload(
                                                    varStack,
                                                    TestSimple1Model.class,
                                                    "single_var2");
                                    if (singleVariable != null) {
                                        TestSimple1ModelCounter++;
                                    }
                                }
                            })
                            .endIteration().and(
                                    RuleSubset.create(
                                            ConfigurationBuilder.begin().addRule(FileMapping.from("adsfsd").to(FileModel.class))
                                    )
                            )

                    );
            return configuration;
        }
        // @formatter:off

    }

    public class TestRuleIterationOverAllSpecifiedWithExceptionProvider extends AbstractRuleProvider {
        public TestRuleIterationOverAllSpecifiedWithExceptionProvider() {
            super(MetadataBuilder.forProvider(TestRuleIterationOverAllSpecifiedWithExceptionProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(TestSimple2Model.class).as("list_variable"))
                    .perform(Iteration
                            .over("list_variable").as("single_var")
                            .perform(new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    Variables varStack = Variables.instance(event);
                                    TestSimple2Model singleVariable =
                                            Iteration.getCurrentPayload(
                                                    varStack,
                                                    TestSimple2Model.class,
                                                    "single_");
                                }
                            })
                            .endIteration()
                    );
            return configuration;
        }
        // @formatter:on

    }

}