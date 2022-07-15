package org.jboss.windup.reporting.model.rule;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Property;
import org.jboss.windup.config.RuleUtils;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.ruleexecution.RuleExecutionInformationForReading;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Represents rule execution with some additional statistics
 * (count of added/removed edges/vertices, execution status, error message)
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@TypeValue(RuleExecutionModel.TYPE)
public interface RuleExecutionModel extends WindupVertexFrame {
    String TYPE = "RuleExecutionModel";

    String RULE_ID = "ruleId";
    String RULE_CONTENTS = "ruleContents";
    String COUNT_ADDED_VERTICES = "countAddedVertices";
    String COUNT_ADDED_EDGES = "countAddedEdges";
    String COUNT_REMOVED_VERTICES = "countRemovedVertices";
    String COUNT_REMOVED_EDGES = "countRemovedEdges";
    String IS_EXECUTED = "isExecuted";
    String IS_FAILED = "isFailed";
    String FAILURE_MESSAGE = "failureMessage";

    @Property(RULE_ID)
    String getRuleId();

    @Property(RULE_ID)
    RuleExecutionModel setRuleId(String id);

    @Property(RULE_CONTENTS)
    String getRuleContents();

    @Property(RULE_CONTENTS)
    RuleExecutionModel setRuleContents(String ruleContents);

    @Property(COUNT_ADDED_VERTICES)
    Integer getCountAddedVertices();

    @Property(COUNT_ADDED_VERTICES)
    RuleExecutionModel setCountAddedVertices(Integer vertexIDsAdded);

    @Property(COUNT_ADDED_EDGES)
    Integer getCountAddedEdges();

    @Property(COUNT_ADDED_EDGES)
    RuleExecutionModel setCountAddedEdges(Integer edgeIDSAdded);

    @Property(COUNT_REMOVED_VERTICES)
    Integer getCountRemovedVertices();

    @Property(COUNT_REMOVED_VERTICES)
    RuleExecutionModel setCountRemovedVertices(Integer vertexIDsRemoved);

    @Property(COUNT_REMOVED_EDGES)
    Integer getCountRemovedEdges();

    @Property(COUNT_REMOVED_EDGES)
    RuleExecutionModel setCountRemovedEdges(Integer getEdgeIDSRemoved);

    @Property(IS_EXECUTED)
    Boolean getExecuted();

    @Property(IS_EXECUTED)
    RuleExecutionModel setExecuted(Boolean executed);

    @Property(IS_FAILED)
    Boolean getFailed();

    @Property(IS_FAILED)
    RuleExecutionModel setFailed(Boolean failed);

    @Property(FAILURE_MESSAGE)
    String getFailureMessage();

    @Property(FAILURE_MESSAGE)
    RuleExecutionModel setFailureMessage(String failureMessage);

    default void setDataFromRuleInfo(RuleExecutionInformationForReading ruleInformation) {
        Rule rule = ruleInformation.getRule();
        this.setRuleId(rule.getId());

        String ruleContents = RuleUtils.ruleToRuleContentsString(rule, 0);
        this.setRuleContents(ruleContents);

        this.setCountAddedVertices(ruleInformation.getVertexIDsAdded());
        this.setCountAddedEdges(ruleInformation.getEdgeIDsAdded());

        this.setCountRemovedVertices(ruleInformation.getVertexIDsRemoved());
        this.setCountRemovedEdges(ruleInformation.getEdgeIDsRemoved());

        this.setExecuted(ruleInformation.isExecuted());
        this.setFailed(ruleInformation.isFailed());

        Throwable failureCase = ruleInformation.getFailureCause();

        if (failureCase != null) {
            String failureMessage = failureCase.getMessage();
            this.setFailureMessage(failureMessage);
        }
    }
}
