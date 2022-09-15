package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.performance.RuleProviderExecutionStatisticsModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This service provides useful methods for dealing with {@link RuleProviderExecutionStatisticsModel} Vertices within
 * the graph
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class RuleProviderExecutionStatisticsService extends GraphService<RuleProviderExecutionStatisticsModel> {
    public RuleProviderExecutionStatisticsService(GraphContext context) {
        super(context, RuleProviderExecutionStatisticsModel.class);
    }

    /**
     * Return an {@link Iterable} of all RuleProviderExecutionStatisticsModel ordered by Index (ascending)
     */
    public Iterable<RuleProviderExecutionStatisticsModel> findAllOrderedByIndex() {
        List<RuleProviderExecutionStatisticsModel> immutableList = findAll();
        List<RuleProviderExecutionStatisticsModel> mutableList = new ArrayList<>(immutableList.size());
        mutableList.addAll(immutableList);

        Collections.sort(mutableList, Comparator.comparingInt(RuleProviderExecutionStatisticsModel::getRuleIndex));
        return mutableList;
    }
}
