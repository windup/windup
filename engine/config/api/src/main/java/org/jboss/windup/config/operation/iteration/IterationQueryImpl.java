package org.jboss.windup.config.operation.iteration;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilderGremlin;
import org.jboss.windup.config.graphsearch.GraphSearchGremlinCriterion;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.common.util.Assert;

import com.tinkerpop.blueprints.Vertex;

public class IterationQueryImpl extends Iteration implements IterationQueryCriteria
{
    private final Iteration root;
    private final GraphSearchConditionBuilderGremlin graphSearchConditionBuilderGremlin;

    private IterationPayloadManager payloadManager;

    public IterationQueryImpl(Iteration root, IterationPayloadManager manager)
    {
        this.root = root;
        setPayloadManager(manager);
        this.graphSearchConditionBuilderGremlin = new GraphSearchConditionBuilderGremlin();
    }

    @Override
    public void setPayloadManager(IterationPayloadManager payloadManager)
    {
        Assert.notNull(payloadManager, "Payload manager must not be null.");
        this.payloadManager = payloadManager;
    }

    @Override
    public IterationSelectionManager getSelectionManager()
    {
        return new IterationSelectionManager()
        {
            @Override
            public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, SelectionFactory factory)
            {
                if (graphSearchConditionBuilderGremlin != null)
                {
                    List<Vertex> initialVertices = new ArrayList<>();

                    Iterable<WindupVertexFrame> initialFrames = root.getSelectionManager().getFrames(event, factory);
                    for (WindupVertexFrame frame : initialFrames)
                    {
                        initialVertices.add(frame.asVertex());
                    }

                    graphSearchConditionBuilderGremlin.setInitialVertices(initialVertices);
                    Iterable<Vertex> v = graphSearchConditionBuilderGremlin.getResults(event);
                    return GraphUtil.toVertexFrames(event.getGraphContext(), v);
                }
                else
                {
                    return root.getSelectionManager().getFrames(event, factory);
                }
            }
        };
    }

    @Override
    public IterationPayloadManager getPayloadManager()
    {
        return payloadManager;
    }

    @Override
    public IterationQueryCriteria withCriterion(GraphSearchGremlinCriterion criterion)
    {
        graphSearchConditionBuilderGremlin.withCriterion(criterion);
        return this;
    }

}
