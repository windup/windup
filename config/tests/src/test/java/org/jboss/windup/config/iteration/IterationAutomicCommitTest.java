package org.jboss.windup.config.iteration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
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
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.WindupConfigurationModel;
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

import com.thinkaurelius.titan.core.EdgeLabel;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.RelationType;
import com.thinkaurelius.titan.core.TitanException;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.TitanIndexQuery;
import com.thinkaurelius.titan.core.TitanMultiVertexQuery;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.TransactionBuilder;
import com.thinkaurelius.titan.core.VertexLabel;
import com.thinkaurelius.titan.core.schema.EdgeLabelMaker;
import com.thinkaurelius.titan.core.schema.PropertyKeyMaker;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.schema.VertexLabelMaker;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * This tests whether or not the automatic insertion of progress tracking and commit operations is handled correctly.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class IterationAutomicCommitTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
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
    public void testAutomaticPeriodicCommit() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext baseContext = factory.create(folder))
        {
            CommitInterceptingGraphContext context = new CommitInterceptingGraphContext(baseContext);

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
            FileService fileModelService = new FileService(context);
            windupCfg.addInputPath(fileModelService.createByFilePath(OperatingSystemUtils.createTempDir()
                        .getAbsolutePath()));

            TestRuleProvider provider = new TestRuleProvider();
            Configuration configuration = provider.getConfiguration(context);

            RuleSubset.create(configuration).perform(event, evaluationContext);

            CommitInterceptingTitanGraph titanGraph = (CommitInterceptingTitanGraph) context.getGraph().getBaseGraph();
            Assert.assertEquals(1, titanGraph.commitCount);

            // Now create a few hundred FileModels to see if autocommit happens periodically
            for (int i = 0; i < 1200; i++)
            {
                fileModelService.create().setFilePath("foo." + i);
            }
            titanGraph.commitCount = 0;

            RuleSubset.create(configuration).perform(event, evaluationContext);
            Assert.assertEquals(2, titanGraph.commitCount);
        }
    }

    public class TestRuleProvider extends AbstractRuleProvider
    {
        public TestRuleProvider()
        {
            super(MetadataBuilder.forProvider(TestRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
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

    private class CommitInterceptingTitanGraph implements TitanGraph
    {
        private int commitCount = 0;
        private TitanGraph delegate;

        public CommitInterceptingTitanGraph(TitanGraph delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public TitanTransaction newTransaction()
        {
            return delegate.newTransaction();
        }

        @Override
        public TransactionBuilder buildTransaction()
        {
            return delegate.buildTransaction();
        }

        @Override
        public TitanManagement getManagementSystem()
        {
            return delegate.getManagementSystem();
        }

        @Override
        public boolean isOpen()
        {
            return delegate.isOpen();
        }

        @Override
        public boolean isClosed()
        {
            return delegate.isClosed();
        }

        @Override
        public void shutdown() throws TitanException
        {
            delegate.shutdown();
        }

        @Override
        public TitanVertex addVertex()
        {
            return delegate.addVertex();
        }

        @Override
        public TitanVertex addVertexWithLabel(String vertexLabel)
        {
            return delegate.addVertexWithLabel(vertexLabel);
        }

        @Override
        public TitanVertex addVertexWithLabel(VertexLabel vertexLabel)
        {
            return delegate.addVertexWithLabel(vertexLabel);
        }

        @Override
        public TitanVertex getVertex(long id)
        {
            return delegate.getVertex(id);
        }

        @Override
        public Map<Long, TitanVertex> getVertices(long... ids)
        {
            return delegate.getVertices(ids);
        }

        @Override
        public boolean containsVertex(long vertexid)
        {
            return delegate.containsVertex(vertexid);
        }

        @Override
        public TitanGraphQuery<? extends TitanGraphQuery> query()
        {
            return delegate.query();
        }

        @Override
        public TitanIndexQuery indexQuery(String indexName, String query)
        {
            return delegate.indexQuery(indexName, query);
        }

        @Override
        public TitanMultiVertexQuery<? extends TitanMultiVertexQuery> multiQuery(TitanVertex... vertices)
        {
            return delegate.multiQuery(vertices);
        }

        @Override
        public TitanMultiVertexQuery<? extends TitanMultiVertexQuery> multiQuery(Collection<TitanVertex> vertices)
        {
            return delegate.multiQuery(vertices);
        }

        @Override
        @Deprecated
        public void stopTransaction(Conclusion conclusion)
        {
            delegate.stopTransaction(conclusion);
        }

        @Override
        public void commit()
        {
            commitCount++;
            delegate.commit();
        }

        @Override
        public void rollback()
        {
            delegate.rollback();
        }

        @Override
        public Features getFeatures()
        {
            return delegate.getFeatures();
        }

        @Override
        public Vertex addVertex(Object id)
        {
            return delegate.addVertex(id);
        }

        @Override
        public Vertex getVertex(Object id)
        {
            return delegate.getVertex(id);
        }

        @Override
        public void removeVertex(Vertex vertex)
        {
            delegate.removeVertex(vertex);
        }

        @Override
        public Iterable<Vertex> getVertices()
        {
            return delegate.getVertices();
        }

        @Override
        public Iterable<Vertex> getVertices(String key, Object value)
        {
            return delegate.getVertices(key, value);
        }

        @Override
        public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label)
        {
            return delegate.addEdge(id, outVertex, inVertex, label);
        }

        @Override
        public Edge getEdge(Object id)
        {
            return delegate.getEdge(id);
        }

        @Override
        public void removeEdge(Edge edge)
        {
            delegate.removeEdge(edge);
        }

        @Override
        public Iterable<Edge> getEdges()
        {
            return delegate.getEdges();
        }

        @Override
        public Iterable<Edge> getEdges(String key, Object value)
        {
            return delegate.getEdges(key, value);
        }

        @Override
        public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass)
        {
            delegate.dropKeyIndex(key, elementClass);
        }

        @Override
        public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter... indexParameters)
        {
            delegate.createKeyIndex(key, elementClass, indexParameters);
        }

        @Override
        public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass)
        {
            return delegate.getIndexedKeys(elementClass);
        }

        @Override
        public PropertyKeyMaker makePropertyKey(String name)
        {
            return delegate.makePropertyKey(name);
        }

        @Override
        public EdgeLabelMaker makeEdgeLabel(String name)
        {
            return delegate.makeEdgeLabel(name);
        }

        @Override
        public VertexLabelMaker makeVertexLabel(String name)
        {
            return delegate.makeVertexLabel(name);
        }

        @Override
        public boolean containsRelationType(String name)
        {
            return delegate.containsRelationType(name);
        }

        @Override
        public RelationType getRelationType(String name)
        {
            return delegate.getRelationType(name);
        }

        @Override
        public boolean containsPropertyKey(String name)
        {
            return delegate.containsPropertyKey(name);
        }

        @Override
        public PropertyKey getOrCreatePropertyKey(String name)
        {
            return delegate.getOrCreatePropertyKey(name);
        }

        @Override
        public PropertyKey getPropertyKey(String name)
        {
            return delegate.getPropertyKey(name);
        }

        @Override
        public boolean containsEdgeLabel(String name)
        {
            return delegate.containsEdgeLabel(name);
        }

        @Override
        public EdgeLabel getOrCreateEdgeLabel(String name)
        {
            return delegate.getOrCreateEdgeLabel(name);
        }

        @Override
        public EdgeLabel getEdgeLabel(String name)
        {
            return delegate.getEdgeLabel(name);
        }

        @Override
        public boolean containsVertexLabel(String name)
        {
            return delegate.containsVertexLabel(name);
        }

        @Override
        public VertexLabel getVertexLabel(String name)
        {
            return delegate.getVertexLabel(name);
        }
    }

    private class CommitInterceptingEventGraph extends EventGraph<TitanGraph>
    {
        private CommitInterceptingTitanGraph commitInterceptingTitanGraph;

        public CommitInterceptingEventGraph(TitanGraph baseGraph)
        {
            super(baseGraph);
            this.commitInterceptingTitanGraph = new CommitInterceptingTitanGraph(baseGraph);
        }

        @Override
        public TitanGraph getBaseGraph()
        {
            return commitInterceptingTitanGraph;
        }
    }

    private class CommitInterceptingGraphContext implements GraphContext
    {
        private GraphContext delegate;
        private CommitInterceptingEventGraph commitInterceptingEventGraph;

        public CommitInterceptingGraphContext(GraphContext delegate)
        {
            this.delegate = delegate;
            this.commitInterceptingEventGraph = new CommitInterceptingEventGraph(delegate.getGraph().getBaseGraph());
        }

        @Override
        public Path getGraphDirectory()
        {
            return delegate.getGraphDirectory();
        }

        @Override
        public EventGraph<TitanGraph> getGraph()
        {
            return commitInterceptingEventGraph;
        }

        @Override
        public GraphContext create()
        {
            return delegate.create();
        }

        @Override
        public GraphContext load()
        {
            return delegate.load();
        }

        @Override
        public FramedGraph<EventGraph<TitanGraph>> getFramed()
        {
            return delegate.getFramed();
        }

        @Override
        public GraphTypeManager getGraphTypeManager()
        {
            return delegate.getGraphTypeManager();
        }

        @Override
        public TypeAwareFramedGraphQuery getQuery()
        {
            return delegate.getQuery();
        }

        @Override
        public void clear()
        {
            delegate.clear();
        }

        @Override
        public void setOptions(Map<String, Object> options)
        {
            delegate.setOptions(options);
        }

        @Override
        public Map<String, Object> getOptionMap()
        {
            return delegate.getOptionMap();
        }

        @Override
        public void close() throws IOException
        {
            delegate.close();
        }


        @Override
        public <T extends WindupVertexFrame> Service<T> service(Class<T> clazz)
        {
            return delegate.service(clazz);
        }


        @Override
        public <T extends WindupVertexFrame> T getUnique(Class<T> clazz)
        {
            return delegate.getUnique(clazz);
        }


        @Override
        public <T extends WindupVertexFrame> Iterable<T> findAll(Class<T> clazz)
        {
            return delegate.findAll(clazz);
        }


        @Override
        public <T extends WindupVertexFrame> T create(Class<T> clazz)
        {
            return delegate.create(clazz);
        }

        @Override
        public void commit()
        {
            delegate.commit();
        }
    }
}
