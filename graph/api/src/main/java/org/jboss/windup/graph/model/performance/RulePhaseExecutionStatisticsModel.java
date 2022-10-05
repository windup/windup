package org.jboss.windup.graph.model.performance;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.jboss.windup.graph.Property;

import java.util.Comparator;

/**
 * This stores the time it takes to execute all of the rules within a particular phase of execution.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(RulePhaseExecutionStatisticsModel.TYPE)
public interface RulePhaseExecutionStatisticsModel extends WindupVertexFrame {
    String ORDER_EXECUTED = "orderExecuted";
    String RULE_PHASE = "rulePhase";
    String TYPE = "RulePhaseExecutionStatisticsModel";
    String TIME_TAKEN = "timeTaken";

    /**
     * The phase represented by this model.
     */
    @Property(RULE_PHASE)
    RulePhaseExecutionStatisticsModel setRulePhase(String phase);

    /**
     * The phase represented by this model.
     */
    @Property(RULE_PHASE)
    String getRulePhase();

    /**
     * The time taken for all of the rules within this phase (in milliseconds)
     */
    @Property(TIME_TAKEN)
    RulePhaseExecutionStatisticsModel setTimeTaken(int timeTakenMillis);

    /**
     * The time taken for all of the rules within this phase (in milliseconds)
     */
    @Property(TIME_TAKEN)
    int getTimeTaken();

    /**
     * Stores an increasing index indicating the order in which the phases were executed (lower numbers execute earlier than larger numbers).
     */
    @Property(ORDER_EXECUTED)
    int getOrderExecuted();

    /**
     * Stores an increasing index indicating the order in which the phases were executed (lower numbers execute earlier than larger numbers).
     */
    @Property(ORDER_EXECUTED)
    void setOrderExecuted(int orderExecuted);

    Comparator<RulePhaseExecutionStatisticsModel> BY_ORDER_EXECUTED = new Comparator<RulePhaseExecutionStatisticsModel>() {
        @Override
        public int compare(RulePhaseExecutionStatisticsModel o1, RulePhaseExecutionStatisticsModel o2) {
            return o1.getOrderExecuted() - o2.getOrderExecuted();
        }
    };
}
