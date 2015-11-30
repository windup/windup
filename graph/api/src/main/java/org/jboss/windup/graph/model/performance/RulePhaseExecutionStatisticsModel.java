package org.jboss.windup.graph.model.performance;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This stores the time it takes to execute all of the rules within a particular phase of execution.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(RulePhaseExecutionStatisticsModel.TYPE)
public interface RulePhaseExecutionStatisticsModel extends WindupVertexFrame
{
    public static final String ORDER_EXECUTED = "orderExecuted";
    public static final String RULE_PHASE = "rulePhase";
    public static final String TYPE = "RulePhaseExecutionStatistics";
    public static final String TIME_TAKEN = "timeTaken";

    /**
     * The phase represented by this model.
     */
    @Property(RULE_PHASE)
    public RulePhaseExecutionStatisticsModel setRulePhase(String phase);

    /**
     * The phase represented by this model.
     */
    @Property(RULE_PHASE)
    public String getRulePhase();

    /**
     * The time taken for all of the rules within this phase (in milliseconds)
     */
    @Property(TIME_TAKEN)
    public RulePhaseExecutionStatisticsModel setTimeTaken(int timeTakenMillis);

    /**
     * The time taken for all of the rules within this phase (in milliseconds)
     */
    @Property(TIME_TAKEN)
    public int getTimeTaken();

    /**
     * Stores an increasing index indicating the order in which the phases were executed (lower numbers execute earlier than larger numbers).
     */
    @Property(ORDER_EXECUTED)
    public int getOrderExecuted();

    /**
     * Stores an increasing index indicating the order in which the phases were executed (lower numbers execute earlier than larger numbers).
     */
    @Property(ORDER_EXECUTED)
    public void setOrderExecuted(int orderExecuted);
}
