package org.jboss.windup.reporting.model.rule;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;

/**
 * Represents a rule provider
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@TypeValue(RuleProviderModel.TYPE)
public interface RuleProviderModel extends WindupVertexFrame {
    String TYPE = "RuleProviderModel";

    String RULE_PROVIDER_ID = "ruleProviderID";
    String EXECUTED_RULES = "rules";

    @Property(RULE_PROVIDER_ID)
    String getRuleProviderID();

    @Property(RULE_PROVIDER_ID)
    void setRuleProviderID(String id);

    @Adjacency(label = EXECUTED_RULES, direction = Direction.OUT)
    List<RuleExecutionModel> getRules();

    @Adjacency(label = EXECUTED_RULES, direction = Direction.OUT)
    void addRule(RuleExecutionModel rule);
}
