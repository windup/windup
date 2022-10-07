package org.jboss.windup.reporting.model.rule;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;

/**
 * Represents execution phase
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@TypeValue(ExecutionPhaseModel.TYPE)
public interface ExecutionPhaseModel extends WindupVertexFrame {
    String TYPE = "ExecutionPhaseModel";

    String NAME = "name";
    String RULE_PROVIDERS = "ruleProviders";

    @Property(NAME)
    String getName();

    @Property(NAME)
    ExecutionPhaseModel setName(String name);

    @Adjacency(label = RULE_PROVIDERS, direction = Direction.OUT)
    List<RuleProviderModel> getRuleProviders();

    @Adjacency(label = RULE_PROVIDERS, direction = Direction.OUT)
    void addRuleProvider(RuleProviderModel ruleProvider);

    @Adjacency(label = RULE_PROVIDERS, direction = Direction.OUT)
    void setRuleProviders(List<RuleProviderModel> ruleProviders);
}
