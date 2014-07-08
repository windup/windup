package org.jboss.windup.config.operation.iteration;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilderGremlin;
import org.jboss.windup.config.graphsearch.GremlinPipelineCriterion;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.IterationRoot;
import org.jboss.windup.config.runner.VarStack;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.common.util.Assert;

import com.tinkerpop.blueprints.Vertex;

/**
 * Gremlin Pipes adapter for Iteration.queryFor( ... ).
 */
public class GremlinPipesQueryImpl extends Iteration implements IterationQueryCriteria
{
    private final IterationRoot root;
    private final GraphSearchConditionBuilderGremlin graphSearchConditionBuilderGremlin;
    private IterationPayloadManager payloadManager;

    public GremlinPipesQueryImpl(IterationRoot root, IterationPayloadManager manager)
    {
        this.root = root;
        this.setPayloadManager(manager);
        this.graphSearchConditionBuilderGremlin = new GraphSearchConditionBuilderGremlin();
    }

    /**
     * @returns A SelectionManager which performs a Gremlin query.
     */
    @Override
    public IterationSelectionManager getSelectionManager()
    {
        return new GremlinIterationSelectionManager();
    }

    public GremlinPipesQuery addCriterion(GremlinPipelineCriterion criterion)
    {
        graphSearchConditionBuilderGremlin.addCriterion(criterion);
        return this;
    }

    /**
     * 
     */
    private class GremlinIterationSelectionManager implements IterationSelectionManager
    {
        @Override
        public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, VarStack varStack)
        {
            List<Vertex> initialVertices = getInitialVertices(event, varStack);

            // Perform the query and convert to frames.
            graphSearchConditionBuilderGremlin.setInitialVertices(initialVertices);
            Iterable<Vertex> v = graphSearchConditionBuilderGremlin.getResults(event);
            return GraphUtil.toVertexFrames(event.getGraphContext(), v);
        }

        /**
         * The initial vertices are those matched by previous query constructs. Iteration.[initial
         * vertices].queryFor().[gremlin pipe wrappers]
         */
        private List<Vertex> getInitialVertices(GraphRewrite event, VarStack varStack)
        {
            List<Vertex> initialVertices = new ArrayList<>();
            Iterable<WindupVertexFrame> initialFrames = root.getSelectionManager().getFrames(event, varStack);
            // TODO: Doesn't the root SelectionManager have the same event and varStack?
            for (WindupVertexFrame frame : initialFrames)
                initialVertices.add(frame.asVertex());
            return initialVertices;
        }
    }

    @Override
    public void setSelectionManager(IterationSelectionManager mgr)
    {
        // NO-OP, created internally.
    }

    @Override
    public void setPayloadManager(IterationPayloadManager payloadManager)
    {
        Assert.notNull(payloadManager, "Payload manager must not be null.");
        this.payloadManager = payloadManager;
    }

    @Override
    public IterationPayloadManager getPayloadManager()
    {
        return payloadManager;
    }

    @Override
    public IterationQueryCriteria endQuery()
    {
        return this;
    }
}
