package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.performance.RuleProviderExecutionStatisticsModel;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Comparator;

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
        pipeline.order().by(Comparator.comparingInt(RuleProviderExecutionStatisticsModel::getRuleIndex));

        return pipeline;
    }
}
