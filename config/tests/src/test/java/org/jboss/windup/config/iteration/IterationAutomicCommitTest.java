package org.jboss.windup.config.iteration;

import com.syncleus.ferma.Traversable;
import com.syncleus.ferma.WrappedFramedGraph;
import org.janusgraph.core.JanusGraph;
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
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.GraphListener;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.Service;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * This tests whether or not the automatic insertion of progress tracking and commit operations is handled correctly.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class IterationAutomicCommitTest {
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
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    private DefaultEvaluationContext createEvalContext(GraphRewrite event) {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

    @Test
    public void testAutomaticPeriodicCommit() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext baseContext = factory.create(folder, true)) {
            CommitInterceptingGraphContext context = new CommitInterceptingGraphContext(baseContext);

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addFramedVertex(WindupConfigurationModel.class);
            FileService fileModelService = new FileService(context);
            windupCfg.addInputPath(fileModelService.createByFilePath(OperatingSystemUtils.createTempDir()
                    .getAbsolutePath()));

            TestRuleProvider provider = new TestRuleProvider();
            Configuration configuration = provider.getConfiguration(null);

            RuleSubset.create(configuration).perform(event, evaluationContext);

            Assert.assertEquals(1, context.commitCount);

            // Now create a few hundred FileModels to see if autocommit happens periodically
            for (int i = 0; i < 1200; i++) {
                fileModelService.create().setFilePath("foo." + i);
            }
            context.commitCount = 0;

            RuleSubset.create(configuration).perform(event, evaluationContext);
            Assert.assertEquals(2, context.commitCount);
        }
    }

    public class TestRuleProvider extends AbstractRuleProvider {
        public TestRuleProvider() {
            super(MetadataBuilder.forProvider(TestRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(FileModel.class))
                    .perform(Iteration
                            .over()
                            .perform(new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    // no-op
                                }
                            })
                            .endIteration()
                    );
            return configuration;
        }
        // @formatter:on

    }

    private class CommitInterceptingGraphContext implements GraphContext {
        private int commitCount = 0;
        private GraphContext delegate;

        public CommitInterceptingGraphContext(GraphContext delegate) {
            this.delegate = delegate;
        }

        @Override
        public Path getGraphDirectory() {
            return delegate.getGraphDirectory();
        }

        @Override
        public JanusGraph getGraph() {
            return delegate.getGraph();
        }

        @Override
        public GraphContext create(boolean enableListeners) {
            return delegate.create(enableListeners);
        }

        @Override
        public GraphContext load() {
            return delegate.load();
        }

        @Override
        public WrappedFramedGraph<JanusGraph> getFramed() {
            return delegate.getFramed();
        }

        @Override
        public GraphTypeManager getGraphTypeManager() {
            return delegate.getGraphTypeManager();
        }

        @Override
        public Traversable<?, ?> getQuery(Class<? extends WindupVertexFrame> kind) {
            return delegate.getQuery(kind);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public void setOptions(Map<String, Object> options) {
            delegate.setOptions(options);
        }

        @Override
        public Map<String, Object> getOptionMap() {
            return delegate.getOptionMap();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public <T extends WindupVertexFrame> Service<T> service(Class<T> clazz) {
            return delegate.service(clazz);
        }

        @Override
        public <T extends WindupVertexFrame> T getUnique(Class<T> clazz) {
            return delegate.getUnique(clazz);
        }

        @Override
        public <T extends WindupVertexFrame> Iterable<T> findAll(Class<T> clazz) {
            return delegate.findAll(clazz);
        }

        @Override
        public <T extends WindupVertexFrame> T create(Class<T> clazz) {
            return delegate.create(clazz);
        }

        @Override
        public void commit() {
            commitCount++;
            delegate.commit();
        }

        @Override
        public void registerGraphListener(GraphListener listener) {
            delegate.registerGraphListener(listener);
        }
    }
}
