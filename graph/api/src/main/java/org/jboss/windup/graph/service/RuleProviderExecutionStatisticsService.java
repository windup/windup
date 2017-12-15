package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.performance.RuleProviderExecutionStatisticsModel;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.util.structures.Pair;

/**
 * This service provides useful methods for dealing with {@link RuleProviderExecutionStatisticsModel} Vertices within
 * the graph
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class RuleProviderExecutionStatisticsService extends GraphService<RuleProviderExecutionStatisticsModel>
{
    public RuleProviderExecutionStatisticsService(GraphContext context)
    {
        super(context, RuleProviderExecutionStatisticsModel.class);
    }

    /**
     * Return an {@link Iterable} of all RuleProviderExecutionStatisticsModel ordered by Index (ascending)
     */
    public Iterable<RuleProviderExecutionStatisticsModel> findAllOrderedByIndex()
    {
        GraphTraversal<RuleProviderExecutionStatisticsModel, RuleProviderExecutionStatisticsModel> pipeline = new GraphTraversal<>(
                    findAll());
        pipeline.order(new PipeFunction<Pair<RuleProviderExecutionStatisticsModel, RuleProviderExecutionStatisticsModel>, Integer>()
        {
            @Override
            public Integer compute(
                        Pair<RuleProviderExecutionStatisticsModel, RuleProviderExecutionStatisticsModel> argument)
            {
                return argument.getA().getRuleIndex() - argument.getB().getRuleIndex();
            }
        });
        return pipeline;
    }
}
