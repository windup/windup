package org.jboss.windup.reporting.ruleexecution;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.WindupRuleMetadata;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

/**
 * Manages recording the history of {@link Rule}s executed by Windup.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class RuleExecutionResultsListener implements RuleLifecycleListener
{
    private IdentityHashMap<Rule, RuleExecutionInformation> ruleExecutionInformation = new IdentityHashMap<>();
    private GraphRewrite event;
    private Rule currentRule = null;

    /**
     * Returns the {@link RuleExecutionResultsListener} for this execution of Windup.
     */
    public static RuleExecutionResultsListener instance(GraphRewrite event)
    {
        return (RuleExecutionResultsListener) event.getRewriteContext().get(RuleExecutionResultsListener.class);
    }

    /**
     * 
     */
    public List<RuleExecutionInformation> getRuleExecutionInformation(WindupRuleProvider provider)
    {
        WindupRuleMetadata ruleExecutionMetadata = WindupRuleMetadata.instance(event);
        List<Rule> rules = ruleExecutionMetadata.getRules(provider);

        List<RuleExecutionInformation> allRuleExecutions = new ArrayList<>();
        for (Rule rule : rules)
        {
            allRuleExecutions.add(ruleExecutionInformation.get(rule));
        }
        return allRuleExecutions;
    }

    @Override
    public void beforeExecution(GraphRewrite event)
    {
        ruleExecutionInformation.clear();
        this.event = event;
        event.getRewriteContext().put(RuleExecutionResultsListener.class, this);
        event.getGraphContext().getGraph().addListener(new GraphChangeListener());
    }

    @Override
    public void beforeRuleEvaluation(GraphRewrite event, Rule rule, EvaluationContext context)
    {
        ruleExecutionInformation.put(rule, new RuleExecutionInformation(rule));
        RuleExecutionResultsListener.this.currentRule = rule;
    }

    @Override
    public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule,
                boolean result)
    {
        ruleExecutionInformation.get(rule).setEvaluationResult(result);
        if (!result)
        {
            RuleExecutionResultsListener.this.currentRule = null;
        }
    }

    @Override
    public void beforeRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule)
    {
    }

    @Override
    public void afterRuleOperationsPerformed(GraphRewrite event, EvaluationContext context, Rule rule)
    {
        ruleExecutionInformation.get(rule).setExecuted(true);
        RuleExecutionResultsListener.this.currentRule = null;
    }

    @Override
    public void afterRuleExecutionFailed(GraphRewrite event, EvaluationContext context, Rule rule, Throwable failureCause)
    {
        ruleExecutionInformation.get(rule).setFailed(true);
        ruleExecutionInformation.get(rule).setFailureCause(failureCause);
        RuleExecutionResultsListener.this.currentRule = null;
    }

    @Override
    public void afterExecution(GraphRewrite event)
    {
    }

    private class GraphChangeListener implements GraphChangedListener
    {

        @Override
        public synchronized void vertexAdded(Vertex vertex)
        {
            if (currentRule != null)
            {
                ruleExecutionInformation.get(currentRule).addVertexIDAdded(vertex.getId());
            }
        }

        @Override
        public synchronized void vertexRemoved(Vertex vertex, Map<String, Object> props)
        {
            if (currentRule != null)
            {
                ruleExecutionInformation.get(currentRule).addVertexIDRemoved(vertex.getId());
            }
        }

        @Override
        public synchronized void edgeAdded(Edge edge)
        {
            if (currentRule != null)
            {
                ruleExecutionInformation.get(currentRule).addEdgeIDAdded(edge.getId());
            }
        }

        @Override
        public synchronized void edgeRemoved(Edge edge, Map<String, Object> props)
        {
            if (currentRule != null)
            {
                ruleExecutionInformation.get(currentRule).addVertexIDRemoved(edge.getId());
            }
        }

        @Override
        public void edgePropertyRemoved(Edge edge, String key, Object removedValue)
        {
        }

        @Override
        public void vertexPropertyChanged(Vertex vertex, String key, Object oldValue, Object setValue)
        {
        }

        @Override
        public void vertexPropertyRemoved(Vertex vertex, String key, Object removedValue)
        {
        }

        @Override
        public void edgePropertyChanged(Edge edge, String key, Object oldValue, Object setValue)
        {
        }

    }
}
