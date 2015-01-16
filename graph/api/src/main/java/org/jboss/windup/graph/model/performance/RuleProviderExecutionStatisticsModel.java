package org.jboss.windup.graph.model.performance;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This model stores the time taken to execute all of the rules provided by a particular RuleProvider.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@TypeValue(RuleProviderExecutionStatisticsModel.TYPE)
public interface RuleProviderExecutionStatisticsModel extends WindupVertexFrame
{
    public static final String TYPE = "RuleProviderExecutionStatisticsModel";
    public static final String TIME_TAKEN = "timeTaken";
    public static final String RULE_INDEX = "ruleIndex";
    public static final String RULE_PROVIDER_ID = "ruleProviderID";

    /**
     * The id of the RuleProvider
     */
    @Property(RULE_PROVIDER_ID)
    public RuleProviderExecutionStatisticsModel setRuleProviderID(String id);

    /**
     * The id of the RuleProvider
     */
    @Property(RULE_PROVIDER_ID)
    public String getRuleProviderID();

    /**
     * The execution order of the rules for sorting purposes
     */
    @Property(RULE_INDEX)
    public RuleProviderExecutionStatisticsModel setRuleIndex(int idx);

    /**
     * The execution order of the rules for sorting purposes
     */
    @Property(RULE_INDEX)
    public int getRuleIndex();

    /**
     * The time taken in milliseconds
     */
    @Property(TIME_TAKEN)
    public RuleProviderExecutionStatisticsModel setTimeTaken(int timeTakenMillis);

    /**
     * The time taken in milliseconds
     */
    @Property(TIME_TAKEN)
    public int getTimeTaken();
}
