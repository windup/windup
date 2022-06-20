package org.jboss.windup.reporting.ruleexecution;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.EventStrategy;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.graph.GraphListener;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Manages recording the history of {@link Rule}s executed by Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class RuleExecutionResultsListener implements RuleLifecycleListener {
    private final IdentityHashMap<Rule, RuleExecutionInformation> ruleExecutionInformation = new IdentityHashMap<>();
    private GraphRewrite event;
    private Rule currentRule = null;

    /**
     * Returns the {@link RuleExecutionResultsListener} for this execution of Windup.
     */
    public static RuleExecutionResultsListener instance(GraphRewrite event) {
        return (RuleExecutionResultsListener) event.getRewriteContext().get(RuleExecutionResultsListener.class);
    }

    /**
     *
     */
    public List<RuleExecutionInformation> getRuleExecutionInformation(AbstractRuleProvider provider) {
        RuleProviderRegistry ruleExecutionMetadata = RuleProviderRegistry.instance(event);
        List<Rule> rules = ruleExecutionMetadata.getRules(provider);

        List<RuleExecutionInformation> allRuleExecutions = new ArrayList<>();
        for (Rule rule : rules) {
            allRuleExecutions.add(ruleExecutionInformation.get(rule));
        }
        return allRuleExecutions;
    }

    @Override
    public void beforeExecution(GraphRewrite event) {
        ruleExecutionInformation.clear();
        this.event = event;
        event.getRewriteContext().put(RuleExecutionResultsListener.class, this);

        event.getGraphContext().registerGraphListener(new RuleExecutionGraphListener());
    }

    @Override
    public boolean beforeRuleEvaluation(GraphRewrite event, Rule rule, EvaluationContext context) {
        ruleExecutionInformation.put(rule, new RuleExecutionInformation(rule));
        RuleExecutionResultsListener.this.currentRule = rule;
        return false; // Don't request a stop.
    }

    @Override
    public boolean ruleEvaluationProgress(GraphRewrite event, String name, int currentPosition, int total, int timeRemainingInSeconds) {
        return false; // Don't request a stop.
    }

    @Override
    public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule,
                                             boolean result) {
        ruleExecutionInformation.get(rule).setEvaluationResult(result);
        if (!result) {
            RuleExecutionResultsListener.this.currentRule = null;
        }
    }

    @Override
    public boolean beforeRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule) {
        return false; // Don't request a stop.
    }

    @Override
    public void afterRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule) {
        ruleExecutionInformation.get(rule).setExecuted(true);
        RuleExecutionResultsListener.this.currentRule = null;
    }

    @Override
    public void afterRuleExecutionFailed(GraphRewrite event, EvaluationContext context, Rule rule, Throwable failureCause) {
        ruleExecutionInformation.get(rule).setFailed(true);
        ruleExecutionInformation.get(rule).setFailureCause(failureCause);
        RuleExecutionResultsListener.this.currentRule = null;
    }

    @Override
    public void afterExecution(GraphRewrite event) {
    }

    /**
     * Stores or counts the information about graph changes, especially which rules created which elements.
     */
    private class RuleExecutionGraphListener implements GraphListener {
        @Override
        public synchronized void vertexAdded(Vertex vertex) {
            if (currentRule != null) {
                ruleExecutionInformation.get(currentRule).addVertexIDAdded(vertex.id());
            }
        }

        @Override
        public void vertexPropertyChanged(final Vertex element, final Property oldValue, final Object setValue,
                                          final Object... vertexPropertyKeyValues) {
        }
    }
}
