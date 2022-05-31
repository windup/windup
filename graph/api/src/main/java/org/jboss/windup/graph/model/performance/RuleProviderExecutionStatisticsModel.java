package org.jboss.windup.graph.model.performance;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * This model stores the time taken to execute all of the rules provided by a particular RuleProvider.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(RuleProviderExecutionStatisticsModel.TYPE)
public interface RuleProviderExecutionStatisticsModel extends WindupVertexFrame {
    String TYPE = "RuleProviderExecutionStatisticsModel";
    String TIME_TAKEN = "timeTaken";
    String RULE_INDEX = "ruleIndex";
    String RULE_PROVIDER_ID = "ruleProviderID";

    /**
     * The id of the RuleProvider
     */
    @Property(RULE_PROVIDER_ID)
    String getRuleProviderID();

    /**
     * The id of the RuleProvider
     */
    @Property(RULE_PROVIDER_ID)
    RuleProviderExecutionStatisticsModel setRuleProviderID(String id);

    /**
     * The execution order of the rules for sorting purposes
     */
    @Property(RULE_INDEX)
    int getRuleIndex();

    /**
     * The execution order of the rules for sorting purposes
     */
    @Property(RULE_INDEX)
    RuleProviderExecutionStatisticsModel setRuleIndex(int idx);

    /**
     * The time taken in milliseconds
     */
    @Property(TIME_TAKEN)
    int getTimeTaken();

    /**
     * The time taken in milliseconds
     */
    @Property(TIME_TAKEN)
    RuleProviderExecutionStatisticsModel setTimeTaken(int timeTakenMillis);
}
