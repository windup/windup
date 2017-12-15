package org.jboss.windup.reporting.model.rule;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.annotations.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Represents a rule provider
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@TypeValue(RuleProviderModel.TYPE)
public interface RuleProviderModel extends WindupVertexFrame
{
    String TYPE = "RuleProviderModel";

    String RULE_PROVIDER_ID = "ruleProviderID";
    String EXECUTED_RULES = "rules";

    @Property(RULE_PROVIDER_ID)
    String getRuleProviderID();

    @Property(RULE_PROVIDER_ID)
    RuleProviderModel setRuleProviderID(String id);

    @Adjacency(label = EXECUTED_RULES, direction = Direction.OUT)
    Iterable<RuleExecutionModel> getRules();

    @Adjacency(label = EXECUTED_RULES, direction = Direction.OUT)
    RuleProviderModel addRule(RuleExecutionModel rule);
}
