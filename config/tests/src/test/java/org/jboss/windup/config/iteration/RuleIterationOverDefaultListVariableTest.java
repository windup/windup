package org.jboss.windup.config.iteration;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
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

/**
 * Testing the Iteration.over() approach.
 * 
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * 
 */
@RunWith(Arquillian.class)
public class RuleIterationOverDefaultListVariableTest
{
    public static int TestSimple2ModelCounter = 0;
    public static int TestSimple1ModelCounter = 0;

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClasses(
                                TestRuleIterationOverDefaultListVariableProvider.class,
                                TestSimple1Model.class,
                                TestSimple2Model.class);
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

            TestSimple1Model vertex = context.getFramed().addVertex(null, TestSimple1Model.class);
            context.getFramed().addVertex(null, TestSimple2Model.class);
            context.getFramed().addVertex(null, TestSimple2Model.class);

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
            FileService fileModelService = new FileService(context);
            windupCfg.setInputPath(fileModelService.createByFilePath(OperatingSystemUtils.createTempDir()
                        .getAbsolutePath()));

            TestRuleIterationOverDefaultListVariableProvider provider = new TestRuleIterationOverDefaultListVariableProvider();
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
    }

    public class TestRuleIterationOverDefaultListVariableProvider extends AbstractRuleProvider
    {
        public TestRuleIterationOverDefaultListVariableProvider()
        {
            super(MetadataBuilder.forProvider(TestRuleIterationOverDefaultListVariableProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            Configuration configuration = ConfigurationBuilder.begin()
            .addRule()
            .when(Query.fromType(TestSimple2Model.class).as("abc"))
            .perform(Iteration
                .over()
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
            .when(Query.fromType(TestSimple1Model.class))
            .perform(Iteration
                .over()
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
        // @formatter:on

    }

}